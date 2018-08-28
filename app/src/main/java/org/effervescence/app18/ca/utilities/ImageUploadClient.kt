package org.effervescence.app18.ca.utilities

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ImageUploadClient {

    @Multipart
    @POST("file_upload/")
    fun uploadImage(@Header(Constants.AUTHORIZATION_KEY) token: String,
                    @Part(Constants.EVENT_ID_KEY) eventId: RequestBody,
                    @Part("file") file: RequestBody): Call<ResponseBody>

}