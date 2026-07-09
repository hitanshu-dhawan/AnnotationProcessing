package com.hitanshudhawan.annotationprocessingexample.models;

import com.google.gson.annotations.SerializedName;
import com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel;

/**
 * Sample login request model in Java.
 * Demonstrates proper usage of @NetworkModel and @SerializedName annotations.
 */
@NetworkModel
public class LoginRequestJava {

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    @SerializedName("remember_me")
    private final boolean rememberMe;

    public LoginRequestJava(String email, String password, boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

}
