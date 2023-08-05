package com.example.whatsappclone.activity.meetingInvitation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.databinding.ActivityIncomingInvitationBinding
import com.example.whatsappclone.model.NotificationModel
import com.example.whatsappclone.model.PushNotification
import com.example.whatsappclone.model.UserModel
import com.example.whatsappclone.network.ApiClient
import com.example.whatsappclone.service.CallNotificationForegroundService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import timber.log.Timber
import java.net.URL

class IncomingInvitationActivity : AppCompatActivity() {

    private lateinit var senderUid: String
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityIncomingInvitationBinding
    private lateinit var localBroadcastReceiver: BroadcastReceiver
    private lateinit var meetingType: String
    companion object{
        private const val TAG = "IncomingInvitationActivity"
        private const val REMOTE_MESSAGE_MEETING_ROOM = "meeting_room"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        senderUid = FirebaseAuth.getInstance().uid.toString()

        Timber.tag(TAG).d("call UI")

        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")
        val number = intent.getStringExtra("number")
        val meetingRoom = intent.getStringExtra(REMOTE_MESSAGE_MEETING_ROOM)!!
        val receiverToken = intent.getStringExtra("fcmToken")!!
        meetingType = intent.getStringExtra("message")!!

        Timber.tag(TAG).d("$name")

        binding.callUserName.text = "$name"
        Glide.with(this).load(image).into(binding.callUserImage)

        stopService(Intent(this, CallNotificationForegroundService::class.java))

        binding.accept.setOnClickListener {
            sendRemoteMessageInvitation(Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED, receiverToken, meetingRoom)
        }
        binding.cancel.setOnClickListener {
            sendRemoteMessageInvitation(Constants.REMOTE_MESSAGE_INVITATION_REJECTED, receiverToken, null)
        }

        localBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Handle the broadcast message here
                if (intent?.action == Constants.LOCAL_BROADCAST_INCOMING_MESSAGE_RESPONSE) {
                    val responseType = intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
                    if(responseType != null){
                        if(responseType == Constants.REMOTE_MESSAGE_INVITATION_CANCELLED){
                            Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show()
                            Timber.tag("BroadCast").d("Invitation Cancelled")
                            finish()
                        }
                    }
                }
            }
        }

        // Register the local broadcast receiver with the LocalBroadcastManager
        val filter = IntentFilter(Constants.LOCAL_BROADCAST_INCOMING_MESSAGE_RESPONSE)
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter)

    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the local broadcast receiver when the activity is destroyed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
    }

    private fun sendRemoteMessageInvitation(invitationResponse: String, receiverToken: String, meetingRoom: String?){
        database.reference.child("users")
            .child(senderUid).addListenerForSingleValueEvent(
                object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val data = snapshot.getValue(UserModel::class.java)
                            val userName = data?.name.toString()
                            val userImage = data?.imageUrl.toString()
                            val userNo = data?.number.toString()
                            val senderFcmToken = data?.fcmToken.toString()
                            //model for sending the invitation

                            val notificationData = PushNotification(
                                NotificationModel(
                                    Constants.TITLE,
                                    Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,
                                    userImage,
                                    userName,
                                    userNo,
                                    senderFcmToken,
                                    invitationResponse
                                ), receiverToken)
                            sendVideoNotification(notificationData, meetingRoom)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@IncomingInvitationActivity, error.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )
    }

    private fun sendVideoNotification(notificationData: PushNotification, meetingRoom: String?) {
        val firebaseApi = ApiClient.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = firebaseApi.sendRemoteMessage(notificationData)
                if (response.isSuccessful){
                    if (notificationData.data.invitationResponse == Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED){
                        try{
                            if(meetingRoom != null){
                                Timber.tag(TAG).d("$meetingRoom")
                                val serverURL = URL("https://meet.jit.si")
                                val conferenceOptions = JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setRoom(meetingRoom)
                                if(meetingType == "audio"){
                                    conferenceOptions.setVideoMuted(true)
                                }
                                JitsiMeetActivity.launch(this@IncomingInvitationActivity, conferenceOptions.build())
                                finish()
                            }else{
                                runOnUiThread{
                                    Toast.makeText(
                                        this@IncomingInvitationActivity,
                                        "MEETING_ROOM: It is null",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }catch (e: Exception){
                            Timber.tag(TAG).d("${e.printStackTrace()}")
                            finish()
                        }

                        Log.d(TAG, "RESPONSE: ${response.code()}, Network Request Sent Successfully")
                        Log.d(TAG, "RESPONSE: Invitation Accepted,")
                    }else if(notificationData.data.invitationResponse == Constants.REMOTE_MESSAGE_INVITATION_REJECTED){
                        runOnUiThread{
                            Toast.makeText(
                                this@IncomingInvitationActivity,
                                "RESPONSE: ${response.errorBody().toString()}, ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d(TAG, "RESPONSE: ${response.code()}, ${response.errorBody()}")
                        Log.d(TAG, "RESPONSE: Invitation Rejected,")
                        finish()
                    }
                }else{
                    runOnUiThread{
                        Toast.makeText(
                            this@IncomingInvitationActivity,
                            "RESPONSE: ${response.errorBody().toString()}, ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(TAG, "RESPONSE: ${response.code()}, ${response.errorBody()}")
                    finish()
                }
            }catch (exception: Exception){
                Log.e(TAG, exception.message.toString())
                finish()
            }
        }
    }
}