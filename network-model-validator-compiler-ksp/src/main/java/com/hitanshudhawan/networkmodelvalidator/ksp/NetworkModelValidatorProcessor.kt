package com.hitanshudhawan.networkmodelvalidator.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

/**
 * KSP processor for @NetworkModel that validates network model classes.
 *
 * This processor is a direct migration of the JSR-269 (javax.annotation.processing)
 * NetworkModelValidatorProcessor and enforces the exact same rules:
 * 1. All non-static, non-transient fields must have @SerializedName annotation
 * 2. All field types (and their generic type arguments) must be annotated with @NetworkModel
 * 3. Subclasses of @NetworkModel annotated classes must also be annotated with @NetworkModel
 *
 * It works for both Kotlin and Java sources.
 */
class NetworkModelValidatorProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {

    // ============================================================
    // Constants
    // ============================================================

    companion object {
        /** Fully qualified name of the @NetworkModel annotation */
        private const val NETWORK_MODEL_ANNOTATION = "com.hitanshudhawan.networkmodelvalidator.ksp.NetworkModel"

        /** Fully qualified name of Gson's @SerializedName annotation */
        private const val SERIALIZED_NAME_ANNOTATION = "com.google.gson.annotations.SerializedName"

        /**
         * Package prefixes for standard library types that should be skipped during validation.
         * These types cannot be annotated with @NetworkModel since they are part of the platform.
         *
         * Note: KSP maps Java platform types onto their Kotlin equivalents
         * (e.g. java.lang.String -> kotlin.String, java.util.List -> kotlin.collections.MutableList,
         * primitives -> kotlin.Int/Long/...), all of which are still covered by these prefixes.
         */
        private val STANDARD_LIBRARY_PREFIXES = listOf(
            "java.",      // Java standard library (String, Integer, List, Map, etc.)
            "javax.",     // Java extensions (annotations, etc.)
            "kotlin.",    // Kotlin standard library (incl. mapped Java types)
            "android."    // Android framework classes
        )
    }

    // ============================================================
    // Main Processing Entry Point
    // ============================================================

    /**
     * Main processing method called by the KSP framework.
     *
     * @param resolver Provides access to the symbols being processed
     * @return List of symbols that could not be processed in this round (always empty here,
     *         since this is a validation-only processor that generates no code)
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {

        // Step 1: Collect all classes annotated with @NetworkModel in this round
        val networkModelClasses = getNetworkModelAnnotatedClasses(resolver)

        // Step 2: Validate fields in each @NetworkModel class
        networkModelClasses.forEach { classDeclaration ->
            validateClassFields(classDeclaration)
        }

        // Step 3: Ensure subclasses of @NetworkModel classes are also annotated
        getAllRootClasses(resolver).forEach { classDeclaration ->
            validateSubclassAnnotation(classDeclaration)
        }

        return emptyList()
    }

    // ============================================================
    // Class Collection Helpers
    // ============================================================

    /**
     * Retrieves all classes annotated with @NetworkModel in the current processing round.
     */
    private fun getNetworkModelAnnotatedClasses(resolver: Resolver): List<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(NETWORK_MODEL_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
    }

    /**
     * Retrieves all class declarations in the current processing round, including nested classes.
     * Mirrors the APT processor's use of RoundEnvironment.rootElements.
     */
    private fun getAllRootClasses(resolver: Resolver): List<KSClassDeclaration> {
        val result = mutableListOf<KSClassDeclaration>()
        resolver.getAllFiles().forEach { file ->
            collectClassDeclarations(file.declarations.filterIsInstance<KSClassDeclaration>(), result)
        }
        return result
    }

    private fun collectClassDeclarations(
        declarations: Sequence<KSClassDeclaration>,
        accumulator: MutableList<KSClassDeclaration>
    ) {
        declarations.forEach { classDeclaration ->
            accumulator.add(classDeclaration)
            collectClassDeclarations(
                classDeclaration.declarations.filterIsInstance<KSClassDeclaration>(),
                accumulator
            )
        }
    }

    // ============================================================
    // Field Validation
    // ============================================================

    /**
     * Validates all fields in a @NetworkModel annotated class.
     * Checks that each serializable field has @SerializedName and valid types.
     */
    private fun validateClassFields(classDeclaration: KSClassDeclaration) {
        getSerializableFields(classDeclaration).forEach { field ->
            // Check 1: Field must have @SerializedName annotation
            validateSerializedNameAnnotation(field)
            // Check 2: Field type must be annotated with @NetworkModel
            validateFieldType(field)
        }
    }

    /**
     * Gets all serializable fields declared on a class.
     * Serializable fields are non-static and non-transient instance fields.
     */
    private fun getSerializableFields(classDeclaration: KSClassDeclaration): List<KSPropertyDeclaration> {
        return classDeclaration.getDeclaredProperties()
            .filter { field -> isSerializableField(field) }
            .toList()
    }

