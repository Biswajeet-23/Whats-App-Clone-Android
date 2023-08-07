package com.example.whatsappclone.listener

import com.example.whatsappclone.model.UserModel

interface UserListener {
    fun initiateVideoMeeting(user: UserModel)
    fun initiateAudioMeeting(user: UserModel)

    fun onMultipleUsersAction(isMultipleUsersSelected: Boolean)
}