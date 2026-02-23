package com.hitanshudhawan.annotationprocessingexample.models

import com.google.gson.annotations.SerializedName
import com.hitanshudhawan.networkmodelvalidator.NetworkModel

/**
 * Sample login response model in Kotlin.
 * Demonstrates proper usage of @NetworkModel and @SerializedName annotations.
 */
@NetworkModel
data class LoginResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserData?,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("expires_in")
    val expiresIn: Long

)

/**
 * Nested user data model in Kotlin.
 * Must also be annotated with @NetworkModel.
 */
@NetworkModel
data class UserData(

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("avatar_url")
    val avatarUrl: String?

)
