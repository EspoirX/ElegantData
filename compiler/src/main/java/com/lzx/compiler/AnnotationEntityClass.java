package com.lzx.compiler;

import com.google.common.base.Strings;
import com.google.common.base.VerifyException;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.ElegantEntity;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @PreferenceEntity 信息封装
 * create by lzx
 * 2019-05-29
 */
public class AnnotationEntityClass {

    private TypeElement mTypeElement;
    private Elements mElements;
    private String packageName;
    private TypeName typeName;
    private String clazzName;
    private String fileName;
    private int fileType;

    private List<AnnotationEntityField> keyFields;

    AnnotationEntityClass(TypeElement typeElement, Elements elements) throws IllegalAccessException {
        mTypeElement = typeElement;
        mElements = elements;
        keyFields = new ArrayList<>();

        ElegantEntity elegantEntity = typeElement.getAnnotation(ElegantEntity.class);

        //获取基本的类信息
        getBaseClassInfo(typeElement, elegantEntity);

        //遍历类中的元素
        for (Element variable : typeElement.getEnclosedElements()) {
            //如果是变量
            if (variable instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) variable;
                IgnoreField ignoreField = variableElement.getAnnotation(IgnoreField.class);
                AnnotationEntityField entityField = new AnnotationEntityField(variableElement, mElements);
                //如果有重复的keyName,不允许
                checkFieldValidity(entityField);
                //过滤被IgnoreField修饰的变量
                if (ignoreField == null) {
                    keyFields.add(entityField);   //保存变量值
                }
            }
        }
    }

    /**
     * 如果有重复的keyName,不允许
     */
    private void checkFieldValidity(AnnotationEntityField entityField) {
        Map<String, String> checkKeyNameMap = new HashMap<>();
        if (checkKeyNameMap.get(entityField.getKeyName()) != null) {
            throw new VerifyException(
                    String.format("\'%s\' key is already used in class.", entityField.getKeyName()));
        }
        checkKeyNameMap.put(entityField.getKeyName(), entityField.getFieldName());
    }

    /**
     * 获取基本的类信息
     */
    private void getBaseClassInfo(TypeElement typeElement, ElegantEntity elegantEntity) {
        PackageElement packageElement = mElements.getPackageOf(typeElement);
        //获取包名
        packageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();
        //
        typeName = TypeName.get(typeElement.asType());
        //获取class名称
        clazzName = typeElement.getSimpleName().toString();
        //获取创建的文件名
        fileName = Strings.isNullOrEmpty(elegantEntity.fileName()) ? clazzName.toUpperCase() : elegantEntity.fileName();
        //获取文件类型
        fileType = elegantEntity.fileType();
    }

    String getPackageName() {
        return packageName;
    }

    TypeName getTypeName() {
        return typeName;
    }

    String getClazzName() {
        return clazzName;
    }

    List<AnnotationEntityField> getKeyFields() {
        return keyFields;
    }

    String getFileName() {
        return fileName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public int getFileType() {
        return fileType;
    }
}
