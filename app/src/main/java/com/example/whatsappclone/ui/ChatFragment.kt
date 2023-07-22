package com.example.whatsappclone.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsappclone.R
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.databinding.FragmentChatBinding
import com.example.whatsappclone.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding?=null
    private val binding get() = _binding
    private var database: FirebaseDatabase? =null
    lateinit var userList: ArrayList<UserModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        userList = ArrayList()

        database!!.reference.child("users")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    for(snapshot1 in snapshot.children){
                        val user = snapshot1.getValue(UserModel::class.java)
                        if(user!!.uid != FirebaseAuth.getInstance().uid){
                            userList.add(user)
                        }
                    }
                    binding?.userListRecyclerView?.adapter = ChatAdapter(requireContext(), userList)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return binding?.root
    }
}