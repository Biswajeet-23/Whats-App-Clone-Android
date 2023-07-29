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
import com.bumptech.glide.Glide
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R
import com.example.whatsappclone.activity.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class CloudMessagingService: FirebaseMessagingService() {

    private val channelId = "whatsApp"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM" , "token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.notification != null){
            Log.d("FCM" , "token: ${message.notification?.body}")
        }
        Log.d("Firebase Messaging", "Message Received")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)

        val imageUrl = message.data["imgUrl"]
        val image = getBitmapFromUrl(imageUrl.toString())

        val intent1 = PendingIntent.getActivities(this, 0,
        arrayOf(intent), PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.whatsapp_logo)
            .setLargeIcon(image)
            .setAutoCancel(true)
            .setContentIntent(intent1)
            .build()

        manager.notify(Random.nextInt(), notification)
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
        val channel = NotificationChannel(channelId, "whatsAppChat", NotificationManager.IMPORTANCE_HIGH)

        channel.apply{
            description = "Chat"
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }
}