package com.lzx.compiler;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;
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
 * Preference具体实现类的封装
 * create by lzx
 * 2019-05-30
 */
class PreferenceGenerator {

    private static final String preferencesName = "mPreferences";
    private PreferenceEntityClass mEntityClass;

    private String[] putArray;
    private String[] getArray;
    private TypeName[] typeNameArray;
    private Filer mFiler;

    PreferenceGenerator(PreferenceEntityClass entityClass, Filer filer) {
        mEntityClass = entityClass;
        mFiler = filer;
        putArray = new String[]{"putString", "putInt", "putLong", "putFloat", "putBoolean"};
        getArray = new String[]{"getString", "getInt", "getLong", "getFloat", "getBoolean"};
        typeNameArray = new TypeName[]{TypeName.get(String.class), TypeName.INT, TypeName.LONG,
                TypeName.FLOAT, TypeName.BOOLEAN};
    }

    /**
     * 创建Dao接口类
     */
    void createPreferenceDaoInterface() throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();
        for (PreferenceEntityField field : fieldList) {
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
            TypeName typeName = field.getTypeName();
            String putMethodName = "put" + fieldName;
            String putAsyncMethodName = "put" + fieldName + "Async";
            String getMethodName = "get" + fieldName;
            String getAsyncMethodName = "get" + fieldName + "Async";
            String removeMethodName = "remove" + fieldName;
            String containsMethodName = "contains" + fieldName;
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
            if (!field.isObjectField()) {
                methodSpecs.add(MethodSpec.methodBuilder(getMethodName)
                        .addModifiers(ABSTRACT, PUBLIC)
                        .addParameter(typeName, "defValue")
                        .returns(typeName)
                        .build());
                methodSpecs.add(MethodSpec.methodBuilder(getAsyncMethodName)
                        .addModifiers(ABSTRACT, PUBLIC)
                        .addParameter(getDataCallBack(), "callBack")
                        .addParameter(typeName, "defValue")
                        .build());
            }
            methodSpecs.add(MethodSpec.methodBuilder(getAsyncMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .addParameter(getDataCallBack(), "callBack")
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(removeMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .returns(TypeName.BOOLEAN)
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(containsMethodName)
                    .addModifiers(ABSTRACT, PUBLIC)
                    .returns(TypeName.BOOLEAN)
                    .build());
        }
        methodSpecs.add(MethodSpec.methodBuilder("clear")
                .addModifiers(ABSTRACT, PUBLIC)
                .returns(TypeName.BOOLEAN)
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
                .addParameter(GeneratorHelper.getSharedPreferences(), "sharedPreferences")
                .addStatement("$N = sharedPreferences", preferencesName)
                .addStatement("mDispatcher = new DataDispatcher()")
                .build());
        methodSpecs.addAll(createPutAndGetMethod());
        methodSpecs.addAll(createObjectPutAndGet());
        methodSpecs.addAll(createRemoveAndContains());

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

        TypeSpec typeSpec = TypeSpec.classBuilder(mEntityClass.getClazzName() + GeneratorHelper.IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(mEntityClass.getTypeName())
                .addField(GeneratorHelper.getSharedPreferences(), preferencesName, Modifier.PRIVATE)
                .addField(ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "DataDispatcher"), "mDispatcher", PRIVATE)
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
                .addModifiers(PRIVATE)
                .returns(TypeName.BOOLEAN)
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
                .addModifiers(PRIVATE)
                .returns(valueTypeName)
                .addParameter(TypeName.get(String.class), "key")
                .addParameter(valueTypeName, "defValue")
                .addStatement("return $N.$N(key, defValue)", preferencesName, methodName)
                .build();
    }

    /**
     * 创建remove 和 clear 封装方法
     */
    private MethodSpec createRemoveAndClearImplMethod(boolean isRemove) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(isRemove ? "remove" : "clear").returns(TypeName.BOOLEAN);
        if (isRemove) {
            builder.addParameter(TypeName.get(String.class), "key");
            builder.addModifiers(PRIVATE);
        } else {
            builder.addAnnotation(Override.class);
            builder.addModifiers(PUBLIC);
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
                .addModifiers(PRIVATE)
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
            if (field.isObjectField()) {
                continue;
            }
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
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
     * 创建 Object 方法
     */
    private List<MethodSpec> createObjectPutAndGet() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();
        for (PreferenceEntityField keyField : fieldList) {
            if (keyField.isObjectField()) {
                ClassName converterClazz = ClassName.get(keyField.getConverterPackage(), keyField.getConverter());
                ClassName encryptionClazz = ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "AESEncryption");
                String typeName = keyField.getTypeName().box().toString();

                System.out.println("converterClazz = " + converterClazz + "  getConverter = " + keyField.getConverter() + " typeName = " + typeName);

                String upperKeyName = GeneratorHelper.toUpperFirstChar(keyField.getKeyName());
                String putMethodName = "put" + upperKeyName;
                String putAsyncMethodName = putMethodName + "Async";
                String getMethodName = "get" + upperKeyName;
                String getAsyncMethodName = getMethodName + "Async";

                methodSpecs.add(MethodSpec.methodBuilder(putMethodName)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(keyField.getTypeName(), keyField.getKeyName())
                        .addStatement("$T parser = new $N($N.class)", converterClazz, keyField.getConverter(), typeName)
                        .addStatement("String json = parser.convertObject($N)", keyField.getKeyName())
                        .addStatement("putString(\"$N\",$T.encrypt(json,\"1234567890ABCDFG\"))",
                                keyField.getKeyName().toUpperCase(), encryptionClazz)
                        .build());
                methodSpecs.add(MethodSpec.methodBuilder(putAsyncMethodName)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(keyField.getTypeName(), keyField.getKeyName(), FINAL)
                        .addCode("    getExecutor().execute(new Runnable() {\n")
                        .addCode("        @Override\n")
                        .addCode("        public void run() {\n")
                        .addStatement("$N($N)", putMethodName, keyField.getKeyName())
                        .addCode("        }\n")
                        .addCode("    });\n")
                        .build());
                methodSpecs.add(MethodSpec.methodBuilder(getMethodName)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .returns(keyField.getTypeName())
                        .addStatement("$T parser = new $N($N.class)", converterClazz, keyField.getConverter(), typeName)
                        .addStatement("String json = getString(\"$N\", \"\")", keyField.getKeyName().toUpperCase())
                        .addCode("if (\"\".equals(json)) {\n")
                        .addStatement("    return new $T()", keyField.getTypeName())
                        .addCode("} else {\n")
                        .addCode("    try {\n")
                        .addStatement("return parser.onParse(AESEncryption.decrypt(json, \"\", \"1234567890ABCDFG\"))")
                        .addCode("    } catch (Exception e) {\n")
                        .addStatement("        e.printStackTrace()")
                        .addCode("    }\n")
                        .addCode("}\n")
                        .addStatement("return new $T()", keyField.getTypeName())
                        .build());
                methodSpecs.add(MethodSpec.methodBuilder(getAsyncMethodName)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(getDataCallBack(), "callBack", FINAL)
                        .addCode("    getExecutor().execute(new Runnable() {\n")
                        .addCode("        @Override\n")
                        .addCode("        public void run() {\n")
                        .addStatement("$T value = $N()", keyField.getTypeName(), getMethodName)
                        .addStatement("callBack.onSuccess(value)")
                        .addCode("        }\n")
                        .addCode("    });\n")
                        .build());
            }
        }
        return methodSpecs;
    }

