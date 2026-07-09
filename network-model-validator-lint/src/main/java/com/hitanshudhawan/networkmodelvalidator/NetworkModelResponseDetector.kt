package com.hitanshudhawan.networkmodelvalidator

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiWildcardType
import org.jetbrains.uast.UCallExpression

/**
 * Lint detector that ensures the return type of `getSuccessResponse()` / `getResponse()` method calls
 * are classes annotated with @NetworkModel.
 *
 * This helps enforce type safety for network response models by ensuring all response
 * types have proper validation annotations.
 */
class NetworkModelResponseDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val NETWORK_MODEL_ANNOTATION = "com.hitanshudhawan.networkmodelvalidator.NetworkModel"
        private const val NETWORK_RESPONSE_CLASS = "com.hitanshudhawan.annotationprocessingexample.network.NetworkResponse"

        /**
         * Package prefixes for standard library types that should be skipped during validation.
         * These types cannot be annotated with @NetworkModel since they are part of the platform.
         */
        private val STANDARD_LIBRARY_PREFIXES = listOf(
            "java.",      // Java standard library (String, Integer, List, Map, etc.)
            "javax.",     // Java extensions (annotations, etc.)
            "kotlin.",    // Kotlin standard library
            "android."    // Android framework classes
        )

        val ISSUE = Issue.create(
            id = "NetworkModelResponse",
            briefDescription = "Network response type missing @NetworkModel",
            explanation = "The return type of getSuccessResponse() / getResponse() must be a class annotated with @NetworkModel. " +
                    "This ensures that all network response models are properly validated.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(NetworkModelResponseDetector::class.java, Scope.JAVA_FILE_SCOPE)
        ).setAndroidSpecific(true)
    }

    override fun getApplicableMethodNames(): List<String> {
        return listOf("getSuccessResponse", "getResponse")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator

        // Check if the method is called on NetworkResponse class
        // Try multiple approaches since library methods might not resolve correctly
        val isNetworkResponseMethod = evaluator.isMemberInClass(method, NETWORK_RESPONSE_CLASS) ||
                isReceiverNetworkResponse(node)

        if (!isNetworkResponseMethod) {
            return
        }

        // Collect all types to validate
        val typesToValidate = mutableSetOf<PsiType>()

        // Try to get explicit type arguments (e.g., getResponse<List<RechargeChangeResponse>>())
        node.typeArguments.forEach { psiType ->
            typesToValidate.add(psiType)
        }

        // Also get the return type of the method call as fallback/additional check
        node.getExpressionType()?.let { returnType ->
            typesToValidate.add(returnType)
        }

        // Validate all collected types
        typesToValidate.forEach { psiType ->
            validateType(context, node, psiType)
        }
    }

    /**
     * Checks if the receiver of the method call is a NetworkResponse type.
     * This is an alternative check when isMemberInClass doesn't work for library methods.
     */
    private fun isReceiverNetworkResponse(node: UCallExpression): Boolean {
        val receiverType = node.receiverType ?: return false
        val psiClassType = receiverType as? PsiClassType ?: return false
        val psiClass = psiClassType.resolve() ?: return false
        return psiClass.qualifiedName == NETWORK_RESPONSE_CLASS
    }

    // ============================================================
    // Type Validation
    // ============================================================

    /**
     * Validates any PsiType by dispatching to the appropriate handler.
     * Handles PsiClassType, PsiWildcardType, and other type variants.
     *
     * @param context The lint context
     * @param node The call expression node (for error reporting)
     * @param psiType The type to validate
     */
    private fun validateType(context: JavaContext, node: UCallExpression, psiType: PsiType) {
        when (psiType) {
            is PsiClassType -> validateDeclaredType(context, node, psiType)
            is PsiWildcardType -> {
                // Handle wildcard types like ? extends Foo or ? super Bar
                psiType.bound?.let { boundType ->
                    validateType(context, node, boundType)
                }
            }
            // Other types (primitives, arrays, etc.) are skipped
        }
    }

    /**
     * Recursively validates a declared type and its generic type arguments.
     * Ensures all user-defined types are annotated with @NetworkModel.
     * Standard library types (java.*, javax.*, kotlin.*, android.*) are skipped.
     *
     * @param context The lint context
     * @param node The call expression node (for error reporting)
     * @param psiClassType The type to validate
     */
    private fun validateDeclaredType(context: JavaContext, node: UCallExpression, psiClassType: PsiClassType) {
        val psiClass = psiClassType.resolve() ?: return

        // Skip validation for standard library types (String, Integer, List, etc.)
        // These types cannot be annotated with @NetworkModel
        if (!isStandardLibraryType(psiClass)) {
            // Check if user-defined type has @NetworkModel annotation
            if (!psiClass.hasAnnotation(NETWORK_MODEL_ANNOTATION)) {
                reportUsage(context, node, psiClass.name ?: "Unknown")
            }
        }

        // Recursively validate generic type arguments (e.g., List<Item>, Map<String, User>)
        validateGenericTypeArguments(context, node, psiClassType)
    }

    /**
     * Checks if a type is from a standard library package.
     * Standard library types don't need @NetworkModel annotation.
     *
     * @param psiClass The class to check
     * @return true if the type is from a standard library
     */
    private fun isStandardLibraryType(psiClass: PsiClass): Boolean {
        val qualifiedName = psiClass.qualifiedName ?: return false
        return STANDARD_LIBRARY_PREFIXES.any { prefix ->
            qualifiedName.startsWith(prefix)
        }
    }

    /**
     * Validates generic type arguments of a declared type.
     * For example, validates 'Item' in List<Item> or 'User' in Map<String, User>.
     *
     * @param context The lint context
     * @param node The call expression node (for error reporting)
     * @param psiClassType The type whose generic arguments should be validated
     */
    private fun validateGenericTypeArguments(context: JavaContext, node: UCallExpression, psiClassType: PsiClassType) {
        psiClassType.parameters.forEach { typeArgument ->
            validateType(context, node, typeArgument)
        }
    }

    // ============================================================
    // Error Reporting
    // ============================================================

    private fun reportUsage(context: JavaContext, node: UCallExpression, typeName: String) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = true,
                includeArguments = true
            ),
            message = "Type '$typeName' must be annotated with @NetworkModel.",
        )
    }
}
