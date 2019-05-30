package com.lzx.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * create by lzx
 * 2019-05-30
 */
public class PreferenceGenerator {


    private static final String FIELD_INSTANCE = "instance";
    private static final String CONSTRUCTOR_CONTEXT = "context";

    private static final String FIELD_PREFERENCE = "preference";

    private static final String EDIT_METHOD = "edit()";
    private static final String CLEAR_METHOD = "clear()";
    private static final String APPLY_METHOD = "apply()";

    private static final String PACKAGE_CONTEXT = "android.content.Context";
    private static final String PACKAGE_SHAREDPREFERENCE = "android.content.SharedPreferences";
    private static final String PACKAGE_PREFERENCEMANAGER = "android.preference.PreferenceManager";

    private PreferenceEntityClass mEntityClass;
    private Elements mElements;

    public PreferenceGenerator(PreferenceEntityClass entityClass, Elements elements) {
        mEntityClass = entityClass;
        mElements = elements;
    }

    public TypeSpec getTypeSpec() {
        return TypeSpec.classBuilder(mEntityClass.getSpFileName() + "Impl")
                .addModifiers(Modifier.PUBLIC)
                .superclass(mEntityClass.getTypeName()) //继承抽象类
                .addMethod(getConstructorMethod())
                .addMethod(getInstanceMethod())
                .build();
    }

    /**
     * 创建构造方法
     *
     * @return
     */
    private MethodSpec getConstructorMethod() {
        ParameterSpec parameterSpec = ParameterSpec.builder(getContextPackageType(), CONSTRUCTOR_CONTEXT)
                .addAnnotation(Nonnull.class)
                .build();
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(parameterSpec)
                .addStatement(
                        "this.$N = $N.getSharedPreferences($S, Context.MODE_PRIVATE)",
                        FIELD_PREFERENCE,
                        CONSTRUCTOR_CONTEXT,
                        mEntityClass.getSpFileName()
                ).build();
    }

    /**
     * 创建单例模式
     */
    private MethodSpec getInstanceMethod() {
        ParameterSpec parameterSpec = ParameterSpec.builder(getContextPackageType(), CONSTRUCTOR_CONTEXT)
                .addAnnotation(Nonnull.class)
                .build();
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(parameterSpec)
                .addStatement("if($N != null) return $N", FIELD_INSTANCE, FIELD_INSTANCE)
                .addStatement("$N = new $N($N)", FIELD_INSTANCE, getClazzName(), CONSTRUCTOR_CONTEXT)
                .addStatement("return $N", FIELD_INSTANCE)
                .build();
    }


    private TypeName getContextPackageType() {
        return TypeName.get(mElements.getTypeElement(PACKAGE_CONTEXT).asType());
    }

    private TypeName getSharedPreferencesPackageType() {
        return TypeName.get(mElements.getTypeElement(PACKAGE_SHAREDPREFERENCE).asType());
    }

    private TypeName getPreferenceManagerPackageType() {
        return TypeName.get(mElements.getTypeElement(PACKAGE_PREFERENCEMANAGER).asType());
    }

    private String getClazzName() {
        return mEntityClass.getSpFileName() + "Impl";
    }
}
