package com.hitanshudhawan.networkmodel

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
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
