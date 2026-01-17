package com.hitanshudhawan.annotationprocessingexample;

import com.google.gson.annotations.SerializedName;
import com.hitanshudhawan.networkmodel.NetworkModel;

@NetworkModel
public class UserNameJava {

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    public UserNameJava(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
