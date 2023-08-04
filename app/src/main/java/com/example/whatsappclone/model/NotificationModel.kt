package com.example.whatsappclone.model

data class NotificationModel (
    val title: String?= "",
    val message: String?= "",
    val imgUrl: String?= "",
    val senderName: String?="",
    val senderNumber: String?="",
    val fcmToken: String?="",
    val invitationResponse: String?="",
    val meetingRoom: String?=""
    )