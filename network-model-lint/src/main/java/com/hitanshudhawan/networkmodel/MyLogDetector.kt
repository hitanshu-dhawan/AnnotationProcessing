package com.hitanshudhawan.networkmodel

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class MyLogDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> {
        return listOf("v", "d", "i", "w", "e")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, "android.util.Log")) {
            reportUsage(context, node, method)
        }
    }

    private fun reportUsage(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = true,
                includeArguments = true
            ),
            message = "Usage of android Log is prohibited",
            quickfixData = fix()
                .name("Use MyLog.${method.name}()")
                .replace()
                .text("Log")
                .with("com.hitanshudhawan.library.MyLog")
                .shortenNames()
                .reformat(true)
                .autoFix() // .robot(true).independent(true)
                .build()
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "LogUsageWarning",
            briefDescription = "The android Log should not be used",
            explanation = "The android Log should not be used, use MyLog instead.",
            category = Category.CORRECTNESS,
            priority = 3,
            severity = Severity.FATAL,
            implementation = Implementation(MyLogDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )/*.setAndroidSpecific(true)*/
    }

}
