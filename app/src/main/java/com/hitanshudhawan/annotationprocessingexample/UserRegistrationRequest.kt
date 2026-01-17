package com.hitanshudhawan.annotationprocessingexample

import com.google.gson.annotations.SerializedName
import com.hitanshudhawan.networkmodel.NetworkModel
import kotlin.jvm.Transient

@NetworkModel
data class UserRegistrationRequest(

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("email_address")
    val email: String,

    // Nullable type: The API might accept null for age
    @SerializedName("age")
    val age: Int?,

    // Default value: If the JSON doesn't include this key, it defaults to false
    @Transient
    val isAdmin: Boolean = false

)
