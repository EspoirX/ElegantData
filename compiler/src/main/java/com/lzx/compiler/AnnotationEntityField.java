package com.lzx.compiler;

import com.google.common.base.Strings;
import com.lzx.annoation.EntityClass;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.NameField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * 被 @PreferenceEntity 标记的类中变量信息获取的封装
 * create by lzx
 * 2019-05-29
 */
public class AnnotationEntityField {

    private VariableElement mVariableElement;
    private Elements mElements;
    private TypeName typeName;
    public String typeStringName;
    private String fieldName;
    private String keyName;
    private String converterPackage;
    private String converter;
    public Object value;
    private boolean isObjectField;
    private boolean hasIgnoreField;
    private boolean hasEntityClass;

    public AnnotationEntityField(VariableElement variableElement, Elements elements) throws IllegalAccessException {
        mVariableElement = variableElement;
        mElements = elements;
        IgnoreField ignoreField = variableElement.getAnnotation(IgnoreField.class);
        EntityClass entityClass = variableElement.getAnnotation(EntityClass.class);
        NameField nameField = variableElement.getAnnotation(NameField.class);

        hasIgnoreField = ignoreField != null;
        hasEntityClass = entityClass != null;

        typeName = TypeName.get(variableElement.asType());
        fieldName = variableElement.getSimpleName().toString();
        //返回被final修饰的常量的值,该值将是基本类型或字符串，如果该值是基本类型，则将其包装在适当的包装类（例如Integer）中。
        value = variableElement.getConstantValue();

        //给typeStringName赋值
        setTypeStringName();

        if (nameField != null) {
            keyName = Strings.isNullOrEmpty(nameField.value()) ? fieldName : nameField.value();
        } else {
            keyName = fieldName;
        }

        if (isObjectField) {
            //获取直接出现在这个结构上的注解; 如果没有，则为空列表
            List<? extends AnnotationMirror> list = mVariableElement.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : list) {
                TypeName mirrorTypeName = TypeName.get(annotationMirror.getAnnotationType());

                if (mirrorTypeName.equals(ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "TypeConverter"))) {
                    //返回此注释元素的值。此值是以映射的形式返回的，该映射将元素与其相应的值关联。
                    // 只包括那些注释中明确存在其值的元素，不包括那些隐式假定其默认值的元素。
                    // 映射的顺序与值出现在注释源中的顺序匹配。
                    // 注意，标记注释类型的注释镜像将被定义为有一个空映射。
                    Map<? extends ExecutableElement, ? extends AnnotationValue> maps = annotationMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : maps.entrySet()) {
                        String[] split = entry.getValue().getValue().toString().split("\\.");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < split.length - 1; i++) {
                            builder.append(split[i]).append(".");
                        }
                        converterPackage = builder.toString().substring(0, builder.toString().length() - 1);
                        converter = split[split.length - 1];
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
            // 如果不是基础类型，则是 object 类型，判断有没有被 EntityClass 和 IgnoreField 标记
            if (!hasEntityClass && !hasIgnoreField) {
                throw new IllegalAccessException(
                        String.format(
                                "Field \'%s\' can not use %s type. \nObjects should be annotated with '@EntityClass'." +
                                        "Or you should annotated with '@IgnoreField'",
                                mVariableElement.getSimpleName(), this.typeName.toString()));
            } else {
                //更改标记位
                this.typeStringName = "String";
                this.isObjectField = true;
            }
        }
    }

    TypeName getTypeName() {
        return typeName;
    }

    String getTypeStringName() {
        return typeStringName;
    }

    String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    boolean isObjectField() {
        return isObjectField;
    }

    String getKeyName() {
        return keyName;
    }

    String getConverterPackage() {
        return converterPackage;
    }

    String getConverter() {
        return converter;
    }
}
