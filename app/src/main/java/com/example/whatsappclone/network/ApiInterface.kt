package com.example.whatsappclone.network

import com.example.whatsappclone.model.PushNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
    @Headers("Content-Type:application/json",
        "Authorization:key=AAAA0TrC7xQ:APA91bH9IVdBylJPWsFVAP5fpGjX2kMX2wXbUGZPUMuRthcXOzYih8jVMOiyDzS3r2L4mY5wasnThx6dEPsD6dLoGUR00jb-R7N6HXEC3BWIfMoncoiPKixt1heIuvqKil4e8T-1OIPc")
    @POST("send")
    suspend fun sendRemoteMessage(@Body notification: PushNotification) : Response<PushNotification>
}