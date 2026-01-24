package com.hitanshudhawan.networkmodelvalidator

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

/**
 * Annotation processor for @NetworkModel that validates network model classes.
 *
 * This processor enforces the following rules:
 * 1. All non-static, non-transient fields must have @SerializedName annotation
 * 2. All field types (and their generic type arguments) must be annotated with @NetworkModel
 * 3. Subclasses of @NetworkModel annotated classes must also be annotated with @NetworkModel
 */
class NetworkModelValidatorProcessor : AbstractProcessor() {

    // ============================================================
    // Constants
    // ============================================================

    companion object {
        /** Fully qualified name of Gson's @SerializedName annotation */
        private const val SERIALIZED_NAME_ANNOTATION = "com.google.gson.annotations.SerializedName"

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
    }

    // ============================================================
    // Main Processing Entry Point
    // ============================================================

    /**
     * Main processing method called by the annotation processing framework.
     *
     * @param annotations Set of annotation types being processed
     * @param roundEnvironment Environment providing information about the current processing round
     * @return true to indicate that these annotations are claimed by this processor
     */
    override fun process(annotations: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

        // Step 1: Collect all classes annotated with @NetworkModel in this round
        val networkModelClasses = getNetworkModelAnnotatedClasses(roundEnvironment)

        // Step 2: Validate fields in each @NetworkModel class
        networkModelClasses.forEach { typeElement ->
            validateClassFields(typeElement)
        }

        // Step 3: Ensure subclasses of @NetworkModel classes are also annotated
        getAllRootClasses(roundEnvironment).forEach { typeElement ->
            validateSubclassAnnotation(typeElement, networkModelClasses)
        }

        return true
    }

    // ============================================================
    // Class Collection Helpers
    // ============================================================

    /**
     * Retrieves all classes annotated with @NetworkModel in the current processing round.
     *
     * @param roundEnvironment The current round environment
     * @return Set of TypeElements representing @NetworkModel annotated classes
     */
    private fun getNetworkModelAnnotatedClasses(roundEnvironment: RoundEnvironment): Set<TypeElement> {
        return ElementFilter.typesIn(
            roundEnvironment.getElementsAnnotatedWith(NetworkModel::class.java)
        ).toSet()
    }

    /**
     * Retrieves all root type elements in the current processing round.
     *
     * @param roundEnvironment The current round environment
     * @return Set of all TypeElements being processed
     */
    private fun getAllRootClasses(roundEnvironment: RoundEnvironment): Set<TypeElement> {
        return ElementFilter.typesIn(roundEnvironment.rootElements).toSet()
    }

    // ============================================================
    // Field Validation
    // ============================================================

    /**
     * Validates all fields in a @NetworkModel annotated class.
     * Checks that each serializable field has @SerializedName and valid types.
     *
     * @param classElement The class to validate
     */
    private fun validateClassFields(classElement: TypeElement) {
        getSerializableFields(classElement).forEach { field ->
            // Check 1: Field must have @SerializedName annotation
            validateSerializedNameAnnotation(field)
            // Check 2: Field type must be annotated with @NetworkModel
            validateFieldType(field)
        }
    }

    /**
     * Gets all serializable fields from a class.
     * Serializable fields are non-static and non-transient instance fields.
     *
     * @param classElement The class to get fields from
     * @return List of serializable VariableElements (fields)
     */
    private fun getSerializableFields(classElement: TypeElement): List<VariableElement> {
        return ElementFilter.fieldsIn(classElement.enclosedElements)
            .filter { field -> isSerializableField(field) }
    }

    /**
     * Checks if a field should be serialized.
     * Static and transient fields are excluded from serialization.
     *
     * @param field The field to check
     * @return true if the field should be serialized
     */
    private fun isSerializableField(field: VariableElement): Boolean {
        val modifiers = field.modifiers
        val isStatic = modifiers.contains(Modifier.STATIC)
        val isTransient = modifiers.contains(Modifier.TRANSIENT)
        return !isStatic && !isTransient
    }

    /**
     * Validates that a field has the @SerializedName annotation.
     * Reports an error if the annotation is missing.
     *
     * @param field The field to validate
     */
    private fun validateSerializedNameAnnotation(field: VariableElement) {
        if (!hasAnnotation(field, SERIALIZED_NAME_ANNOTATION)) {
            reportError(
                message = "Missing @SerializedName annotation on field: ${field.simpleName}",
                element = field
            )
        }
    }

    /**
     * Validates that a field's type is annotated with @NetworkModel.
     * Only checks declared types (classes/interfaces), not primitives.
     *
     * @param field The field whose type should be validated
     */
    private fun validateFieldType(field: VariableElement) {
        val typeMirror = field.asType()

        // Only validate declared types (classes/interfaces), skip primitives
        if (typeMirror.kind == TypeKind.DECLARED) {
            val declaredType = typeMirror as DeclaredType
            validateDeclaredType(declaredType, field)
        }
    }

    // ============================================================
    // Type Validation
    // ============================================================

