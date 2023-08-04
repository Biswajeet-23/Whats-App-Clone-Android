package com.example.whatsappclone.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.activity.ChatActivity
import com.example.whatsappclone.databinding.ChatUserItemLayoutBinding
import com.example.whatsappclone.model.UserModel
import com.google.android.material.card.MaterialCardView
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(var context: Context, var list: ArrayList<UserModel>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var binding : ChatUserItemLayoutBinding = ChatUserItemLayoutBinding.bind(view)
        val profileImageView: MaterialCardView = view.findViewById(R.id.cardView2)
        val usernameTextView: TextView = view.findViewById(R.id.itemUserName)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_user_item_layout, parent, false))
    }
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = list[position]
        Glide.with(context).load(user.imageUrl).into(holder.binding.userImage)
        holder.binding.itemUserName.text = user.name
        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("userProfileImage", user.imageUrl)
            intent.putExtra("userName", user.name)
            intent.putExtra("fmcToken", user.fcmToken)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                context as Activity,
                androidx.core.util.Pair(holder.profileImageView, "cardView2"),
                androidx.core.util.Pair(holder.usernameTextView, "itemUserName")
            )
            context.startActivity(intent, options.toBundle())
        }
    }
}