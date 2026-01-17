package com.hitanshudhawan.networkmodel

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

class Processor : AbstractProcessor() {

    private var mProcessingEnvironment: ProcessingEnvironment? = null

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        mProcessingEnvironment = processingEnvironment
    }

    override fun process(annotations: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

        for (typeElement in ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(NetworkModel::class.java))) {
            val fields = ElementFilter.fieldsIn(typeElement.enclosedElements)
            for (field in fields) {
                if (!field.modifiers.contains(Modifier.STATIC) && !field.modifiers.contains(Modifier.TRANSIENT)) {
                    var hasSerializedName = false
                    for (annotationMirror in field.annotationMirrors) {
                        if (annotationMirror.annotationType.asElement().toString() == "com.google.gson.annotations.SerializedName") {
                            hasSerializedName = true
                            break
                        }
                    }
                    if (!hasSerializedName) {
                        mProcessingEnvironment!!.messager.printMessage(Diagnostic.Kind.ERROR, "Missing @SerializedName annotation on field: ${field.simpleName}", field)
                    }

                    val typeMirror = field.asType()
                    if (typeMirror.kind == TypeKind.DECLARED) {
                        val declaredType = typeMirror as DeclaredType
                        val fieldTypeElement = declaredType.asElement() as TypeElement
                        val qualifiedName = fieldTypeElement.qualifiedName.toString()
                        if (!qualifiedName.startsWith("java.") &&
                            !qualifiedName.startsWith("javax.") &&
                            !qualifiedName.startsWith("android.") &&
                            !qualifiedName.startsWith("kotlin.")) {

                            var isAnnotated = false
                            for (annotationMirror in fieldTypeElement.annotationMirrors) {
                                if (annotationMirror.annotationType.asElement().toString() == NetworkModel::class.java.canonicalName) {
                                    isAnnotated = true
                                    break
                                }
                            }
                            if (!isAnnotated) {
                                mProcessingEnvironment!!.messager.printMessage(Diagnostic.Kind.ERROR, "Field '${field.simpleName}' is of type '${fieldTypeElement.simpleName}' which is not annotated with @NetworkModel", field)
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(NetworkModel::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}
