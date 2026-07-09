package com.hitanshudhawan.annotationprocessingexample.models

import com.google.gson.annotations.SerializedName
import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel

/**
 * Sample login request model in Kotlin.
 * Demonstrates proper usage of @NetworkModel and @SerializedName annotations.
 */
@NetworkModel
data class LoginRequest(

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("remember_me")
    val rememberMe: Boolean = false

)
