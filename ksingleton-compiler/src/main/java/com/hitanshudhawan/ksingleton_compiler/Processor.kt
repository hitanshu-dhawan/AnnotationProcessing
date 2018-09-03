package com.hitanshudhawan.ksingleton_compiler

import com.hitanshudhawan.ksingleton_annotations.KSingleton
import java.util.*
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

        for (typeElement in ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(KSingleton::class.java))) {
            if (!checkForPrivateConstructors(typeElement)) return false
            if (!checkForGetInstanceMethod(typeElement)) return false
        }

        return true
    }

    private fun checkForPrivateConstructors(typeElement: TypeElement): Boolean {
        val constructors = ElementFilter.constructorsIn(typeElement.enclosedElements)
        for (constructor in constructors) {
            if (constructor.modifiers.isEmpty() || !constructor.modifiers.contains(Modifier.PRIVATE)) {
                mProcessingEnvironment!!.messager.printMessage(Diagnostic.Kind.ERROR, "constructor of a singleton class must be private", constructor)
                return false
            }
        }
        return true
    }

    private fun checkForGetInstanceMethod(typeElement: TypeElement): Boolean {
        val methods = ElementFilter.methodsIn(typeElement.enclosedElements)
        for (method in methods) {

            // check for name
            if (method.simpleName.contentEquals("getInstance")) {

                // check for return type
                if (mProcessingEnvironment!!.typeUtils.isSameType(method.returnType, typeElement.asType())) {

                    // check for modifiers
                    if (method.modifiers.contains(Modifier.PRIVATE)) {
                        mProcessingEnvironment!!.messager.printMessage(Diagnostic.Kind.ERROR, "getInstance method can't have a private modifier", method)
                        return false
                    }
                    if (!method.modifiers.contains(Modifier.STATIC)) {
                        mProcessingEnvironment!!.messager.printMessage(Diagnostic.Kind.ERROR, "getInstance method should have a static modifier", method)
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return object : HashSet<String>() {
            init {
                add(KSingleton::class.java.canonicalName)
            }
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}