package com.hitanshudhawan.networkmodelvalidator.registry

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.hitanshudhawan.networkmodelvalidator.NetworkModelRequestDetector

class IssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(
            NetworkModelRequestDetector.ISSUE,
//            NetworkModelResponseDetector.ISSUE,
        )

    override val api: Int = CURRENT_API

}
