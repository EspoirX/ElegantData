package com.lzx.compiler;

import com.google.common.base.Strings;
import com.lzx.annoation.Embedded;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.PreferenceEntity;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.util.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * PreferenceEntity 对类信息获取的封装
 * create by lzx
 * 2019-05-29
 */
public class PreferenceEntityClass {

    private TypeElement mTypeElement;
    private Elements mElements;

    private String packageName;
    private TypeName typeName;
    private String clazzName;
    private String spFileName;

    public PreferenceEntityClass(TypeElement typeElement, Elements elements) {
        mTypeElement = typeElement;
        mElements = elements;
        PreferenceEntity preferenceEntity = typeElement.getAnnotation(PreferenceEntity.class);
        //获取基本的类信息
        getBaseClassInfo(typeElement, preferenceEntity);

        //遍历类中的元素
        for (Element variable : typeElement.getEnclosedElements()) {
            //如果是变量
            if (variable instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) variable;
                PreferenceEntityField entityField = new PreferenceEntityField(variableElement, mElements);
            }
        }

    }

    /**
     * 获取基本的类信息
     */
    private void getBaseClassInfo(TypeElement typeElement, PreferenceEntity preferenceEntity) {
        PackageElement packageElement = mElements.getPackageOf(typeElement);
        //获取包名
        packageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();
        //
        typeName = TypeName.get(typeElement.asType());
        //获取class名称
        clazzName = typeElement.getSimpleName().toString();
        //获取PreferenceEntity注解上的值
        spFileName = Strings.isNullOrEmpty(preferenceEntity.fileName())
                ? StringUtils.toUpperCase(clazzName) : preferenceEntity.fileName();
    }
}
