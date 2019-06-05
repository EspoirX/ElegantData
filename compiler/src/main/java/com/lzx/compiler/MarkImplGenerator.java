package com.lzx.compiler;

import com.lzx.annoation.ElegantEntity;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * 创建被 @ElegantDataMark 标记的抽象类的实体类
 * create by lzx
 * 2019-05-30
 */
class MarkImplGenerator {

    private List<AnnotationEntityClass> mEntityClassList;
    private ElegantDataMarkInfo markInfo;
    private Filer mFiler;
    private Map<String, String> fileNameMap = new HashMap<>();
    private String packageName;

    MarkImplGenerator(List<AnnotationEntityClass> entityClassList, Filer filer, ElegantDataMarkInfo markInfo) {
        mEntityClassList = entityClassList;
        mFiler = filer;
        this.markInfo = markInfo;
        for (AnnotationEntityClass entityClass : entityClassList) {
            packageName = entityClass.getPackageName();
            fileNameMap.put(entityClass.getClazzName(), entityClass.getFileName());
        }
    }

    /**
     * 创建FileDataBase实现类
     */
    void createElegantDataMarkImpl() {
        if (markInfo == null || "".equals(markInfo.markClassName)) {
            return;
        }
        if (markInfo.mFieldInfos.size() != mEntityClassList.size()) {
            return;
        }
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();

        createFieldsAndMethods(fieldSpecs, methodSpecs);

        TypeSpec typeSpec = TypeSpec.classBuilder(markInfo.markClassName + GeneratorHelper.IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(packageName, markInfo.markClassName))
                .addFields(fieldSpecs)
                .addMethod(createDataFolderHelper())
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建createDataFolderHelper方法
     */
    private MethodSpec createDataFolderHelper() {
        return MethodSpec.methodBuilder("createDataFolderHelper")
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "Configuration"), "configuration")
                .returns(ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "IFolderCreateHelper"))
                .addStatement("return configuration.mFactory.create(configuration.context, configuration.destFileDir)")
                .build();
    }

    private void createFieldsAndMethods(List<FieldSpec> fieldSpecs, List<MethodSpec> methodSpecs) {
        for (int i = 0; i < markInfo.mFieldInfos.size(); i++) {
            ElegantDataMarkInfo.FieldInfo fieldInfo = markInfo.mFieldInfos.get(i);
            AnnotationEntityClass entityClass = mEntityClassList.get(i);

            String spFileName = fileNameMap.get(fieldInfo.getFieldTypeNameString());

            String fieldName = fieldInfo.fieldName;
            if (fieldName.contains("get")) {
                fieldName = fieldName.replace("get", "m");
            }
            ClassName fieldTypeName = ClassName.get(packageName, fieldInfo.fieldTypeName.toString());
            fieldSpecs.add(
                    FieldSpec
                            .builder(fieldTypeName, fieldName, Modifier.PRIVATE)
                            .build());
            if (entityClass.getFileType() == ElegantEntity.TYPE_PREFERENCE) {
                methodSpecs.add(MethodSpec.methodBuilder(fieldInfo.fieldName)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(fieldTypeName)
                        .addCode("if ($N != null) {\n", fieldName)
                        .addStatement("    return $N", fieldName)
                        .addCode("} else {\n")
                        .addCode("    synchronized (this) {\n")
                        .addCode("        if ($N == null) {\n", fieldName)
                        .addCode("            $T sharedPreferences = getCreateHelper().getContext()\n", GeneratorHelper.getSharedPreferences())
                        .addStatement("                    .getSharedPreferences(\"$N\", $T.MODE_PRIVATE)", spFileName,
                                ClassName.get("android.content", "Context"))
                        .addStatement("            $N = new $N(sharedPreferences)", fieldName,
                                fieldInfo.getFieldTypeNameString() + GeneratorHelper.IMPL_SUFFIX)
                        .addCode("        }\n")
                        .addStatement("        return $N", fieldName)
                        .addCode("    }\n")
                        .addCode("}")
                        .build()
                );
            } else if (entityClass.getFileType() == ElegantEntity.TYPE_FILE) {
                methodSpecs.add(MethodSpec.methodBuilder(fieldInfo.fieldName)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(fieldTypeName)
                        .addCode("if ($N != null) {\n", fieldName)
                        .addStatement("    return $N", fieldName)
                        .addCode("} else {\n")
                        .addCode("    synchronized (this) {\n")
                        .addCode("        if ($N == null) {\n", fieldName)
                        .addStatement("IFolderCreateHelper createHelper = getCreateHelper()")
                        .addStatement("            $N = new $N(createHelper)", fieldName,
                                fieldInfo.getFieldTypeNameString() + GeneratorHelper.IMPL_SUFFIX)
                        .addCode("        }\n")
                        .addStatement("        return $N", fieldName)
                        .addCode("    }\n")
                        .addCode("}")
                        .build());
            }
        }
    }


}
