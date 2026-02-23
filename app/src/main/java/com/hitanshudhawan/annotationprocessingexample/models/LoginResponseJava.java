package com.hitanshudhawan.annotationprocessingexample.models;

import com.google.gson.annotations.SerializedName;
import com.hitanshudhawan.networkmodelvalidator.NetworkModel;

/**
 * Sample login response model in Java.
 * Demonstrates proper usage of @NetworkModel and @SerializedName annotations.
 */
@NetworkModel
public class LoginResponseJava {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserDataJava user;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private long expiresIn;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserDataJava getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Nested user data model.
     * Must also be annotated with @NetworkModel.
     */
    @NetworkModel
    public static class UserDataJava {

        @SerializedName("user_id")
        private String userId;

        @SerializedName("email")
        private String email;

        @SerializedName("display_name")
        private String displayName;

        @SerializedName("avatar_url")
        private String avatarUrl;

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

    }

}
