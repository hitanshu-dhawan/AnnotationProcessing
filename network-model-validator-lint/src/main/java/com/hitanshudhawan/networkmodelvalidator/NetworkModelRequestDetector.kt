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

/**
 * Lint detector that ensures the body argument passed to `NetworkRequestBuilder.body()`
 * is a class annotated with @NetworkModel.
 *
 * This helps enforce type safety for network request models by ensuring all request
 * body types have proper validation annotations.
 */
class NetworkModelRequestDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val NETWORK_MODEL_ANNOTATION = "com.hitanshudhawan.networkmodelvalidator.NetworkModel"
        private const val NETWORK_REQUEST_BUILDER_CLASS = "com.hitanshudhawan.annotationprocessingexample.network.NetworkRequestBuilder"

        val ISSUE = Issue.create(
            id = "NetworkModelRequest",
            briefDescription = "Network request body missing @NetworkModel",
            explanation = "Arguments passed to NetworkRequestBuilder.body() must be classes annotated with @NetworkModel. " +
                    "This ensures that all network request models are properly validated.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(NetworkModelRequestDetector::class.java, Scope.JAVA_FILE_SCOPE)
        ).setAndroidSpecific(true)
    }

    override fun getApplicableMethodNames(): List<String> {
        return listOf("body")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator

        // Check if the method is called on NetworkRequestBuilder class
        if (!evaluator.isMemberInClass(method, NETWORK_REQUEST_BUILDER_CLASS)) {
            return
        }

        // Get the first argument (the body)
        val argument = node.valueArguments.firstOrNull()
        val type = argument?.getExpressionType()
        val psiClass = (type as? PsiClassType)?.resolve()

        // Check if the argument type has the @NetworkModel annotation
        if (psiClass != null && !psiClass.hasAnnotation(NETWORK_MODEL_ANNOTATION)) {
            reportUsage(context, node)
        }
    }

    private fun reportUsage(context: JavaContext, node: UCallExpression) {
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
}
