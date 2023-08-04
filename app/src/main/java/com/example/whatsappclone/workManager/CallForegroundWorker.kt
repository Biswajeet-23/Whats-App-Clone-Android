package com.example.whatsappclone.workManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappclone.R
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.broadcastReceiver.NotificationBroadcastReceiver
import com.example.whatsappclone.service.CallNotificationForegroundService

class CallForegroundWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params){
    override suspend fun doWork(): Result {
        val data = inputData
        Log.d(TAG, "${data.getString(Constants.REMOTE_MESSAGE_MEETING_ROOM)}")
        val notificationIntent = Intent(applicationContext, CallNotificationForegroundService::class.java)
        notificationIntent.apply {
            putExtra(MESSAGE_TYPE, data.getString(MESSAGE_TYPE))
            putExtra(NAME, data.getString(NAME))
            putExtra(IMAGE, data.getString(IMAGE))
            putExtra(NUMBER, data.getString(NUMBER))
            putExtra(FCM_TOKEN, data.getString(FCM_TOKEN))
            putExtra(Constants.REMOTE_MESSAGE_MEETING_ROOM, data.getString(Constants.REMOTE_MESSAGE_MEETING_ROOM))
        }
        applicationContext.startForegroundService(notificationIntent)

        return Result.success()
    }

    companion object {
        const val TAG = "CallForegroundWorker"
        private const val MESSAGE_TYPE = "type"
        private const val NAME = "name"
        private const val IMAGE = "image"
        private const val NUMBER = "number"
        private const val FCM_TOKEN = "fcmToken"
        private const val CHANNEL_ID = "call_channel_id"
    }
}