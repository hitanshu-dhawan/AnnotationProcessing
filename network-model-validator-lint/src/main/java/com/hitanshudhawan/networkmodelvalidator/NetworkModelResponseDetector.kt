package com.hitanshudhawan.networkmodelvalidator

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class NetworkModelResponseDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> {
        return listOf("body")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, "com.hitanshudhawan.annotationprocessingexample.network.NetworkRequestBuilder")) {
            val argument = node.valueArguments.firstOrNull()
            val type = argument?.getExpressionType()
            val psiClass = (type as? PsiClassType)?.resolve()
            if (psiClass != null && !psiClass.hasAnnotation("com.hitanshudhawan.networkmodel.NetworkModel")) {
                reportUsage(context, node, method)
            }
        }
    }

    private fun reportUsage(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = false,
                includeArguments = true
            ),
            message = "The request body must be a class annotated with @NetworkModel.",
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "NetworkModelRequest",
            briefDescription = "Network request body missing @NetworkModel",
            explanation = "Arguments passed to NetworkRequestBuilder.body() must be classes annotated with @NetworkModel.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(NetworkModelRequestDetector::class.java, Scope.JAVA_FILE_SCOPE)
        ).setAndroidSpecific(true)
    }

}