    private List<MethodSpec> createRemoveAndContains() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();
        for (PreferenceEntityField field : fieldList) {
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
            String removeMethodName = "remove" + fieldName;
            String containsMethodName = "contains" + fieldName;
            methodSpecs.add(MethodSpec.methodBuilder(removeMethodName)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .returns(TypeName.BOOLEAN)
                    .addStatement("return remove(\"$N\")", fieldName.toUpperCase())
                    .build());
            methodSpecs.add(MethodSpec.methodBuilder(containsMethodName)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .returns(TypeName.BOOLEAN)
                    .addStatement("return contains(\"$N\")", fieldName.toUpperCase())
                    .build());
        }
        return methodSpecs;
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
                hasDef ? "defValue" : (GeneratorHelper.isEqualsString(typeName) ? "\"\"" : "0")
        );
    }

    /**
     * 创建get异步步方法
     */
    private void createGetMethodSpecAsync(MethodSpec.Builder builder, TypeName typeName, String getMethodImplName,
                                          String fieldName, boolean hasDef) {
        fieldName = fieldName.toUpperCase();
        builder.addAnnotation(Override.class);
        builder.addParameter(getDataCallBack(), "callBack", Modifier.FINAL);
        if (hasDef) {
            builder.addParameter(typeName, "defValue", Modifier.FINAL);
        }
        builder.addCode("getExecutor().execute(new Runnable() {\n");
        builder.addCode("            @Override\n");
        builder.addCode("            public void run() {\n");
        builder.addStatement("$N value = $N(\"$N\", $N)", typeName.toString(), getMethodImplName, fieldName,
                hasDef ? "defValue" : (GeneratorHelper.isEqualsString(typeName) ? "\"\"" : "0"));
        builder.addCode("                callBack.onSuccess(value);\n");
        builder.addCode("            }\n");
        builder.addStatement("        })");
    }

    private String getPackageName() {
        return mEntityClass.getPackageName();
    }

    private ClassName getDataCallBack() {
        return ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "OnDataCallBack");
    }
}
