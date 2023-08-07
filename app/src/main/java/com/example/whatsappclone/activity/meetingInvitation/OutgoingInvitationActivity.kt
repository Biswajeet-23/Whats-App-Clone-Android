package com.example.whatsappclone.activity.meetingInvitation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.Utils.Constants
import com.example.whatsappclone.databinding.ActivityOutgoingInvitationBinding
import com.example.whatsappclone.model.NotificationModel
import com.example.whatsappclone.model.PushNotification
import com.example.whatsappclone.model.UserModel
import com.example.whatsappclone.network.ApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import timber.log.Timber
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.net.URL
import java.util.UUID

class OutgoingInvitationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOutgoingInvitationBinding
    private lateinit var senderUid: String
    private lateinit var database: FirebaseDatabase
    private var receiverToken: String? =""
    private lateinit var localBroadcastReceiver: BroadcastReceiver
    private lateinit var meetingRoom: String
    private lateinit var meetingType: String
    companion object{
        private const val TAG = "OutgoingInvitationActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOutgoingInvitationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        meetingType = intent.getStringExtra("meetingType")!!
        val groupVideoCall = intent.getBooleanExtra("isMultiple", false)
        val receiverImage = intent.getStringExtra("image")

        database = FirebaseDatabase.getInstance()
        senderUid = FirebaseAuth.getInstance().uid.toString()
        meetingRoom = senderUid

        if(meetingType.isNotEmpty()){
            if((meetingType == "video") && groupVideoCall){
                val turnsType = object : TypeToken<ArrayList<UserModel>>(){}.type
                val receivers = Gson().fromJson<ArrayList<UserModel>>(intent.getStringExtra("selected_users"), turnsType)
                binding.callImg.setImageResource(R.drawable.baseline_videocam_24)
                val userName = StringBuilder()
                for(i in receivers.indices){
                    userName.append(receivers[i].name).append("\n")
                }
                binding.callTV.text = "Video meetings with $userName"
                binding.userName.text = userName
                sendRemoteMessageInvitation(receivers)
            }else{
                val receiverName = intent.getStringExtra("name")
                receiverToken = intent.getStringExtra("token")!!
                if (meetingType == "video") {
                    binding.callImg.setImageResource(R.drawable.baseline_videocam_24)
                    binding.callTV.text = "Video meeting with $receiverName"
                    binding.userName.text = receiverName
                    sendRemoteMessageInvitation(null)
                } else if (meetingType == "audio") {
                    binding.callImg.setImageResource(R.drawable.baseline_call_24)
                    binding.callTV.text = "Audio meeting with $receiverName"
                    binding.userName.text = receiverName
                    sendRemoteMessageInvitation(null)
                }
            }
        }

        if (receiverImage != null){
            Glide.with(this).load(receiverImage).into(binding.callImg)
        }else if(groupVideoCall){
            Glide.with(this).load(R.drawable.baseline_group_24).into(binding.callImg)
        }

        // Initialize the local broadcast receiver
        localBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Handle the broadcast message here
                if (intent?.action == Constants.LOCAL_BROADCAST_OUTGOING_MESSAGE_RESPONSE) {
                    val responseType = intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
                    if(responseType != null){
                        if(responseType == Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED){
                            runOnUiThread {
                                Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show()
                                Timber.tag("BroadCast").d("Invitation Accepted")
                                try {
                                    Timber.tag("BroadCast").d("Meeting_room: $meetingRoom")
                                    val serverURL = URL("https://meet.jit.si")
                                    val conferenceOptions = JitsiMeetConferenceOptions.Builder()
                                        .setServerURL(serverURL)
                                        .setRoom(meetingRoom)
                                    if(meetingType == "audio"){
                                        conferenceOptions.setVideoMuted(true)
                                    }
                                    JitsiMeetActivity.launch(this@OutgoingInvitationActivity, conferenceOptions.build())
                                    finish()
                                }catch (e: Exception){
                                    Timber.tag("Broadcast").d("${e.printStackTrace()}")
                                    finish()
                                }
                            }
                        }
                        else if(responseType == Constants.REMOTE_MESSAGE_INVITATION_REJECTED){
                            Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                            Timber.tag("BroadCast").d("Invitation Rejected")
                            finish()
                        }
                    }
                }
            }
        }

        // Register the local broadcast receiver with the LocalBroadcastManager
        val filter = IntentFilter(Constants.LOCAL_BROADCAST_OUTGOING_MESSAGE_RESPONSE)
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter)

        binding.cancel.setOnClickListener {
            cancelRemoteMessageInvitation(Constants.REMOTE_MESSAGE_INVITATION_CANCELLED)
        }

    }

    private fun cancelRemoteMessageInvitation(invitationResponse: String){
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
                            sendVideoNotification(notificationData)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OutgoingInvitationActivity, error.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )
    }

    private fun sendRemoteMessageInvitation(receivers: ArrayList<UserModel>?){
        database.reference.child("users")
            .child(senderUid).addListenerForSingleValueEvent(
                object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val data = snapshot.getValue(UserModel::class.java)
                            val userName = data?.name.toString()
                            val userImage = data?.imageUrl.toString()
                            val userNo = data?.number.toString()
                            val senderFcmToken = data?.fcmToken.toString()
                            val meetingRoom = senderUid

                            if(receivers != null){
                                for(i in receivers.indices){
                                    val notificationData = PushNotification(
                                        NotificationModel(
                                            Constants.TITLE,
                                            meetingType,
                                            userImage,
                                            userName,
                                            userNo,
                                            senderFcmToken,
                                            "",
                                            meetingRoom
                                        ), receivers[i].fcmToken)
                                    sendVideoNotification(notificationData)
                                }
                            }
                            //model for sending the invitation
                            val notificationData = PushNotification(
                                NotificationModel(
                                    Constants.TITLE,
                                    meetingType,
                                    userImage,
                                    userName,
                                    userNo,
                                    senderFcmToken,
                                    "",
                                    meetingRoom
                                ), receiverToken)
                            sendVideoNotification(notificationData)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OutgoingInvitationActivity, error.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )
    }
    private fun sendVideoNotification(notificationData: PushNotification) {
        val firebaseApi = ApiClient.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = firebaseApi.sendRemoteMessage(notificationData)
                if (response.isSuccessful){
                    if(notificationData.data.invitationResponse == Constants.REMOTE_MESSAGE_INVITATION_CANCELLED){
                        runOnUiThread{
                            Toast.makeText(
                                this@OutgoingInvitationActivity,
                                "status: ${response.code()} Network Request Sent Successfully and Invitation is cancelled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d(TAG, "status: ${response.code()}, Network Request Sent Successfully and Invitation is cancelled")
                        finish()
                    }
                    runOnUiThread{
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "status: ${response.code()} Network Request Sent Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(TAG, "status: ${response.code()}, Network Request Sent Successfully")
                }else{
                    runOnUiThread{
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "${response.errorBody().toString()}, ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(TAG, "${response.code()}, ${response.errorBody()}")
                    finish()
                }
            }catch (exception: Exception){
                Log.e(TAG, exception.message.toString())
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the local broadcast receiver when the activity is destroyed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
    }
}