package com.example.whatsappclone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.whatsappclone.adapter.MessageAdapter
import com.example.whatsappclone.databinding.ActivityChatBinding
import com.example.whatsappclone.model.MessageModel
import com.example.whatsappclone.model.NotificationModel
import com.example.whatsappclone.model.PushNotification
import com.example.whatsappclone.model.UserModel
import com.example.whatsappclone.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var list: ArrayList<MessageModel>
    private lateinit var receiverUserName: String
    private lateinit var receiverUserImage: String
    private lateinit var userName: String
    private lateinit var userImage: String
    private lateinit var msg: String

    override fun onCreate(savedInstanceState: Bundle?) {

        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the shared element transition for the profileImageView and userName
        val profileImageView = binding.cardView2
        ViewCompat.setTransitionName(profileImageView, "cardView2")
        val usernameTextView = binding.itemUserName
        ViewCompat.setTransitionName(usernameTextView, "itemUserName")

        list = ArrayList()

        senderUid = FirebaseAuth.getInstance().uid.toString()
        receiverUid = intent.getStringExtra("uid")!!

        userProfileSetUp()

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        database = FirebaseDatabase.getInstance()

        binding.imageView2.setOnClickListener {
            if(binding.messageBox.text.isEmpty()){
                binding.messageBox.error = "Please enter your message"
            }else{
                msg = binding.messageBox.text.toString()
                val message = MessageModel(binding.messageBox.text.toString(), senderUid, Date().time)
                val randomKey = database.reference.push().key

                database.reference.child("chats")
                    .child(senderRoom).child("message")
                    .child(randomKey!!).setValue(message).addOnSuccessListener {

                        database.reference.child("chats")
                            .child(receiverRoom).child("message")
                            .child(randomKey).setValue(message).addOnSuccessListener{
                                Toast.makeText(this, "Message sent!!", Toast.LENGTH_SHORT).show()
                                sendNotification(binding.messageBox.text.toString())
                                binding.messageBox.text = null
                            }
                    }
            }
        }

        database.reference.child("chats")
            .child(senderRoom).child("message")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()

                    for(snapshot1 in snapshot.children){
                        val data = snapshot1.getValue(MessageModel::class.java)
                        list.add(data!!)
                    }
                    binding.recyclerView.adapter =  MessageAdapter(this@ChatActivity, list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }

            })
    }
    private fun sendNotification(msg: String) {

        database.reference.child("users")
            .child(senderUid).addListenerForSingleValueEvent(
                object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val data = snapshot.getValue(UserModel::class.java)
                            userName = data?.name.toString()
                            userImage = data?.imageUrl.toString()
                            val notificationData = PushNotification(NotificationModel(
                                userName, msg,
                                userImage
                            ), intent.getStringExtra("fmcToken"))

                            val firebaseApi = ApiClient.getInstance()
                            lifecycleScope.launch {
                                try {
                                    val response = firebaseApi.sendRemoteMessage(notificationData)
                                    if (response.isSuccessful){
                                        Toast.makeText(this@ChatActivity, "status: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(
                                            this@ChatActivity,
                                            "${response.errorBody().toString()} ${response.code()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("response", "${response.errorBody()} + ${response.code()}")
                                    }
                                }catch (exception: Exception){
                                    Log.e("Error", exception.message.toString())
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )

//        database.reference.child("users")
//            .child(receiverUid).addListenerForSingleValueEvent(
//                object: ValueEventListener{
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if(snapshot.exists()){
//                            val data = snapshot.getValue(UserModel::class.java)
//                            val notificationData = PushNotification(NotificationModel(
//                                userName, msg,
//                                userImage
//                            ), data?.fcmToken)
//
//                            val firebaseApi = ApiClient.getInstance()
//                            lifecycleScope.launch {
//                                try {
//                                    val response = firebaseApi.sendRemoteMessage(notificationData)
//                                    if (response.isSuccessful){
//                                        Toast.makeText(this@ChatActivity, "status: ${response.code()}", Toast.LENGTH_SHORT).show()
//                                    }else{
//                                        Toast.makeText(
//                                            this@ChatActivity,
//                                            "${response.errorBody().toString()} ${response.code()}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        Log.d("response", "${response.errorBody()} + ${response.code()}")
//                                    }
//                                }catch (exception: Exception){
//                                    Log.e("Error", exception.message.toString())
//                                }
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
//                    }
//
//                }
//            )
    }

    private fun userProfileSetUp() {
        receiverUserName = intent.getStringExtra("userName")!!
        receiverUserImage = intent.getStringExtra("userProfileImage")!!
        binding.itemUserName.text = receiverUserName
        Glide.with(this).load(receiverUserImage).into(binding.userImage)
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }
}