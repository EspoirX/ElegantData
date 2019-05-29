package com.lzx.compiler;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.lzx.annoation.Embedded;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.PreferenceEntity;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class) //自动注册
public class PreferenceProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Messager mMessager;
    private Map<String, List<Element>> classMap = new HashMap<>();

    private JavacTrees mTrees;
    private TreeMaker mTreeMaker;
    private Names mNames;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();

        mTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.mTreeMaker = TreeMaker.instance(context);
        mNames = Names.instance(context);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add(PreferenceEntity.class.getCanonicalName());
        supportedTypes.add(IgnoreField.class.getCanonicalName());
        supportedTypes.add(Embedded.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (set.isEmpty()) {
            return true;
        }
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
        return true;
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

    private void processPreferenceEntity(TypeElement type) {
        PreferenceEntity preferenceEntity = type.getAnnotation(PreferenceEntity.class);
        IgnoreField ignoreField = type.getAnnotation(IgnoreField.class);
        Embedded embedded = type.getAnnotation(Embedded.class);
        PackageElement packageElement = mElements.getPackageOf(type);
        //获取包名
        String packageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();
        //获取定义的类型
        TypeName typeName = TypeName.get(type.asType());
        //获取class名称
        String clazzName = type.getSimpleName().toString();
        //获取PreferenceEntity注解上的值
        String spFileName = Strings.isNullOrEmpty(preferenceEntity.fileName())
                ? StringUtils.toUpperCase(clazzName) : preferenceEntity.fileName();
        //遍历类中的元素
        for (Element variable : type.getEnclosedElements()) {
            //如果是变量
            if (variable instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) variable;

            }
        }
    }


    private void showErrorLog(String message, Element element) {
        mMessager.printMessage(ERROR, "Error:" + message, element);
    }


    private void showLog(String message) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
