package com.hitanshudhawan.annotationprocessingexample.network

import android.net.Uri
import com.google.gson.Gson
import java.util.*

class NetworkRequestBuilder() {

    fun addQueryParam(key: String, value: String?): NetworkRequestBuilder {
        return this
    }

    fun addQueryParam(key: String, value: List<String>?): NetworkRequestBuilder {
        return this
    }

    fun addQueryParam(key: String, value: Array<String>?): NetworkRequestBuilder {
        return this
    }

    fun queryParams(queryParams: HashMap<String, String>?): NetworkRequestBuilder {
        return this
    }

    fun <T_Request> body(requestBody: T_Request): NetworkRequestBuilder {
        return this
    }

    fun requestAnchor(requestAnchorName: String): NetworkRequestBuilder {
        return this
    }

    fun extras(extras: HashMap<String, String>): NetworkRequestBuilder {
        return this
    }

//    fun ksMeta(ksMeta: KillSwitchMeta): NetworkRequestBuilder {
//        return this
//    }

    fun mailBox(isMailBox: Boolean): NetworkRequestBuilder {
        return this
    }

//    fun subscribeOnBolt(topics: Set<Topic>): NetworkRequestBuilder {
//        return this
//    }

    fun mailBoxPollTimeout(timeout: Int): NetworkRequestBuilder {
        return this
    }

    fun filePath(filePath: String): NetworkRequestBuilder {
        return this
    }

    fun fileUri(fileUri: Uri): NetworkRequestBuilder {
        return this
    }

    fun formDataMap(formDataMap: HashMap<String, String>): NetworkRequestBuilder {
        return this
    }

    fun isMultipart(isMultipart: Boolean?): NetworkRequestBuilder {
        return this
    }

    fun rawBody(rawRequestBody: String): NetworkRequestBuilder {
        return this
    }

    fun subUrl(subUrl: String): NetworkRequestBuilder {
        return this
    }

    fun shouldDisableChecksum(disableCheckSum: Boolean): NetworkRequestBuilder {
        return this
    }

    fun addPathParam(key: String, value: String?): NetworkRequestBuilder {
        return this
    }

    fun pathParams(pathParams: HashMap<String, String>): NetworkRequestBuilder {
        return this
    }

    fun addHeader(key: String, value: String?): NetworkRequestBuilder {
        return this
    }

    fun headers(headers: Map<String, String>): NetworkRequestBuilder {
        return this
    }

    fun tokenRequired(isTokenRequired: Boolean): NetworkRequestBuilder {
        return this
    }

    fun customPlaceholderAuthToken(authToken: String?): NetworkRequestBuilder {
        return this
    }

    fun isTokenToBePassedInWhitelistedCall(isTokenToBePassedInWhitelistedCall: Boolean?): NetworkRequestBuilder {
        return this
    }

    fun shouldEnableResponseEncryption(shouldEnableResponseEncryption: Boolean): NetworkRequestBuilder {
        return this
    }

    fun timeout(timeout: Int): NetworkRequestBuilder {
        return this
    }

    fun connectTimeout(connectTimeout: Int): NetworkRequestBuilder {
        return this
    }

    fun readTimeout(readTimeout: Int): NetworkRequestBuilder {
        return this
    }

    fun writeTimeout(writeTimeout: Int): NetworkRequestBuilder {
        return this
    }

    fun retry(): NetworkRequestBuilder {
        return retryCount(3)
    }

    fun retryCount(retryCount: Int): NetworkRequestBuilder {
        return this
    }

    fun retryStrategy(retryStrategy: RetryStrategyType): NetworkRequestBuilder {
        return this
    }

    fun mediaType(mediaType: String): NetworkRequestBuilder {
        return this
    }

    fun contentType(contentType: String): NetworkRequestBuilder {
        return this
    }

    fun requestCode(requestCode: Int): NetworkRequestBuilder {
        return this
    }

    fun httpMethod(httpRequestType: HttpRequestType): NetworkRequestBuilder {
        return this
    }

    fun baseUrl(baseUrl: String): NetworkRequestBuilder {
        return this
    }

    fun encrypted(isEncrypted: Boolean): NetworkRequestBuilder {
        return this
    }

    fun priority(priorityLevel: PriorityLevel): NetworkRequestBuilder {
        return this
    }

    fun requestPriority(requestPriority: RequestPriority): NetworkRequestBuilder {
        return this
    }

//    fun setRequestType(requestType: GenericRestData.RequestType): NetworkRequestBuilder {
//        return this
//    }

    fun shouldEnableRequestCompression(shouldEnableRequestCompression: Boolean): NetworkRequestBuilder {
        return this
    }

    fun phonePeMultipartRequest(isPhonePeMultipartRequest: Boolean): NetworkRequestBuilder {
        return this
    }

//    fun orgId(orgId: Org): NetworkRequestBuilder {
//        return this
//    }

    fun gson(gson: Gson?): NetworkRequestBuilder {
        return this
    }

    fun build(): NetworkRequest {
        return NetworkRequest()
    }

}
