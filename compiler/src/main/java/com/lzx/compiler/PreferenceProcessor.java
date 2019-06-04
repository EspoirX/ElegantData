package com.lzx.compiler;

import com.google.auto.service.AutoService;
import com.lzx.annoation.ElegantDataMark;
import com.lzx.annoation.Embedded;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.NameField;
import com.lzx.annoation.PreferenceEntity;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class PreferenceProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Messager mMessager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add(PreferenceEntity.class.getCanonicalName());
        supportedTypes.add(IgnoreField.class.getCanonicalName());
        supportedTypes.add(Embedded.class.getCanonicalName());
        supportedTypes.add(NameField.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> markElementSet = roundEnv.getElementsAnnotatedWith(ElegantDataMark.class);
        if (markElementSet.size() == 0) {
            return false;
        }
        if (markElementSet.size() > 1) {
            throw new RuntimeException("Can only mark one @ElegantDataMark");
        }
        ElegantDataMarkInfo markInfo = null;
        for (Element element : markElementSet) {
            TypeElement type = (TypeElement) element;

            markInfo = new ElegantDataMarkInfo();
            markInfo.markClassName = type.getSimpleName().toString();
            List<ElegantDataMarkInfo.FieldInfo> fieldInfos = new ArrayList<>();

            for (Element variable : type.getEnclosedElements()) {
                if (variable.getKind() == ElementKind.METHOD) {
                    ElegantDataMarkInfo.FieldInfo info = new ElegantDataMarkInfo.FieldInfo();
                    ExecutableElement executableElement = (ExecutableElement) variable;
                    Set<Modifier> modifiers = executableElement.getModifiers();
                    for (Modifier modifier : modifiers) {
                        if (modifier.name().equals("ABSTRACT")) {
                            info.fieldName = executableElement.getSimpleName().toString();
                            info.fieldTypeName = TypeName.get(executableElement.getReturnType());
                            fieldInfos.add(info);
                        }
                    }
                }
            }
            markInfo.mFieldInfos = fieldInfos;
        }
        //返回所有被注解了@PreferenceEntity的元素的列表
        Set<? extends Element> preferenceElementSet = roundEnv.getElementsAnnotatedWith(PreferenceEntity.class);

        List<PreferenceEntityClass> entityClassList = new ArrayList<>();
        for (Element element : preferenceElementSet) {
            //类信息
            TypeElement type = (TypeElement) element;
            try {
                //检查类信息
                checkValidEntityType(type);
                PreferenceEntityClass entityClass = new PreferenceEntityClass(type, mElements, mMessager);
                entityClassList.add(entityClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (PreferenceEntityClass entityClass : entityClassList) {
            //解析注解
            generatePreferenceInjector(entityClass, markInfo);
        }

        return false;
    }

    /**
     * 检查合法性
     * 1. PreferenceEntity只能加在类上面
     * 2. 类不能有 final 修饰
     * 3. 类不能有 private 修饰
     */
    private void checkValidEntityType(TypeElement annotatedType) throws IllegalAccessException {
        if (!annotatedType.getKind().isInterface()) {
            throw new IllegalAccessException("Only Interface can be annotated with @PreferenceEntity");
        } else if (annotatedType.getModifiers().contains(Modifier.FINAL)) {
            showErrorLog("class modifier should not be final", annotatedType);
        } else if (annotatedType.getModifiers().contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be private", annotatedType);
        }
    }

    private void generatePreferenceInjector(PreferenceEntityClass entityClass, ElegantDataMarkInfo markInfo) {
        PreferenceGenerator generator = new PreferenceGenerator(entityClass, mElements, mFiler, markInfo);
        try {
            generator.createPreferenceDaoInterface();
            generator.createPreferenceDaoImpl();
            generator.createDataBaseImpl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showErrorLog(String message, Element element) {
        mMessager.printMessage(ERROR, "Error:" + message, element);
    }


    private void showLog(String message) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
