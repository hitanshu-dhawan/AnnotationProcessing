package com.hitanshudhawan.annotationprocessingexample.network

enum class RequestPriority {

    /**
     * P0 (Critical)
     * Must never be dropped; directly involved in transaction or authentication paths of phonepe app.
     * Examples: Payment initiation, Transaction-related APIs, Login on share.market, Refresh token
     */
    P0,

    /**
     * P1 (High)
     * Onboarding-critical but not directly in the transaction path. Can tolerate minor delays during spikes.
     * Examples: User registration, Login, Device verification, Address management
     */
    P1,

    /**
     * P2 (Medium)
     * Operational or internal flows with user relevance but not time-critical.
     * Examples: User state management, CS console APIs
     */
    P2,

    /**
     * P3 (Low)
     * Non-critical background or enhancement flows.
     * Examples: Device API's
     */
    P3

}
