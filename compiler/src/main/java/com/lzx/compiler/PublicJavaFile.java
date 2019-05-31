package com.lzx.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.Modifier.SYNCHRONIZED;

/**
 * create by lzx
 * 2019-05-31
 */
public class PublicJavaFile {

    /**
     * 创建DataAction接口
     */
    public static void createDataAction(String packageName, Filer filer) throws IOException {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        methodSpecList.add(createPutMethod("putString", TypeName.get(String.class)));
        methodSpecList.add(createPutMethod("putInt", TypeName.INT));
        methodSpecList.add(createPutMethod("putLong", TypeName.LONG));
        methodSpecList.add(createPutMethod("putFloat", TypeName.FLOAT));
        methodSpecList.add(createPutMethod("putBoolean", TypeName.BOOLEAN));

        methodSpecList.add(createGetMethod("getString", TypeName.get(String.class)));
        methodSpecList.add(createGetMethod("getInt", TypeName.INT));
        methodSpecList.add(createGetMethod("getLong", TypeName.LONG));
        methodSpecList.add(createGetMethod("getFloat", TypeName.FLOAT));
        methodSpecList.add(createGetMethod("getBoolean", TypeName.BOOLEAN));

        TypeSpec typeSpec = TypeSpec.interfaceBuilder("IDataAction")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecList)
                .addMethod(createContainsAndRemoveMethod("contains"))
                .addMethod(createContainsAndRemoveMethod("remove"))
                .addMethod(createClearMethod())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        javaFile.writeTo(filer);
    }

    private static MethodSpec createPutMethod(String methodName, TypeName valueTypeName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, ABSTRACT)
                .addParameter(TypeName.get(String.class), "key")
                .addParameter(valueTypeName, "value")
                .returns(TypeName.BOOLEAN)
                .build();
    }

    private static MethodSpec createGetMethod(String methodName, TypeName valueTypeName) {
        ParameterSpec key = ParameterSpec
                .builder(TypeName.get(String.class), "key")
                .build();
        ParameterSpec value = ParameterSpec.builder(valueTypeName, "defValue")
                .build();
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, ABSTRACT)
                .addParameter(key)
                .addParameter(value)
                .returns(valueTypeName)
                .build();
    }

    private static MethodSpec createContainsAndRemoveMethod(String methodName) {
        ParameterSpec key = ParameterSpec
                .builder(TypeName.get(String.class), "key")
                .build();
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, ABSTRACT)
                .addParameter(key)
                .returns(TypeName.BOOLEAN)
                .build();
    }

    private static MethodSpec createClearMethod() {
        return MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC, ABSTRACT)
                .returns(TypeName.BOOLEAN)
                .build();
    }

    /**
     * 创建线程类
     */
    public static void createDispatcher(String packageName, Filer filer) throws IOException {
        TypeSpec typeSpec = TypeSpec.classBuilder("DataDispatcher")
                .addModifiers(PUBLIC)
                .addField(TypeName.get(ExecutorService.class), "executorService", PRIVATE)
                .addMethod(createDispatcherConstructor(false))
                .addMethod(createDispatcherConstructor(true))
                .addMethod(createExecutorService())
                .addMethod(createThreadFactory())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        javaFile.writeTo(filer);
    }

    private static MethodSpec createDispatcherConstructor(boolean isAddParameter) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(PUBLIC);
        if (isAddParameter) {
            builder.addParameter(TypeName.get(ExecutorService.class), "executorService");
            builder.addStatement(
                    "this.$N = $N",
                    "executorService",
                    "executorService"
            );
        }
        return builder.build();
    }

    private static MethodSpec createExecutorService() {
        ClassName threadPoolExecutor = ClassName.get("java.util.concurrent", "ThreadPoolExecutor");
        ClassName timeUnit = ClassName.get("java.util.concurrent", "TimeUnit");
        ClassName synchronousQueue = ClassName.get("java.util.concurrent", "SynchronousQueue");
        return MethodSpec.methodBuilder("executorService")
                .addModifiers(SYNCHRONIZED)
                .returns(TypeName.get(ExecutorService.class))
                .addModifiers(PUBLIC)
                .addStatement(
                        "if (executorService == null) {\n" +
                                "   executorService = new $T(0, Integer.MAX_VALUE, 60, $T.SECONDS,\n" +
                                "        new $T<Runnable>(), threadFactory(\"Data Dispatcher\", false));\n" +
                                "   }\n" +
                                "return executorService",
                        threadPoolExecutor,
                        timeUnit,
                        synchronousQueue
                ).build();
    }

    private static MethodSpec createThreadFactory() {
        ClassName threadFactory = ClassName.get("java.util.concurrent", "ThreadFactory");
        return MethodSpec.methodBuilder("threadFactory")
                .addModifiers(PRIVATE, STATIC)
                .addParameter(TypeName.get(String.class), "name", FINAL)
                .addParameter(TypeName.BOOLEAN, "daemon", FINAL)
                .returns(threadFactory)
                .addStatement(
                        "return new $T() {\n" +
                                "            @Override\n" +
                                "            public Thread newThread(Runnable runnable) {\n" +
                                "                Thread result = new Thread(runnable, name);\n" +
                                "                result.setDaemon(daemon);\n" +
                                "                return result;\n" +
                                "            }\n" +
                                "        }",
                        threadFactory
                ).build();
    }

    /**
     * 创建PreferenceManager
     */
    public static void createPreferenceManager(String packageName, Filer filer) throws IOException {
        ClassName dataAction = ClassName.get(packageName, "IDataAction");
        ClassName sharedPreferences = ClassName.get("android.content", "SharedPreferences");

        List<MethodSpec> putImplList = new ArrayList<>();
        putImplList.add(createPutImplMethod("putString", TypeName.get(String.class)));
        putImplList.add(createPutImplMethod("putInt", TypeName.INT));
        putImplList.add(createPutImplMethod("putLong", TypeName.LONG));
        putImplList.add(createPutImplMethod("putFloat", TypeName.FLOAT));
        putImplList.add(createPutImplMethod("putBoolean", TypeName.BOOLEAN));

        putImplList.add(createGetImplMethod("getString", TypeName.get(String.class)));
        putImplList.add(createGetImplMethod("getInt", TypeName.INT));
        putImplList.add(createGetImplMethod("getLong", TypeName.LONG));
        putImplList.add(createGetImplMethod("getFloat", TypeName.FLOAT));
        putImplList.add(createGetImplMethod("getBoolean", TypeName.BOOLEAN));

        TypeSpec typeSpec = TypeSpec.classBuilder("PreferenceManager")
                .addModifiers(PUBLIC)
                .addSuperinterface(dataAction)
                .addField(sharedPreferences, "preference", PRIVATE, FINAL)
                .addMethod(createPreferenceConstructor())
                .addMethods(putImplList)
                .addMethod(createRemoveAndClearImplMethod(true))
                .addMethod(createRemoveAndClearImplMethod(false))
                .addMethod(createContainsImplMethod())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        javaFile.writeTo(filer);
    }

    private static MethodSpec createPreferenceConstructor() {
        ClassName context = ClassName.get("android.content", "Context");
        return MethodSpec.constructorBuilder()
                .addParameter(context, "context")
                .addParameter(TypeName.get(String.class), "spFileName")
                .addStatement(
                        "this.preference = context.getSharedPreferences(spFileName, Context.MODE_PRIVATE)"
                ).build();
    }

    private static MethodSpec createPutImplMethod(String methodName, TypeName valueTypeName) {
        TypeName stringType = TypeName.get(String.class);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addParameter(stringType, "key")
                .addParameter(valueTypeName, "value")
                .addStatement("SharedPreferences.Editor editor = preference.edit()");
        if (isEqualsString(valueTypeName)) {
            builder.addStatement("editor.putString(key, value)");
        }
        if (valueTypeName == TypeName.INT) {
            builder.addStatement("editor.putInt(key, value)");
        }
        if (valueTypeName == TypeName.LONG) {
            builder.addStatement("editor.putLong(key, value)");
        }
        if (valueTypeName == TypeName.FLOAT) {
            builder.addStatement("editor.putFloat(key, value)");
        }
        if (valueTypeName == TypeName.BOOLEAN) {
            builder.addStatement("editor.putBoolean(key, value)");
        }
        builder.addStatement("return editor.commit()");
        return builder.build();
    }

    private static MethodSpec createGetImplMethod(String methodName, TypeName valueTypeName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(valueTypeName)
                .addParameter(TypeName.get(String.class), "key")
                .addParameter(valueTypeName, "defValue")
                .addStatement("return preference.$N(key, defValue)", methodName)
                .build();
    }

    private static MethodSpec createRemoveAndClearImplMethod(boolean isRemove) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(isRemove ? "remove" : "clear")
                .addModifiers(PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addAnnotation(Override.class);
        if (isRemove) {
            builder.addParameter(TypeName.get(String.class), "key");
        }
        builder
                .addStatement("SharedPreferences.Editor editor = preference.edit()")
                .addStatement(isRemove ? "editor.remove(key)" : "editor.clear()")
                .addStatement("return editor.commit()");
        return builder.build();
    }

    private static MethodSpec createContainsImplMethod() {
        return MethodSpec.methodBuilder("contains")
                .addModifiers(PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(String.class), "key")
                .addStatement("return preference.contains(key)")
                .build();
    }

    public static void createPreferenceCallBack(String packageName, Filer filer) throws IOException {
        TypeSpec typeSpec = TypeSpec.interfaceBuilder("OnPreferenceCallBack")
                .addTypeVariable(TypeVariableName.get("T"))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(createCallBackMethod())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        javaFile.writeTo(filer);
    }

    private static MethodSpec createCallBackMethod() {
        return MethodSpec
                .methodBuilder("onSuccess")
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(TypeVariableName.get("T"), "data")
                .build();
    }


    private static boolean isEqualsString(TypeName typeName) {
        return typeName.toString().equals("java.lang.String");
    }
}
