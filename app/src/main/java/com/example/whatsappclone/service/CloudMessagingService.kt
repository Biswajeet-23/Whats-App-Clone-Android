package com.example.whatsappclone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.workManager.CallForegroundWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CloudMessagingService: FirebaseMessagingService() {
    companion object{
        private val CHANNEL_ID = "chat"
        private val CHANNEL_ID2 = "call"
        private val NOTIFICATION_ID = 1
        private const val MESSAGE_TYPE = "type"
        private const val NAME = "name"
        private const val IMAGE = "image"
        private const val NUMBER = "number"
        private const val FCM_TOKEN = "fcmToken"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM" , "token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val videoMsg = "video"
        val imageUrl = message.data["imgUrl"]
//        val image = getBitmapFromUrl(imageUrl.toString())
        if(message.data["title"] == Constants.TITLE){

            if(message.data["message"] == Constants.REMOTE_MESSAGE_INVITATION_RESPONSE){
                if(message.data["invitationResponse"] == Constants.REMOTE_MESSAGE_INVITATION_CANCELLED){

                    Log.d("Firebase Messaging", "Incoming Response:" + "${message.data["invitationResponse"]}")
                    val intent = Intent(Constants.LOCAL_BROADCAST_INCOMING_MESSAGE_RESPONSE)
                    intent.putExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE, message.data["invitationResponse"])
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                }else{

                    Log.d("Firebase Messaging", "Incoming Response:" + "${message.data["invitationResponse"]}")
                    val intent = Intent(Constants.LOCAL_BROADCAST_OUTGOING_MESSAGE_RESPONSE)
                    intent.putExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE, message.data["invitationResponse"])
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }else{

                Log.d("Firebase Messaging", "Incoming Call")
                Log.d("Firebase Messaging", "Meeting_RoomID: ${message.data["meetingRoom"]}")

                val inputData = Data.Builder()
                    .putString(MESSAGE_TYPE, message.data["message"])
                    .putString(NAME, message.data["senderName"])
                    .putString(IMAGE, message.data["imgUrl"])
                    .putString(NUMBER, message.data["senderNumber"])
                    .putString(FCM_TOKEN, message.data["fcmToken"])
                    .putString(Constants.REMOTE_MESSAGE_MEETING_ROOM, message.data["meetingRoom"])
                    .build()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val foregroundWork = OneTimeWorkRequest.Builder(CallForegroundWorker::class.java)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .setInitialDelay(0L, TimeUnit.MILLISECONDS)
                    .addTag(CallForegroundWorker.TAG)
                    .build()

                WorkManager.getInstance(this)
                    .enqueueUniqueWork("callForegroundWork", ExistingWorkPolicy.REPLACE, foregroundWork)
            }

        }else{

            Log.d("Firebase Messaging", "Message Received")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val manager = getSystemService(Context.NOTIFICATION_SERVICE)
            createNotificationChannel(manager as NotificationManager)

            val intent1 = PendingIntent.getActivities(
                this, 0,
                arrayOf(intent), PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.whatsapp_logo)
                .setAutoCancel(true)
                .setContentIntent(intent1)
                .build()

            manager.notify(Random.nextInt(), notification)

        }
    }
    private fun getBitmapFromUrl(imgUrl: String?): Bitmap? {
        return try{
            val url = URL(imgUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream as InputStream
            BitmapFactory.decodeStream(inputStream)
        }catch (e: IOException){
            e.printStackTrace()
            null
        }
    }

    private fun createNotificationChannel(manager: NotificationManager){
        val channel = NotificationChannel(CHANNEL_ID, "whatsAppChat",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply{
            description = "Chat"
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }
}

