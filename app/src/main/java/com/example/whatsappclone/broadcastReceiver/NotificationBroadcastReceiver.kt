package com.example.whatsappclone.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.activity.meetingInvitation.IncomingInvitationActivity
import com.example.whatsappclone.service.CallNotificationForegroundService

class NotificationBroadcastReceiver: BroadcastReceiver() {

    companion object{
        private const val CALL_RESPONSE_ACTION_KEY = "call_response"
        private const val CALL_RECEIVE = "call_receive"
        private const val CALL_CANCEL = "call_cancel"
        private const val FCM_TOKEN = "fcmToken"
        private const val TYPE = "message"
        private const val REMOTE_MESSAGE_MEETING_ROOM = "meeting_room"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if(intent != null){
                val act = intent.getStringExtra(CALL_RESPONSE_ACTION_KEY)
                val data = intent.getStringExtra(FCM_TOKEN)
                val type = intent.getStringExtra(TYPE)
                val name = intent.getStringExtra("name")
                val image = intent.getStringExtra("image")
                val number = intent.getStringExtra("number")
                val meetingRoom = intent.getStringExtra(REMOTE_MESSAGE_MEETING_ROOM)
                if(act != null){
                    performClickAction(context, act, data, type, name, image, number, meetingRoom)
                }
                context.stopService(Intent(context, CallNotificationForegroundService::class.java))
            }
        }
    }

    private fun performClickAction(
        context: Context,
        act: String,
        token: String?,
        type: String?,
        name: String?,
        image: String?,
        number: String?,
        meetingRoom: String?
    ) {
        if (act == CALL_RECEIVE) {
            val intent = Intent(context, IncomingInvitationActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("image", image)
            intent.putExtra("number", number)
            intent.putExtra(REMOTE_MESSAGE_MEETING_ROOM, meetingRoom)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }else if(act == CALL_CANCEL){
            Log.d(CALL_CANCEL, "Call is cancelled")
            context.stopService(Intent(context, CallNotificationForegroundService::class.java))
        }
    }
}