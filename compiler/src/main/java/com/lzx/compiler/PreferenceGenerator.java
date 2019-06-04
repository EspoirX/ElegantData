package com.lzx.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * create by lzx
 * 2019-05-30
 */
class PreferenceGenerator {

    public static final String IMPL_SUFFIX = "_Impl";
    private static final String CODE_PACKAGE_NAME = "com.lzx.code";
    private static final String preferencesName = "mPreferences";
    private PreferenceEntityClass mEntityClass;
    private Elements mElements;
    private ElegantDataMarkInfo markInfo;
    private String[] putArray;
    private String[] getArray;
    private TypeName[] typeNameArray;
    private Filer mFiler;

    PreferenceGenerator(PreferenceEntityClass entityClass, Elements elements, Filer filer, ElegantDataMarkInfo markInfo) {
        mEntityClass = entityClass;
        mElements = elements;
        mFiler = filer;
        this.markInfo = markInfo;
        putArray = new String[]{"putString", "putInt", "putLong", "putFloat", "putBoolean"};
        getArray = new String[]{"getString", "getInt", "getLong", "getFloat", "getBoolean"};
        typeNameArray = new TypeName[]{TypeName.get(String.class), TypeName.INT, TypeName.LONG, TypeName.FLOAT, TypeName.BOOLEAN};
    }

