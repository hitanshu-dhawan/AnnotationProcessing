package com.hitanshudhawan.annotationprocessingexample;

import com.google.gson.annotations.SerializedName;
import com.hitanshudhawan.networkmodel.NetworkModel;

@NetworkModel
public class UserRegistrationRequestJava {

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("email_address")
    public String email;

    @SerializedName("age")
    public Integer age;

    public transient boolean isAdmin; // transient keyword in Java corresponds to @Transient in Kotlin/JVM

    public UserRegistrationRequestJava(String firstName, String lastName, String email, Integer age, boolean isAdmin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.isAdmin = isAdmin;
    }
}
