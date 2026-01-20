package com.hitanshudhawan.annotationprocessingexample

import com.google.gson.annotations.SerializedName
import com.hitanshudhawan.networkmodel.NetworkModel

@NetworkModel
data class UserRegistrationResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("user_id")
    val userId: String?

)
