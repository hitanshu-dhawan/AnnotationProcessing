package com.hitanshudhawan.annotationprocessingexample.network

class NetworkResponse {

    fun <T_Success> getSuccessResponse(classOfSuccess: Class<T_Success>): T_Success? {
        return null
    }

    fun <T_Success> getResponse(classOfSuccess: java.lang.reflect.Type): T_Success? {
        return null
    }

    inline fun <reified T_Success> getSuccessResponse(): T_Success? {
        return null
    }

    inline fun <reified T_Error> getErrorResponse(): T_Error? {
        return null
    }

    fun <T_Error> getErrorResponse(classOfError: Class<T_Error>): T_Error? {
        return null
    }

    fun <T_Error> getErrorResponseGeneric(classOfError: Class<T_Error>): T_Error? {
        return null
    }

}
