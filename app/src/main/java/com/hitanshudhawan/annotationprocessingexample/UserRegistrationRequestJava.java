package com.hitanshudhawan.annotationprocessingexample;

import com.google.gson.annotations.SerializedName;
import com.hitanshudhawan.networkmodelvalidator.NetworkModel;

@NetworkModel
public class UserRegistrationRequestJava {

    @SerializedName("user_name")
    public UserNameJava userName;

    @SerializedName("email_address")
    public String email;

    @SerializedName("age")
    public Integer age;

    public transient boolean isAdmin; // transient keyword in Java corresponds to @Transient in Kotlin/JVM

    public UserRegistrationRequestJava(UserNameJava userName, String email, Integer age, boolean isAdmin) {
        this.userName = userName;
        this.email = email;
        this.age = age;
        this.isAdmin = isAdmin;
    }
}
