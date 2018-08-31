package com.hitanshudhawan.butterknife_compiler;

import com.hitanshudhawan.butterknife_annotations.BindView;
import com.hitanshudhawan.butterknife_annotations.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

public class Processor extends AbstractProcessor {

    private ProcessingEnvironment mProcessingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mProcessingEnvironment = processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        for (TypeElement typeElement : getTypeElementsToProcess(roundEnvironment.getRootElements(), annotations)) {

            String packageName = mProcessingEnvironment.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            String typeName = typeElement.getSimpleName().toString();
            ClassName className = ClassName.get(packageName, typeName);

            ClassName generatedClassName = ClassName
                    .get(packageName, typeName + "Binder");

            // define the wrapper class
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName)
                    .addModifiers(Modifier.PUBLIC);
            //.addAnnotation(Keep.class);

            // add constructor
            classBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "activity")
                    .addStatement("$N($N)",
                            "bindViews",
                            "activity")
                    .addStatement("$N($N)",
                            "bindOnClicks",
                            "activity")
                    .build());

            // add method that maps the views with id
            MethodSpec.Builder bindViewsMethodBuilder = MethodSpec
                    .methodBuilder("bindViews")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(void.class)
                    .addParameter(className, "activity");

            for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                BindView bindView = variableElement.getAnnotation(BindView.class);
                if (bindView != null) {
                    bindViewsMethodBuilder.addStatement("$N.$N = ($T) $N.findViewById($L)",
                            "activity",
                            variableElement.getSimpleName(),
                            variableElement,
                            "activity",
                            bindView.value());
                }
            }
            classBuilder.addMethod(bindViewsMethodBuilder.build());

            // add method that attaches the onClickListeners
            ClassName androidOnClickListenerClassName = ClassName.get(
                    "android.view",
                    "View",
                    "OnClickListener");

            ClassName androidViewClassName = ClassName.get(
                    "android.view",
                    "View");

            MethodSpec.Builder bindOnClicksMethodBuilder = MethodSpec
                    .methodBuilder("bindOnClicks")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(void.class)
                    .addParameter(className, "activity", Modifier.FINAL);

            for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                OnClick onClick = executableElement.getAnnotation(OnClick.class);
                if (onClick != null) {
                    TypeSpec OnClickListenerClass = TypeSpec.anonymousClassBuilder("")
                            .addSuperinterface(androidOnClickListenerClassName)
                            .addMethod(MethodSpec.methodBuilder("onClick")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(androidViewClassName, "view")
                                    .addStatement("$N.$N($N)",
                                            "activity",
                                            executableElement.getSimpleName(),
                                            "view")
                                    .returns(void.class)
                                    .build())
                            .build();
                    bindOnClicksMethodBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                            "activity",
                            onClick.value(),
                            OnClickListenerClass);
                }
            }
            classBuilder.addMethod(bindOnClicksMethodBuilder.build());

            // write the defines class to a java file
            try {
                JavaFile.builder(packageName,
                        classBuilder.build())
                        .build()
                        .writeTo(mProcessingEnvironment.getFiler());
            } catch (IOException e) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString(), typeElement);
            }

        }

        return true;
    }

    private Set<TypeElement> getTypeElementsToProcess(Set<? extends Element> elements,
                                                      Set<? extends TypeElement> supportedAnnotations) {
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                boolean found = false;
                for (Element subElement : element.getEnclosedElements()) {
                    for (AnnotationMirror mirror : subElement.getAnnotationMirrors()) {
                        for (Element annotation : supportedAnnotations) {
                            if (mirror.getAnnotationType().asElement().equals(annotation)) {
                                typeElements.add((TypeElement) element);
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (found) break;
                }
            }
        }
        return typeElements;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            add(BindView.class.getCanonicalName());
            add(OnClick.class.getCanonicalName());
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}