    /**
     * Recursively validates a declared type and its generic type arguments.
     * Ensures all user-defined types are annotated with @NetworkModel.
     * Standard library types (java.*, javax.*, kotlin.*, android.*) are skipped.
     *
     * @param declaredType The type to validate
     * @param field The field element (for error pinpointing)
     */
    private fun validateDeclaredType(declaredType: DeclaredType, field: VariableElement) {
        val typeElement = declaredType.asElement() as TypeElement

        // Skip validation for standard library types (String, Integer, List, etc.)
        // These types cannot be annotated with @NetworkModel
        if (!isStandardLibraryType(typeElement)) {
            // Check if user-defined type has @NetworkModel annotation
            if (!hasNetworkModelAnnotation(typeElement)) {
                reportError(
                    message = "Field '${field.simpleName}' contains type '${typeElement.simpleName}' which is not annotated with @NetworkModel",
                    element = field
                )
            }
        }

        // Recursively validate generic type arguments (e.g., List<Item>, Map<String, User>)
        validateGenericTypeArguments(declaredType, field)
    }

    /**
     * Checks if a type is from a standard library package.
     * Standard library types don't need @NetworkModel annotation.
     *
     * @param typeElement The type element to check
     * @return true if the type is from a standard library
     */
    private fun isStandardLibraryType(typeElement: TypeElement): Boolean {
        val qualifiedName = typeElement.qualifiedName.toString()
        return STANDARD_LIBRARY_PREFIXES.any { prefix ->
            qualifiedName.startsWith(prefix)
        }
    }

    /**
     * Validates generic type arguments of a declared type.
     * For example, validates 'Item' in List<Item> or 'User' in Map<String, User>.
     *
     * @param declaredType The type whose generic arguments should be validated
     * @param field The field element (for error pinpointing)
     */
    private fun validateGenericTypeArguments(declaredType: DeclaredType, field: VariableElement) {
        declaredType.typeArguments
            .filter { it.kind == TypeKind.DECLARED }
            .forEach { typeArgument ->
                validateDeclaredType(typeArgument as DeclaredType, field)
            }
    }

    // ============================================================
    // Subclass Validation
    // ============================================================

    /**
     * Validates that if a class extends a @NetworkModel annotated class,
     * the subclass must also be annotated with @NetworkModel.
     *
     * @param classElement The class to check
     * @param networkModelClasses Set of known @NetworkModel classes in current round
     */
    private fun validateSubclassAnnotation(
        classElement: TypeElement,
        networkModelClasses: Set<TypeElement>
    ) {
        // Get the superclass (if it's a declared type, not Object or primitive)
        val superclass = getSuperclass(classElement) ?: return

        // Check if the superclass has @NetworkModel annotation
        val isSuperclassAnnotated = isNetworkModelClass(superclass, networkModelClasses)
        if (!isSuperclassAnnotated) return

        // If superclass is annotated, subclass must also be annotated
        val isSubclassAnnotated = isNetworkModelClass(classElement, networkModelClasses)
        if (!isSubclassAnnotated) {
            reportError(
                message = "Class '${classElement.simpleName}' extends '${superclass.simpleName}' " +
                        "which is annotated with @NetworkModel, but '${classElement.simpleName}' " +
                        "is not annotated with @NetworkModel",
                element = classElement
            )
        }
    }

    /**
     * Gets the superclass of a type element, if it exists and is a declared type.
     *
     * @param classElement The class to get the superclass of
     * @return The superclass TypeElement, or null if none exists
     */
    private fun getSuperclass(classElement: TypeElement): TypeElement? {
        val superclassMirror = classElement.superclass
        if (superclassMirror.kind != TypeKind.DECLARED) return null
        return (superclassMirror as DeclaredType).asElement() as TypeElement
    }

    /**
     * Checks if a class is annotated with @NetworkModel.
     * Checks both the current round's annotated classes and existing annotations.
     *
     * @param classElement The class to check
     * @param networkModelClasses Set of known @NetworkModel classes in current round
     * @return true if the class has @NetworkModel annotation
     */
    private fun isNetworkModelClass(
        classElement: TypeElement,
        networkModelClasses: Set<TypeElement>
    ): Boolean {
        // Check if it's in the current round's annotated classes
        if (networkModelClasses.contains(classElement)) return true

        // Check if it has the annotation from a previous compilation
        return hasNetworkModelAnnotation(classElement)
    }

    // ============================================================
    // Annotation Helpers
    // ============================================================

    /**
     * Checks if an element has a specific annotation.
     *
     * @param element The element to check
     * @param annotationName Fully qualified name of the annotation
     * @return true if the element has the annotation
     */
    private fun hasAnnotation(element: Element, annotationName: String): Boolean {
        return element.annotationMirrors.any { annotationMirror ->
            annotationMirror.annotationType.asElement().toString() == annotationName
        }
    }

    /**
     * Checks if a type element has the @NetworkModel annotation.
     *
     * @param typeElement The type element to check
     * @return true if the element has @NetworkModel annotation
     */
    private fun hasNetworkModelAnnotation(typeElement: TypeElement): Boolean {
        return hasAnnotation(typeElement, NetworkModel::class.java.canonicalName)
    }

    // ============================================================
    // Error Reporting
    // ============================================================

    /**
     * Reports a compilation error.
     *
     * @param message The error message to display
     * @param element Optional element to associate with the error (for IDE navigation)
     */
    private fun reportError(message: String, element: Element? = null) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    // ============================================================
    // Processor Configuration
    // ============================================================

    /**
     * Returns the set of annotation types this processor supports.
     */
    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(NetworkModel::class.java.canonicalName)
    }

    /**
     * Returns the latest supported source version.
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}
