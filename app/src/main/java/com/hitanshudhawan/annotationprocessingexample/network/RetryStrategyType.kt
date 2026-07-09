package com.hitanshudhawan.annotationprocessingexample.network

/**
 * Strategy to determine backoff time to wait before retrying a network request.
 */
enum class RetryStrategyType(val maxRetryCount: Int) {

    /**
     *No delay, retry immediately
     */
    NO_BACKOFF(5),

    /**
     * Constant delay, retry after fixed duration of 1s
     */
    CONSTANT_BACKOFF(5),

    /**
     * Linearly increase delay duration after each retry, for instance: 0,1,2,3,4,5
     */
    LINEAR_BACKOFF(5),

    /**
     * Exponentially increase delay duration after each retry, for instance 1,2,4,8,16,32
     */
    EXPONENTIAL_BACKOFF(5),

    /**
     * Polynomially increase delay duration after each retry, for instance 0,1,4,9,16,25
     */
    POLYNOMIAL_BACKOFF(4),

    UNKNOWN(0);


    companion object {
        fun from(retryStrategyValue: String?): RetryStrategyType {
            for (retryStrategyType in values()) {
                if (retryStrategyType.name == retryStrategyValue) {
                    return retryStrategyType
                }
            }
            return UNKNOWN
        }
    }

}
