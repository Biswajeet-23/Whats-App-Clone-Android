package com.example.whatsappclone.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.whatsappclone.R
import com.example.whatsappclone.activity.meetingInvitation.OutgoingInvitationActivity
import com.example.whatsappclone.adapter.CallAdapter
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.databinding.FragmentCallBinding
import com.example.whatsappclone.databinding.FragmentChatBinding
import com.example.whatsappclone.listener.UserListener
import com.example.whatsappclone.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CallFragment : Fragment(), UserListener {

    private var _binding: FragmentCallBinding?=null
    private val binding get() = _binding
    private var database: FirebaseDatabase? =null
    lateinit var userList: ArrayList<UserModel>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCallBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        userList = ArrayList()

        database!!.reference.child("users")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    for(snapshot1 in snapshot.children){
                        val user = snapshot1.getValue(UserModel::class.java)
                        if(user!!.uid != FirebaseAuth.getInstance().uid){
                            userList.add(user)
                        }
                    }
                    binding?.callListRecyclerView?.adapter = CallAdapter(requireContext(), userList, this@CallFragment)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return binding?.root
    }

    override fun initiateVideoMeeting(user: UserModel) {
        if(user.fcmToken == null || user.fcmToken.trim().isEmpty()){
            Toast.makeText(requireContext(), user.name + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            val intent = Intent(requireContext(), OutgoingInvitationActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("token", user.fcmToken)
            intent.putExtra("image", user.imageUrl)
            intent.putExtra("number", user.number)
            intent.putExtra("id", user.uid)
            intent.putExtra("meetingType", "video")
            startActivity(intent)
        }
    }

    override fun initiateAudioMeeting(user: UserModel) {
        if(user.fcmToken == null || user.fcmToken.trim().isEmpty()){
            Toast.makeText(requireContext(), user.name + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            val intent = Intent(requireContext(), OutgoingInvitationActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("token", user.fcmToken)
            intent.putExtra("image", user.imageUrl)
            intent.putExtra("number", user.number)
            intent.putExtra("id", user.uid)
            intent.putExtra("meetingType", "audio")
            startActivity(intent)
        }
    }
}