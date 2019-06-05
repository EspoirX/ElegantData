package com.lzx.compiler;

import com.google.auto.service.AutoService;
import com.lzx.annoation.ElegantDataMark;
import com.lzx.annoation.EntityClass;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.NameField;
import com.lzx.annoation.PreferenceEntity;
import com.squareup.javapoet.TypeName;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

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
        supportedTypes.add(EntityClass.class.getCanonicalName());
        supportedTypes.add(NameField.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //解析@ElegantDataMark
        Set<? extends Element> markElementSet = roundEnv.getElementsAnnotatedWith(ElegantDataMark.class);
        if (markElementSet.size() == 0) {
            return false;
        }
        if (markElementSet.size() > 1) {
            throw new RuntimeException("Can only mark one @ElegantDataMark");
        }
        //获取ElegantDataMarkInfo
        ElegantDataMarkInfo markInfo = getElegantDataMarkInfo(markElementSet);
        //解析@PreferenceEntity
        Set<? extends Element> preferenceElementSet = roundEnv.getElementsAnnotatedWith(PreferenceEntity.class);
        List<PreferenceEntityClass> entityClassList = getPreferenceEntityClassList(preferenceElementSet);
        //创建被 @ElegantDataMark 标记的抽象类的实现类
        MarkImplGenerator fileDataImplGenerator = new MarkImplGenerator(entityClassList, mFiler, markInfo);
        fileDataImplGenerator.createElegantDataMarkImpl();
        //创建被 @PreferenceEntity 标记的实现类
        for (PreferenceEntityClass entityClass : entityClassList) {
            createPreferenceEntityImpl(entityClass);
        }
        return false;
    }


    /**
     * 获取ElegantDataMarkInfo
     */
    private ElegantDataMarkInfo getElegantDataMarkInfo(Set<? extends Element> markElementSet) {
        ElegantDataMarkInfo markInfo = null;
        for (Element element : markElementSet) {
            TypeElement type = (TypeElement) element;
            try {
                checkValidElegantDataMarkType(type);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
        return markInfo;
    }

    /**
     * 获取List<PreferenceEntityClass>
     */
    private List<PreferenceEntityClass> getPreferenceEntityClassList(Set<? extends Element> preferenceElementSet) {
        List<PreferenceEntityClass> entityClassList = new ArrayList<>();
        for (Element element : preferenceElementSet) {
            TypeElement type = (TypeElement) element;
            try {
                checkValidPreferenceEntityType(type);   //检查类信息
                PreferenceEntityClass entityClass = new PreferenceEntityClass(type, mElements);
                entityClassList.add(entityClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return entityClassList;
    }

    /**
     * 创建被 @PreferenceEntity 标记的实现类
     */
    private void createPreferenceEntityImpl(PreferenceEntityClass entityClass) {
        PreferenceGenerator generator = new PreferenceGenerator(entityClass, mFiler);
        try {
            generator.createPreferenceDaoInterface();
            generator.createPreferenceDaoImpl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查@PreferenceEntity合法性
     */
    private void checkValidPreferenceEntityType(TypeElement annotatedType) throws IllegalAccessException {
        if (!annotatedType.getKind().isInterface()) {
            throw new IllegalAccessException("Only Interface can be annotated with @PreferenceEntity");
        } else if (annotatedType.getModifiers().contains(Modifier.FINAL)) {
            showErrorLog("class modifier should not be final", annotatedType);
        } else if (annotatedType.getModifiers().contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be private", annotatedType);
        }
    }

    /**
     * 检查@ElegantDataMark合法性
     */
    private void checkValidElegantDataMarkType(TypeElement annotatedType) throws IllegalAccessException {
        if (!annotatedType.getKind().isClass()) {
            throw new IllegalAccessException("Only Class can be annotated with @ElegantDataMark");
        } else if (!annotatedType.getModifiers().contains(Modifier.ABSTRACT)) {
            showErrorLog("class modifier should be abstract", annotatedType);
        } else if (annotatedType.getModifiers().contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be private", annotatedType);
        }
    }

    private void showErrorLog(String message, Element element) {
        mMessager.printMessage(ERROR, "Error:" + message, element);
    }
}