    /**
     * 创建Dao接口类
     */
    void createPreferenceDaoInterface() throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();
        for (PreferenceEntityField field : fieldList) {
            String fieldName = toUpperFirstChar(field.getFieldName());
            TypeName typeName = field.getTypeName();
            String putMethodName = "put" + fieldName;
            String putAsyncMethodName = "put" + fieldName + "Async";
            String getMethodName = "get" + fieldName;
            String getAsyncMethodName = "get" + fieldName + "Async";

            methodSpecs.add(MethodSpec.methodBuilder(putMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(typeName, "value")
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(putAsyncMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(typeName, "value")
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(getMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .returns(typeName)
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(getMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(typeName, "defValue")
                    .returns(typeName)
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(getAsyncMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(getPreferenceCallBack(), "callBack")
                    .addParameter(typeName, "defValue")
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(getAsyncMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(getPreferenceCallBack(), "callBack")
                    .build());
        }
        for (int i = 0; i < putArray.length; i++) {
            methodSpecs.add(MethodSpec.methodBuilder(putArray[i])
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(TypeName.get(String.class), "key")
                    .addParameter(typeNameArray[i], "value")
                    .returns(TypeName.BOOLEAN)
                    .build());
        }
        for (int i = 0; i < getArray.length; i++) {
            methodSpecs.add(MethodSpec.methodBuilder(getArray[i])
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(TypeName.get(String.class), "key")
                    .addParameter(typeNameArray[i], "value")
                    .returns(typeNameArray[i])
                    .build());
        }
        methodSpecs.add(MethodSpec.methodBuilder("remove")
                .addModifiers(ABSTRACT, PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(TypeName.get(String.class), "key")
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("clear")
                .addModifiers(ABSTRACT, PUBLIC)
                .returns(TypeName.BOOLEAN)
                .build());
        methodSpecs.add(MethodSpec.methodBuilder("contains")
                .addModifiers(ABSTRACT, PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(TypeName.get(String.class), "key")
                .build());
        TypeSpec typeSpec = TypeSpec.interfaceBuilder("I" + mEntityClass.getClazzName() + "Dao")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(getPackageName(), typeSpec).build();
        javaFile.writeTo(mFiler);
    }

    /**
     * 创建Dao实现类
     */
    void createPreferenceDaoImpl() throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(getSharedPreferences(), "sharedPreferences")
                .addStatement("$N = sharedPreferences", preferencesName)
                .addStatement("mDispatcher = new DataDispatcher()")
                .build());
        methodSpecs.addAll(createPutAndGetMethod());
        //创建put
        for (int i = 0; i < putArray.length; i++) {
            methodSpecs.add(createPutImplMethod(putArray[i], typeNameArray[i]));
        }
        //创建get
        for (int i = 0; i < getArray.length; i++) {
            methodSpecs.add(createGetImplMethod(getArray[i], typeNameArray[i]));
        }
        //创建remove,clear,contains
        methodSpecs.add(createRemoveAndClearImplMethod(true));
        methodSpecs.add(createRemoveAndClearImplMethod(false));
        methodSpecs.add(createContainsImplMethod());
        //创建getExecutor
        methodSpecs.add(createExecutor());

        TypeSpec typeSpec = TypeSpec.classBuilder(mEntityClass.getClazzName() + IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(mEntityClass.getTypeName())
                .addField(getSharedPreferences(), preferencesName, Modifier.PRIVATE)
                .addField(ClassName.get(CODE_PACKAGE_NAME, "DataDispatcher"), "mDispatcher", PRIVATE)
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(mEntityClass.getPackageName(), typeSpec).build();
        javaFile.writeTo(mFiler);
    }

    /**
     * 创建put封装方法
     */
    private MethodSpec createPutImplMethod(String methodName, TypeName valueTypeName) {
        TypeName stringType = TypeName.get(String.class);
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addAnnotation(Override.class)
                .addParameter(stringType, "key")
                .addParameter(valueTypeName, "value")
                .addStatement("SharedPreferences.Editor editor = $N.edit()", preferencesName)
                .addStatement("editor.$N(key, value)", methodName)
                .addStatement("return editor.commit()")
                .build();
    }

    /**
     * 创建get封装方法
     */
    private MethodSpec createGetImplMethod(String methodName, TypeName valueTypeName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .returns(valueTypeName)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(String.class), "key")
                .addParameter(valueTypeName, "defValue")
                .addStatement("return $N.$N(key, defValue)", preferencesName, methodName)
                .build();
    }

    /**
     * 创建remove 和 clear 封装方法
     */
    private MethodSpec createRemoveAndClearImplMethod(boolean isRemove) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(isRemove ? "remove" : "clear")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN);
        if (isRemove) {
            builder.addParameter(TypeName.get(String.class), "key");
        }
        builder
                .addStatement("SharedPreferences.Editor editor = $N.edit()", preferencesName)
                .addStatement(isRemove ? "editor.remove(key)" : "editor.clear()")
                .addStatement("return editor.commit()");
        return builder.build();
    }

    /**
     * 创建contains方法 封装方法
     */
    private MethodSpec createContainsImplMethod() {
        return MethodSpec.methodBuilder("contains")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addParameter(TypeName.get(String.class), "key")
                .addStatement("return $N.contains(key)", preferencesName)
                .build();
    }

    /**
     * 创建 put 和 get 具体实现方法
     */
    private List<MethodSpec> createPutAndGetMethod() {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();

        for (PreferenceEntityField field : fieldList) {
            String fieldName = toUpperFirstChar(field.getFieldName());
            TypeName typeName = field.getTypeName();
            String typeNameString = field.getTypeStringName();

            String putMethodName = "put" + fieldName;
            String putAsyncMethodName = "put" + fieldName + "Async";
            String putMethodImplName = "put" + typeNameString;

            String getMethodName = "get" + fieldName;
            String getAsyncMethodName = "get" + fieldName + "Async";
            String getMethodImplName = "get" + typeNameString;

            //put
            MethodSpec putMethodSpec = MethodSpec.methodBuilder(putMethodName)
                    .addModifiers(PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(typeName, "value", FINAL)
                    .addStatement("$N(\"$N\", value)", putMethodImplName, fieldName.toUpperCase())
                    .build();

            MethodSpec putAsyncMethodSpec = MethodSpec.methodBuilder(putAsyncMethodName)
                    .addModifiers(PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(typeName, "value", FINAL)
                    .addCode("getExecutor().execute(new Runnable() {\n")
                    .addCode("    @Override\n")
                    .addCode("    public void run() {\n")
                    .addStatement("$N(\"$N\", value)", putMethodImplName, fieldName.toUpperCase())
                    .addCode("    }\n")
                    .addStatement("})")
                    .build();

            //get
            //get 有 defValue
            MethodSpec.Builder getMethodDefBuilder = MethodSpec.methodBuilder(getMethodName).addModifiers(PUBLIC);
            createGetMethodSpec(getMethodDefBuilder, typeName, getMethodImplName, fieldName, true);
            MethodSpec getMethodDef = getMethodDefBuilder.build();

            //get 没 defValue
            MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(getMethodName).addModifiers(Modifier.PUBLIC);
            createGetMethodSpec(getMethodBuilder, typeName, getMethodImplName, fieldName, false);
            MethodSpec getMethod = getMethodBuilder.build();

            //get 异步有 defValue
            MethodSpec.Builder getAsyncBuilderDef = MethodSpec.methodBuilder(getAsyncMethodName).addModifiers(Modifier.PUBLIC);
            createGetMethodSpecAsync(getAsyncBuilderDef, typeName, getMethodImplName, fieldName, true);
            MethodSpec getAsyncMethodDef = getAsyncBuilderDef.build();
            //get 异步没 defValue
            MethodSpec.Builder getAsyncBuilder = MethodSpec.methodBuilder(getAsyncMethodName).addModifiers(Modifier.PUBLIC);
            createGetMethodSpecAsync(getAsyncBuilder, typeName, getMethodImplName, fieldName, false);
            MethodSpec getAsyncMethod = getAsyncBuilder.build();

            methodSpecList.add(putMethodSpec);
            methodSpecList.add(putAsyncMethodSpec);
            methodSpecList.add(getMethodDef);
            methodSpecList.add(getMethod);
            methodSpecList.add(getAsyncMethodDef);
            methodSpecList.add(getAsyncMethod);
        }
        return methodSpecList;
    }

    /**
     * 创建获取线程池内部方法
     */
    private MethodSpec createExecutor() {
        ClassName executorService = ClassName.get("java.util.concurrent", "ExecutorService");
        return MethodSpec.methodBuilder("getExecutor")
                .addModifiers(Modifier.PRIVATE)
                .returns(executorService)
                .addStatement("return mDispatcher.executorService()")
                .build();
    }

    /**
     * 创建FileDataBase实现类
     */
    void createDataBaseImpl() throws IOException {
        if (markInfo == null || "".equals(markInfo.markClassName)) {
            return;
        }
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();
        createFieldsAndMethods(fieldSpecs, methodSpecs);
        TypeSpec typeSpec = TypeSpec.classBuilder(markInfo.markClassName + IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(getPackageName(), markInfo.markClassName))
                .addFields(fieldSpecs)
                .addMethod(createDataFolderHelper())
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(mEntityClass.getPackageName(), typeSpec).build();
        javaFile.writeTo(mFiler);
    }

    /**
     * 创建createDataFolderHelper方法
     */
    private MethodSpec createDataFolderHelper() {
        return MethodSpec.methodBuilder("createDataFolderHelper")
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(ClassName.get(CODE_PACKAGE_NAME, "FileConfiguration"), "configuration")
                .returns(ClassName.get(CODE_PACKAGE_NAME, "SupportFolderCreateHelper"))
                .addStatement("return configuration.mFactory.create(configuration.context, configuration.destFileDir)")
                .build();
    }

    private void createFieldsAndMethods(List<FieldSpec> fieldSpecs, List<MethodSpec> methodSpecs) {
        for (ElegantDataMarkInfo.FieldInfo fieldInfo : markInfo.mFieldInfos) {
            String fieldName = fieldInfo.fieldName;
            if (fieldName.contains("get")) {
                fieldName = fieldName.replace("get", "m");
            }
            ClassName fieldTypeName = ClassName.get(getPackageName(), fieldInfo.fieldTypeName.toString());
            fieldSpecs.add(
                    FieldSpec
                            .builder(fieldTypeName, fieldName, Modifier.PRIVATE)
                            .build());
            methodSpecs.add(MethodSpec.methodBuilder(fieldInfo.fieldName)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(fieldTypeName)
                    .addCode("if ($N != null) {\n", fieldName)
                    .addStatement("    return $N", fieldName)
                    .addCode("} else {\n")
                    .addCode("    synchronized (this) {\n")
                    .addCode("        if ($N == null) {\n", fieldName)
                    .addCode("            $T sharedPreferences = getCreateHelper().getContext()\n", getSharedPreferences())
                    .addStatement("                    .getSharedPreferences(\"$N\", $T.MODE_PRIVATE)", mEntityClass.getSpFileName(),
                            ClassName.get("android.content", "Context"))
                    .addStatement("            $N = new $N(sharedPreferences)", fieldName, fieldInfo.fieldTypeName.toString() + IMPL_SUFFIX)
                    .addCode("        }\n")
                    .addStatement("        return $N", fieldName)
                    .addCode("    }\n")
                    .addCode("}")
                    .build()
            );
        }
    }


    /**
     * 创建get同步方法
     */
    private void createGetMethodSpec(MethodSpec.Builder builder, TypeName typeName,
                                     String getMethodImplName, String fieldName, boolean hasDef) {
        fieldName = fieldName.toUpperCase();
        builder.addAnnotation(Override.class);
        builder.returns(typeName);
        if (hasDef) {
            builder.addParameter(typeName, "defValue");
        }
        builder.addStatement("return $N(\"$N\", $N)",
                getMethodImplName,
                fieldName,
                hasDef ? "defValue" : (isEqualsString(typeName) ? "\"\"" : "0")
        );
    }

    /**
     * 创建get异步步方法
     */
    private void createGetMethodSpecAsync(MethodSpec.Builder builder, TypeName typeName, String getMethodImplName,
                                          String fieldName, boolean hasDef) {
        fieldName = fieldName.toUpperCase();
        builder.addAnnotation(Override.class);
        builder.addParameter(getPreferenceCallBack(), "callBack", Modifier.FINAL);
        if (hasDef) {
            builder.addParameter(typeName, "defValue", Modifier.FINAL);
        }
        builder.addCode("getExecutor().execute(new Runnable() {\n");
        builder.addCode("            @Override\n");
        builder.addCode("            public void run() {\n");
        builder.addStatement("$N value = $N(\"$N\", $N)", typeName.toString(), getMethodImplName, fieldName,
                hasDef ? "defValue" : (isEqualsString(typeName) ? "\"\"" : "0"));
        builder.addCode("                callBack.onSuccess(value);\n");
        builder.addCode("            }\n");
        builder.addStatement("        })");
    }

    private boolean isEqualsString(TypeName typeName) {
        return typeName.toString().equals("java.lang.String");
    }

    private String getPackageName() {
        return mEntityClass.getPackageName();
    }

    public static String toUpperFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private ClassName getSharedPreferences() {
        return ClassName.get("android.content", "SharedPreferences");
    }

    public ClassName getPreferenceCallBack() {
        return ClassName.get(CODE_PACKAGE_NAME, "OnPreferenceCallBack");
    }
}
