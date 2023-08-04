package com.example.whatsappclone.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.CombinedVibration
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.whatsappclone.R
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.activity.meetingInvitation.IncomingInvitationActivity
import com.example.whatsappclone.broadcastReceiver.NotificationBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class CallNotificationForegroundService: Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val NOTIFICATION_ID = 120
        private const val CHANNEL_ID = "call_channel_id"
        private const val CALL_RESPONSE_ACTION_KEY = "call_response"
        private const val CALL_RECEIVE = "call_receive"
        private const val CALL_CANCEL = "call_cancel"
        private const val TAG = "foreground_service"
        private const val REMOTE_MESSAGE_MEETING_ROOM = "meeting_room"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            val name = intent?.getStringExtra("name")
            val imageUrl = intent?.getStringExtra("image")
            val number = intent?.getStringExtra("number")
            val token = intent?.getStringExtra("fcmToken")
            val msg = intent?.getStringExtra("type")
            val meetingRoom = intent?.getStringExtra(Constants.REMOTE_MESSAGE_MEETING_ROOM)
            createNotificationChannel()
            serviceScope.launch {
                val image = getBitmapFromUrl(imageUrl.toString())
                val notification = createCallNotification(name, imageUrl, number, image, token, msg, meetingRoom)
                startForeground(NOTIFICATION_ID, notification)
            }
        }catch (e: Exception ) {
            e.printStackTrace();
        }
        return START_STICKY
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createCallNotification(
        name: String?,
        imageUrl: String?,
        number: String?,
        image: Bitmap?,
        token: String?,
        msg: String?,
        meetingRoom: String?
    ): Notification? {
            val receiveCallAction = Intent(this, IncomingInvitationActivity::class.java)
            receiveCallAction.putExtra("name", name)
            receiveCallAction.putExtra("image", imageUrl)
            receiveCallAction.putExtra("number", number)
            receiveCallAction.putExtra("fcmToken", token)
            receiveCallAction.putExtra("message", msg)
            receiveCallAction.putExtra(REMOTE_MESSAGE_MEETING_ROOM, meetingRoom)
            receiveCallAction.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_RECEIVE)
            receiveCallAction.action = "RECEIVE_CALL"

            Log.d(TAG, "$meetingRoom")
            Log.d(TAG, "$name  $number  $msg")

            val cancelCallAction = Intent(this, NotificationBroadcastReceiver::class.java)
            cancelCallAction.putExtra("name", name)
            cancelCallAction.putExtra("image", imageUrl)
            cancelCallAction.putExtra("number", number)
            cancelCallAction.putExtra("fcmToken", token)
            cancelCallAction.putExtra("message", msg)
            cancelCallAction.putExtra(REMOTE_MESSAGE_MEETING_ROOM, meetingRoom)
            cancelCallAction.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL)
            cancelCallAction.action = "CANCEL_CALL"

            val receiveCallPendingIntent = PendingIntent.getActivity(this, 10, receiveCallAction, PendingIntent.FLAG_IMMUTABLE)
            val receiveCallPending = PendingIntent.getBroadcast(this, 1200, receiveCallAction, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val cancelCallPendingIntent = PendingIntent.getBroadcast(this, 1201, cancelCallAction, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Incoming Video Call")
                .setContentText(name)
                .setSmallIcon(R.drawable.whatsapp_logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setLargeIcon(image)
                .addAction(R.drawable.baseline_videocam, "Accept", receiveCallPendingIntent)
                .addAction(R.drawable.baseline_close_24, "Reject", cancelCallPendingIntent)
                .setFullScreenIntent(receiveCallPending, true)
                .setAutoCancel(true)
                .build()
    }
    private fun createNotificationChannel() {
        val name = "Call Channel" // Replace with your own channel name
        val description = "Incoming Call Notification" // Replace with your own channel description
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        channel.apply {
            enableLights(true)
            enableVibration(true)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun getBitmapFromUrl(imgUrl: String?): Bitmap? = withContext(Dispatchers.IO){
        try{
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
    private fun vibratePhone(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrateManager: VibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            if(vibrateManager.defaultVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)){
                vibrateManager.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)
                            .compose()
                    )
                )
            }
        } else {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val VIBRATION_DURATION = 20000L
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        stopForeground(STOP_FOREGROUND_DETACH)
//    }
}
