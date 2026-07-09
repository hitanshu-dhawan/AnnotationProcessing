package com.hitanshudhawan.annotationprocessingexample.network

import androidx.annotation.WorkerThread

class NetworkRequest() {

    inline fun <reified T_Success, reified T_Error> processAsync(callback: ResponseCallback<T_Success, T_Error>) {
        // ...
    }

    fun <T_Success, T_Error> processAsyncForJava(successClass: Class<T_Success>, errorClass: Class<T_Error>, callback: ResponseCallback<T_Success, T_Error>) {
        // ...
    }

    @WorkerThread
    fun processSyncForJava(): NetworkResponse {
        return NetworkResponse()
    }

    suspend fun processSync(): NetworkResponse {
        return NetworkResponse()
    }

}