    /**
     * Checks if a field should be serialized.
     * Static and transient fields are excluded from serialization.
     */
    private fun isSerializableField(field: KSPropertyDeclaration): Boolean {
        val modifiers = field.modifiers
        val isStatic = modifiers.contains(Modifier.JAVA_STATIC) || modifiers.contains(Modifier.CONST)
        val isTransient = modifiers.contains(Modifier.JAVA_TRANSIENT) ||
                field.hasAnnotation(TRANSIENT_ANNOTATION)
        return !isStatic && !isTransient
    }

    /**
     * Validates that a field has the @SerializedName annotation.
     * Reports an error if the annotation is missing.
     */
    private fun validateSerializedNameAnnotation(field: KSPropertyDeclaration) {
        if (!field.hasAnnotation(SERIALIZED_NAME_ANNOTATION)) {
            reportError(
                message = "Missing @SerializedName annotation on field: ${field.simpleName.asString()}",
                node = field
            )
        }
    }

    /**
     * Validates that a field's type is annotated with @NetworkModel.
     */
    private fun validateFieldType(field: KSPropertyDeclaration) {
        val type = field.type.resolve()
        validateDeclaredType(type, field)
    }

    // ============================================================
    // Type Validation
    // ============================================================

    /**
     * Recursively validates a declared type and its generic type arguments.
     * Ensures all user-defined types are annotated with @NetworkModel.
     * Standard library types (java.*, javax.*, kotlin.*, android.*) are skipped.
     */
    private fun validateDeclaredType(type: KSType, field: KSPropertyDeclaration) {
        val declaration = type.declaration

        // Only validate class/interface declarations (skip type parameters, error types, etc.)
        if (declaration is KSClassDeclaration && !type.isError) {
            // Skip validation for standard library types (String, Integer, List, etc.)
            // These types cannot be annotated with @NetworkModel
            if (!isStandardLibraryType(declaration)) {
                // Check if user-defined type has @NetworkModel annotation
                if (!hasNetworkModelAnnotation(declaration)) {
                    reportError(
                        message = "Field '${field.simpleName.asString()}' contains type " +
                                "'${declaration.simpleName.asString()}' which is not annotated with @NetworkModel",
                        node = field
                    )
                }
            }
        }

        // Recursively validate generic type arguments (e.g., List<Item>, Map<String, User>)
        validateGenericTypeArguments(type, field)
    }

    /**
     * Checks if a type is from a standard library package.
     */
    private fun isStandardLibraryType(declaration: KSClassDeclaration): Boolean {
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false
        return STANDARD_LIBRARY_PREFIXES.any { prefix ->
            qualifiedName.startsWith(prefix)
        }
    }

    /**
     * Validates generic type arguments of a declared type.
     * For example, validates 'Item' in List<Item> or 'User' in Map<String, User>.
     */
    private fun validateGenericTypeArguments(type: KSType, field: KSPropertyDeclaration) {
        type.arguments.forEach { argument ->
            val argumentType = argument.type?.resolve() ?: return@forEach
            if (argumentType.declaration is KSClassDeclaration && !argumentType.isError) {
                validateDeclaredType(argumentType, field)
            }
        }
    }

    // ============================================================
    // Subclass Validation
    // ============================================================

    /**
     * Validates that if a class extends a @NetworkModel annotated class,
     * the subclass must also be annotated with @NetworkModel.
     */
    private fun validateSubclassAnnotation(classDeclaration: KSClassDeclaration) {
        // Get the superclass (a real class, not an interface, and not Any/Object)
        val superclass = getSuperclass(classDeclaration) ?: return

        // Check if the superclass has @NetworkModel annotation
        if (!hasNetworkModelAnnotation(superclass)) return

        // If superclass is annotated, subclass must also be annotated
        if (!hasNetworkModelAnnotation(classDeclaration)) {
            reportError(
                message = "Class '${classDeclaration.simpleName.asString()}' extends " +
                        "'${superclass.simpleName.asString()}' which is annotated with @NetworkModel, " +
                        "but '${classDeclaration.simpleName.asString()}' is not annotated with @NetworkModel",
                node = classDeclaration
            )
        }
    }

    /**
     * Gets the superclass of a class declaration, if it exists and is an actual class
     * (not an interface, and not the implicit kotlin.Any / java.lang.Object supertype).
     */
    private fun getSuperclass(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
        return classDeclaration.superTypes
            .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
            .firstOrNull { superType ->
                superType.classKind == ClassKind.CLASS &&
                        superType.qualifiedName?.asString() != "kotlin.Any"
            }
    }

    // ============================================================
    // Annotation Helpers
    // ============================================================

    /**
     * Checks if a declaration has the @NetworkModel annotation.
     */
    private fun hasNetworkModelAnnotation(declaration: KSClassDeclaration): Boolean {
        return declaration.hasAnnotation(NETWORK_MODEL_ANNOTATION)
    }

    /**
     * Checks if an annotated element has a specific annotation.
     */
    private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean {
        return annotations.any { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
        }
    }

    // ============================================================
    // Error Reporting
    // ============================================================

    /**
     * Reports a compilation error.
     */
    private fun reportError(message: String, node: KSNode? = null) {
        logger.error(message, node)
    }
}

/** Fully qualified name of Kotlin's @Transient annotation */
private const val TRANSIENT_ANNOTATION = "kotlin.jvm.Transient"
