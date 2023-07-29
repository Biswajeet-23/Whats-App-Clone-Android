package com.example.whatsappclone.model

data class PushNotification (
    val data: NotificationModel,
    val to: String? = "",
)