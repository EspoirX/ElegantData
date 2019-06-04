package com.lzx.compiler;

import com.google.common.base.Strings;
import com.lzx.annoation.Embedded;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.NameField;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * 对类中变量信息获取的封装
 * create by lzx
 * 2019-05-29
 */
public class PreferenceEntityField {

    private VariableElement mVariableElement;
    private Elements mElements;
    private Messager mMessager;

    private String packageName;
    private TypeName typeName;
    public String typeStringName;
    private String fieldName;
    private String keyName;
    public Object value;
    private boolean isObjectField;
    private boolean hasIgnoreField;
    private boolean hasEmbedded;

    public PreferenceEntityField(VariableElement variableElement, Elements elements, Messager messager) throws IllegalAccessException {
        mVariableElement = variableElement;
        mElements = elements;
        mMessager = messager;
        IgnoreField ignoreField = variableElement.getAnnotation(IgnoreField.class);
        Embedded embedded = variableElement.getAnnotation(Embedded.class);
        NameField nameField = variableElement.getAnnotation(NameField.class);

        hasIgnoreField = ignoreField != null;
        hasEmbedded = embedded != null;

        PackageElement packageElement = mElements.getPackageOf(variableElement);
        packageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();
        typeName = TypeName.get(variableElement.asType());
        fieldName = variableElement.getSimpleName().toString();
        //返回被final修饰的常量的值,该值将是基本类型或字符串，如果该值是基本类型，则将其包装在适当的包装类（例如Integer）中。
        value = variableElement.getConstantValue();

        //给typeStringName赋值
        setTypeStringName();

        if (nameField != null) {
            keyName = Strings.isNullOrEmpty(nameField.value())
                    ? fieldName.toUpperCase()
                    : nameField.value();
        } else {
            keyName = fieldName.toUpperCase();
        }

        if (isObjectField) {
            //获取直接出现在这个结构上的注解; 如果没有，则为空列表
            List<? extends AnnotationMirror> list = mVariableElement.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : list) {
                TypeName mirrorTypeName = TypeName.get(annotationMirror.getAnnotationType());
                if (mirrorTypeName.equals(TypeName.get(Embedded.class))) {
                    //返回此注释元素的值。此值是以映射的形式返回的，该映射将元素与其相应的值关联。
                    // 只包括那些注释中明确存在其值的元素，不包括那些隐式假定其默认值的元素。
                    // 映射的顺序与值出现在注释源中的顺序匹配。
                    // 注意，标记注释类型的注释镜像将被定义为有一个空映射。
                    Map<? extends ExecutableElement, ? extends AnnotationValue> maps
                            = annotationMirror.getElementValues();

                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : maps.entrySet()) {
                        showLog("大妈蛋 = " + entry.getValue().getValue());
                    }
                }
            }
        }

        //过滤被 IgnoreField 修饰的变量
        if (!hasIgnoreField) {
            //不允许 private 修饰变量
            if (variableElement.getModifiers().contains(Modifier.PRIVATE)) {
                throw new IllegalAccessException(
                        String.format("Field \'%s\' should not be private.", variableElement.getSimpleName()));
            }
        }
    }

    private void setTypeStringName() throws IllegalAccessException {
        if (!hasIgnoreField) {
            if (this.typeName.equals(TypeName.BOOLEAN)) {
                this.typeStringName = "Boolean";
            } else if (this.typeName.equals(TypeName.INT)) {
                this.typeStringName = "Int";
            } else if (this.typeName.equals(TypeName.FLOAT)) {
                this.typeStringName = "Float";
            } else if (this.typeName.equals(TypeName.LONG)) {
                this.typeStringName = "Long";
            } else if (this.typeName.equals(TypeName.get(String.class))) {
                this.typeStringName = "String";
            } else {
                //如果不是基础类型，则是object类型，判断有没有被Embedded修饰
                // if (!hasEmbedded) {
                //     throw new IllegalAccessException(
                //             String.format(
                //                     "Field \'%s\' can not use %s type. \nObjects should be annotated with '@Embedded'.",
                //                     mVariableElement.getSimpleName(), this.typeName.toString()));
                // } else {
                //     //更改标记位
                //     this.typeStringName = "String";
                //     this.isObjectField = true;
                // }
            }
        }
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public String getTypeStringName() {
        return typeStringName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public boolean isObjectField() {
        return isObjectField;
    }

    public String getKeyName() {
        return keyName;
    }

    private void showErrorLog(String message, Element element) {
        mMessager.printMessage(ERROR, "Error:" + message, element);
    }


    private void showLog(String message) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
