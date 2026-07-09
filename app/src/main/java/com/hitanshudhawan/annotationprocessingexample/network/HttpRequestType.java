package com.hitanshudhawan.annotationprocessingexample.network;

import androidx.annotation.NonNull;

public enum HttpRequestType {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    PATCH("PATCH"),
    UNKNOWN("UNKNOWN");

    private final String requestType;

    HttpRequestType(String type) {
        this.requestType = type;
    }

    public String getHttpRequestType() {
        return requestType;
    }

    @NonNull
    public static HttpRequestType from(String val) {
        for (HttpRequestType type : HttpRequestType.values()) {
            if (type.getHttpRequestType().equals(val)) {
                return type;
            }
        }
        return HttpRequestType.UNKNOWN;
    }

}
