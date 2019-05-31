package com.lzx.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * create by lzx
 * 2019-05-30
 */
public class PreferenceGenerator {

    private PreferenceEntityClass mEntityClass;
    private Elements mElements;

    public PreferenceGenerator(PreferenceEntityClass entityClass, Elements elements) {
        mEntityClass = entityClass;
        mElements = elements;
    }

    public TypeSpec getTypeSpec() {
        ClassName preferenceManager = ClassName.get(getPackageName(), "PreferenceManager");
        ClassName dataDispatcher = ClassName.get(getPackageName(), "DataDispatcher");
        return TypeSpec.classBuilder(getClazzName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(mEntityClass.getTypeName()) //继承类
                .addField(preferenceManager, "mPreferenceManager", Modifier.PRIVATE)
                .addField(dataDispatcher, "mDispatcher", Modifier.PRIVATE)
                .addField(getImplFileClass(), "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(createConstructorMethod())
                .addMethod(createInstanceMethod())
                .addMethod(createExecutor())
                .addMethods(createPutAndGetMethod())
                .build();
    }

    /**
     * 创建构造方法
     */
    private MethodSpec createConstructorMethod() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(getContextType(), "context")
                .addStatement("mPreferenceManager = new PreferenceManager(context, \"$N\")", mEntityClass.getSpFileName())
                .addStatement("mDispatcher = new DataDispatcher()")
                .build();
    }

    /**
     * 创建单例模式
     */
    private MethodSpec createInstanceMethod() {
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(getContextType(), "context")
                .addStatement("if($N != null) return $N", "instance", "instance")
                .addStatement("$N = new $N($N)", "instance", getClazzName(), "context")
                .addStatement("return $N", "instance")
                .returns(getImplFileClass())
                .build();
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
     * 创建 put 和 get 方法
     */
    private List<MethodSpec> createPutAndGetMethod() {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        List<PreferenceEntityField> fieldList = mEntityClass.getKeyFields();
        for (PreferenceEntityField field : fieldList) {
            String fieldName = toUpperFirstChar(field.getFieldName());
            TypeName typeName = field.getTypeName();
            String typeNameString = field.getTypeStringName();
            //put
            MethodSpec.Builder putBuilder = MethodSpec.methodBuilder("put" + fieldName);
            putBuilder.addModifiers(Modifier.PUBLIC);
            createPutMethodSpec(putBuilder, typeName, "put" + typeNameString, fieldName);
            MethodSpec putModel = putBuilder.build();
            //getSync 有 defValue
            MethodSpec.Builder getSyncBuilderDef = MethodSpec.methodBuilder("get" + fieldName + "Sync");
            getSyncBuilderDef.addModifiers(Modifier.PUBLIC);
            createGetSyncMethodSpec(getSyncBuilderDef, typeName, "get" + typeNameString, fieldName, true);
            MethodSpec getSyncModelDef = getSyncBuilderDef.build();
            //getSync 没 defValue
            MethodSpec.Builder getSyncBuilder = MethodSpec.methodBuilder("get" + fieldName + "Sync");
            getSyncBuilder.addModifiers(Modifier.PUBLIC);
            createGetSyncMethodSpec(getSyncBuilder, typeName, "get" + typeNameString, fieldName, false);
            MethodSpec getSyncModel = getSyncBuilder.build();
            //get 有 defValue
            MethodSpec.Builder getBuilderDef = MethodSpec.methodBuilder("get" + fieldName);
            getBuilderDef.addModifiers(Modifier.PUBLIC);
            createGetMethodSpec(getBuilderDef, typeName, "get" + typeNameString, true);
            MethodSpec getModelDef = getBuilderDef.build();
            //get 没 defValue
            MethodSpec.Builder getBuilder = MethodSpec.methodBuilder("get" + fieldName);
            getBuilder.addModifiers(Modifier.PUBLIC);
            createGetMethodSpec(getBuilder, typeName, "get" + typeNameString, false);
            MethodSpec getModel = getBuilder.build();

            methodSpecList.add(putModel);
            methodSpecList.add(getSyncModelDef);
            methodSpecList.add(getSyncModel);
            methodSpecList.add(getModel);
            methodSpecList.add(getModelDef);
        }
        return methodSpecList;
    }

    /**
     * 创建put方法
     */
    private void createPutMethodSpec(MethodSpec.Builder builder, TypeName typeName, String putName, String fieldName) {
        builder.addParameter(typeName, "value", Modifier.FINAL);
        builder.addStatement(
                "getExecutor().execute(new Runnable() {\n" +
                        "            @Override\n" +
                        "            public void run() {\n" +
                        "                mPreferenceManager.$N(\"$N\", value);\n" +
                        "            }\n" +
                        "        })",
                putName,
                fieldName.toUpperCase()
        );
    }

    /**
     * 创建get同步方法
     */
    private void createGetSyncMethodSpec(MethodSpec.Builder builder, TypeName typeName,
                                         String getName, String fieldName, boolean hasDef) {
        builder.returns(typeName);
        if (hasDef) {
            builder.addParameter(typeName, "defValue");
        }
        builder.addStatement("return mPreferenceManager.$N(\"$N\", $N)",
                getName,
                fieldName.toUpperCase(),
                hasDef ? "defValue" : (isEqualsString(typeName) ? "\"\"" : "0")
        );
    }

    /**
     * 创建get异步步方法
     */
    private void createGetMethodSpec(MethodSpec.Builder builder, TypeName typeName, String getName, boolean hasDef) {
        ClassName callback = ClassName.get(getPackageName(), "OnPreferenceCallBack");
        builder.addParameter(callback, "callBack", Modifier.FINAL);
        if (hasDef) {
            builder.addParameter(typeName, "defValue", Modifier.FINAL);
        }
        builder.addStatement(
                "getExecutor().execute(new Runnable() {\n" +
                        "            @Override\n" +
                        "            public void run() {\n" +
                        "                $N value = mPreferenceManager.$N(name, $N);\n" +
                        "                callBack.onSuccess(value);\n" +
                        "            }\n" +
                        "        })",
                typeName.toString(),
                getName,
                hasDef ? "defValue" : (isEqualsString(typeName) ? "\"\"" : "0")
        );
    }

    private boolean isEqualsString(TypeName typeName) {
        return typeName.toString().equals("java.lang.String");
    }

    private ClassName getContextType() {
        return ClassName.get("android.content", "Context");
    }

    private ClassName getImplFileClass() {
        return ClassName.get(mEntityClass.getPackageName(), getClazzName());
    }

    private String getClazzName() {
        return mEntityClass.getSpFileName() + "Impl";
    }

    private String getPackageName() {
        return mEntityClass.getPackageName();
    }

    private static String toUpperFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
