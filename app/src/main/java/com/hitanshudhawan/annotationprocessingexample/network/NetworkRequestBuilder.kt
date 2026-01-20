package com.hitanshudhawan.annotationprocessingexample.network

import kotlin.String

class NetworkRequestBuilder() {

    fun subUrl(subUrl: String): NetworkRequestBuilder {
        return this
    }

    fun httpMethod(httpRequestType: String): NetworkRequestBuilder {
        return this
    }

    fun <T_Request> body(requestBody: T_Request): NetworkRequestBuilder {
        return this
    }

    fun build(): NetworkRequest {
        return NetworkRequest()
    }

}
