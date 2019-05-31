package com.lzx.compiler;

import com.google.auto.service.AutoService;
import com.lzx.annoation.Embedded;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.NameField;
import com.lzx.annoation.PreferenceEntity;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 返回所有被注解了@PreferenceEntity的元素的列表
        Set<? extends Element> preferenceElementSet = roundEnv.getElementsAnnotatedWith(PreferenceEntity.class);
        for (Element element : preferenceElementSet) {
            //类信息
            TypeElement type = (TypeElement) element;
            try {
                //检查类信息
                checkValidEntityType(type);

                //解析
                processPreferenceEntity(type);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
        if (!annotatedType.getKind().isClass()) {
            throw new IllegalAccessException("Only classes can be annotated with @PreferenceEntity");
        } else if (annotatedType.getModifiers().contains(Modifier.FINAL)) {
            showErrorLog("class modifier should not be final", annotatedType);
        } else if (annotatedType.getModifiers().contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be private", annotatedType);
        }
    }

    private void processPreferenceEntity(TypeElement type) throws IllegalAccessException {
        PreferenceEntityClass entityClass = new PreferenceEntityClass(type, mElements, mMessager);
        generatePreferenceInjector(entityClass);
    }

    private void generatePreferenceInjector(PreferenceEntityClass entityClass) {
        try {
            PreferenceGenerator generator = new PreferenceGenerator(entityClass, mElements);
            //创建一些公用的帮助类
            PublicJavaFile.createDataAction(entityClass.getPackageName(), mFiler);
            PublicJavaFile.createDispatcher(entityClass.getPackageName(), mFiler);
            PublicJavaFile.createPreferenceManager(entityClass.getPackageName(), mFiler);
            PublicJavaFile.createPreferenceCallBack(entityClass.getPackageName(), mFiler);
            //创建具体的实现类
            JavaFile javaFile = JavaFile.builder(entityClass.getPackageName(), generator.getTypeSpec()).build();
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
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
