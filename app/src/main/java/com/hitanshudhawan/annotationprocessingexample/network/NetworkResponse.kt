package com.hitanshudhawan.annotationprocessingexample.network

class NetworkResponse() {

    fun <T_Success> getSuccessResponse(classOfSuccess: Class<T_Success>): T_Success? {
        return null
    }

    inline fun <reified T_Success> getSuccessResponse(): T_Success? {
        return null
    }

}
