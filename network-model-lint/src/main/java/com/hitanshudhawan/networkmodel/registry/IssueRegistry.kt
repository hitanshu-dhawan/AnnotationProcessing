package com.hitanshudhawan.networkmodel.registry

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.hitanshudhawan.networkmodel.MyLogDetector

class IssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(MyLogDetector.ISSUE)

    override val api: Int = CURRENT_API

}
