package com.hitanshudhawan.annotationprocessingexample

import com.hitanshudhawan.annotationprocessingexample.models.LoginRequest
import com.hitanshudhawan.annotationprocessingexample.models.LoginResponse
import com.hitanshudhawan.annotationprocessingexample.network.HttpRequestType
import com.hitanshudhawan.annotationprocessingexample.network.NetworkRequestBuilder
import com.hitanshudhawan.annotationprocessingexample.network.NetworkResponse

class LoginRepository {

    suspend fun login() {
        val loginRequest = LoginRequest(
            email = "user@example.com",
            password = "password123",
            rememberMe = true
        )

        val networkResponse: NetworkResponse = NetworkRequestBuilder()
            .httpMethod(HttpRequestType.POST)
            .subUrl("/api/v1/auth/login")
            .body(loginRequest)
            .build()
            .processSync()

        val successResponse1: LoginResponse? = networkResponse.getSuccessResponse<LoginResponse>()
        val successResponse2: LoginResponse? = networkResponse.getSuccessResponse(LoginResponse::class.java)
        val successResponse3: LoginResponse? = networkResponse.getResponse(LoginResponse::class.java)

        val errorResponse = networkResponse.getErrorResponse<String>()
    }

}
