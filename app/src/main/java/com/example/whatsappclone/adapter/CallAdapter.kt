package com.example.whatsappclone.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.activity.ChatActivity
import com.example.whatsappclone.databinding.CallUserItemLayoutBinding
import com.example.whatsappclone.listener.UserListener
import com.example.whatsappclone.model.UserModel
import com.google.android.material.card.MaterialCardView

class CallAdapter(var context: Context, var list: ArrayList<UserModel>, private var userlistener: UserListener) : RecyclerView.Adapter<CallAdapter.CallViewHolder>() {

    private val userListener: UserListener = userlistener
    private var selectedUsers: MutableList<UserModel> = mutableListOf()
    inner class CallViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var binding : CallUserItemLayoutBinding = CallUserItemLayoutBinding.bind(view)
        val profileImageView: MaterialCardView = view.findViewById(R.id.cardView2)
        val usernameTextView: TextView = view.findViewById(R.id.itemUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        return CallViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.call_user_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val user = list[position]
        Glide.with(context).load(user.imageUrl).into(holder.binding.userImage)
        holder.binding.itemUserName.text = user.name
        holder.binding.videoMeeting.setOnClickListener {
            userListener.initiateVideoMeeting(user)
        }
        holder.binding.audioMeeting.setOnClickListener {
            userListener.initiateAudioMeeting(user)
        }
        holder.binding.container.setOnLongClickListener{
            selectedUsers.add(user)
            holder.binding.selectUser.visibility = View.VISIBLE
            holder.binding.videoMeeting.visibility = View.GONE
            holder.binding.audioMeeting.visibility = View.GONE
            userListener.onMultipleUsersAction(true)
            true
        }
        holder.binding.container.setOnClickListener {
            if(holder.binding.selectUser.visibility == View.VISIBLE){
                selectedUsers.remove(user)
                holder.binding.selectUser.visibility = View.GONE
                holder.binding.videoMeeting.visibility = View.VISIBLE
                holder.binding.audioMeeting.visibility = View.VISIBLE
                if(selectedUsers.size == 0){
                    userListener.onMultipleUsersAction(false)
                }
            }else{
                if(selectedUsers.size > 0){
                    selectedUsers.add(user)
                    holder.binding.selectUser.visibility = View.VISIBLE
                    holder.binding.videoMeeting.visibility = View.GONE
                    holder.binding.audioMeeting.visibility = View.GONE
                }
            }
        }
    }

    public fun getSelectedUsers(): List<UserModel>{
        return selectedUsers
    }
}