package com.lzx.compiler;

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

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Preference具体实现类的封装
 * create by lzx
 * 2019-05-30
 */
class ElegantDataGenerator {

    private static final String preferencesName = "mPreferences";
    private AnnotationEntityClass mEntityClass;

    private String[] putArray;
    private String[] getArray;
    private TypeName[] typeNameArray;
    private Filer mFiler;

    ElegantDataGenerator(AnnotationEntityClass entityClass, Filer filer) {
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
        List<AnnotationEntityField> fieldList = mEntityClass.getKeyFields();
        for (AnnotationEntityField field : fieldList) {
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
            TypeName typeName = field.getTypeName();
            String putMethodName = "put" + fieldName;
            String getMethodName = "get" + fieldName;
            String removeMethodName = "remove" + fieldName;
            String containsMethodName = "contains" + fieldName;
            methodSpecs.add(MethodSpec.methodBuilder(putMethodName)
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
            }
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

    void createFileDaoInterface() throws IOException {
        createPreferenceDaoInterface();
    }

    /**
     * 创建Dao实现类
     */
    void createPreferenceDaoImpl() throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(GeneratorHelper.getSharedPreferences(), "sharedPreferences")
                .addStatement("$N = sharedPreferences", preferencesName)
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

        TypeSpec typeSpec = TypeSpec.classBuilder(mEntityClass.getClazzName() + GeneratorHelper.IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(mEntityClass.getTypeName())
                .addField(GeneratorHelper.getSharedPreferences(), preferencesName, Modifier.PRIVATE)
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(mEntityClass.getPackageName(), typeSpec).build();
        javaFile.writeTo(mFiler);
    }

    void createFileDaoImpl() throws IOException {
        ClassName folderCreateHelper = ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "IFolderCreateHelper");
        List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(folderCreateHelper, "createHelper")
                .addModifiers(PUBLIC)
                .addStatement("this.createHelper = createHelper")
                .addStatement("folder = this.createHelper.getFileDirectoryPath()")
                .build());
        methodSpecs.add(createWriteDataToFile());
        methodSpecs.add(createReadDataFromFile());
        //创建put
        for (int i = 0; i < putArray.length; i++) {
            methodSpecs.add(createFilePutImplMethod(putArray[i], typeNameArray[i]));
        }
        //创建get
        for (int i = 0; i < getArray.length; i++) {
            methodSpecs.add(createFileGetImplMethod(getArray[i], typeNameArray[i]));
        }
        TypeSpec typeSpec = TypeSpec.classBuilder(mEntityClass.getClazzName() + GeneratorHelper.IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(mEntityClass.getTypeName())
                .addField(folderCreateHelper, "createHelper")
                .addField(TypeName.get(String.class), "folder")
                .addMethods(methodSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(mEntityClass.getPackageName(), typeSpec).build();
        javaFile.writeTo(mFiler);
    }


    private MethodSpec createWriteDataToFile() {
        TypeName stringType = TypeName.get(String.class);
        return MethodSpec.methodBuilder("writeDataToFile")
                .addModifiers(PRIVATE)
                .addParameter(stringType, "folder")
                .addParameter(stringType, "fileName")
                .addParameter(stringType, "data")
                .addStatement("$T writer = null", getIOClass("BufferedWriter"))
                .addStatement("$T fileWriter = null", getIOClass("FileWriter"))
                .addCode("try {\n")
                .addStatement("$T newFile = new File(folder, fileName)", getIOClass("File"))
                .addStatement("fileWriter = new FileWriter(newFile, false)")
                .addStatement("writer = new $T(fileWriter)", getIOClass("BufferedWriter"))
                .addStatement("writer.write(data)")
                .addStatement("writer.flush()")
                .addCode("} catch ($T e) {\n", getIOClass("FileNotFoundException"))
                .addStatement("e.printStackTrace()")
                .addCode("} catch ($T e) {\n", getIOClass("IOException"))
                .addStatement("e.printStackTrace()")
                .addCode("} finally {\n")
                .addCode("if (writer != null) {\n")
                .addCode("try {\n")
                .addStatement("fileWriter.close()")
                .addStatement("writer.close()")
                .addCode("} catch (IOException e) {\n")
                .addStatement("e.printStackTrace()")
                .addCode("}\n")
                .addCode("}\n")
                .addCode("}\n")
                .build();
    }

    private MethodSpec createReadDataFromFile() {
        TypeName stringType = TypeName.get(String.class);
        return MethodSpec.methodBuilder("readDataFromFile")
                .addModifiers(PRIVATE)
                .addParameter(stringType, "folder")
                .addParameter(stringType, "fileName")
                .returns(stringType)
                .addStatement("StringBuilder stringBuilder = new StringBuilder()")
                .addStatement("$T reader", getIOClass("BufferedReader"))
                .addStatement("$T fileReader", getIOClass("FileReader"))
                .addStatement("File file = new File(folder, fileName)")
                .addCode("try {\n")
                .addStatement("fileReader = new FileReader(file)")
                .addStatement("reader = new BufferedReader(fileReader)")
                .addStatement("String line")
                .addCode("while ((line = reader.readLine()) != null) {\n")
                .addStatement("stringBuilder.append(line)")
                .addCode("}\n")
                .addStatement("return stringBuilder.toString()")
                .addCode("} catch (FileNotFoundException e) {\n")
                .addStatement("e.printStackTrace()")
                .addCode("} catch (IOException e) {\n")
                .addStatement("e.printStackTrace()")
                .addCode("}\n")
                .addStatement("return stringBuilder.toString()")
                .build();
    }

    private List<MethodSpec> createFilePutAndGetMethod() {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        List<AnnotationEntityField> fieldList = mEntityClass.getKeyFields();
        for (AnnotationEntityField field : fieldList) {
            if (field.isObjectField()) {
                continue;
            }
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
            TypeName typeName = field.getTypeName();
            String typeNameString = field.getTypeStringName();
            String putMethodName = "put" + fieldName;
            String putMethodImplName = "put" + typeNameString;
            String getMethodName = "get" + fieldName;
            String getMethodImplName = "get" + typeNameString;


        }
        return methodSpecList;
    }

    private MethodSpec createFileGetImplMethod(String methodName, TypeName valueTypeName) {
        TypeName stringType = TypeName.get(String.class);
        String returnCode = valueTypeName.equals(TypeName.BOOLEAN)
                ? "return false;"
                : (valueTypeName.equals(stringType) ? "return \"\";\n" : "return 0;");
        TypeName returnType = valueTypeName.equals(TypeName.FLOAT) ? TypeName.DOUBLE : valueTypeName;
        String realMethod = methodName.equals("getFloat") ? "getDouble" : methodName;
        return MethodSpec.methodBuilder(realMethod)
                .addModifiers(PRIVATE)
                .addParameter(stringType, "key")
                .returns(returnType)
                .addCode("try {\n")
                .addStatement("String data = readDataFromFile(folder, \"$N\")", mEntityClass.getFileName())
                .addStatement("String json = AESEncryption.decrypt(data, \"\", \"1234567890ABCDFG\")")
                .addStatement("JSONObject object = new JSONObject(json)")
                .addStatement("return object.$N(key)", realMethod)
                .addCode("} catch (JSONException e) {\n")
                .addCode(" e.printStackTrace();\n")
                .addCode("}\n")
                .addCode(returnCode)
                .build();
    }

    private MethodSpec createFilePutImplMethod(String methodName, TypeName valueTypeName) {
        TypeName stringType = TypeName.get(String.class);
        ClassName encryptionClazz = ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "AESEncryption");
        String realMethod = methodName.equals("putFloat") ? "putDouble" : methodName;
        return MethodSpec.methodBuilder(realMethod)
                .addModifiers(PRIVATE)
                .addParameter(stringType, "key")
                .addParameter(valueTypeName, "value")
                .addCode("try {\n")
                .addStatement("$T object = new JSONObject()", ClassName.get("org.json", "JSONObject"))
                .addStatement("object.put(\"key\", value)")
                .addStatement("String json = $T.encrypt(object.toString(), \"1234567890ABCDFG\")", encryptionClazz)
                .addStatement("writeDataToFile(folder, \"$N\", json)", mEntityClass.getFileName())
                .addCode("} catch ($T e) {\n", ClassName.get("org.json", "JSONException"))
                .addStatement("e.printStackTrace()")
                .addCode("}")
                .build();
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
        List<AnnotationEntityField> fieldList = mEntityClass.getKeyFields();

        for (AnnotationEntityField field : fieldList) {
            if (field.isObjectField()) {
                continue;
            }
            String fieldName = GeneratorHelper.toUpperFirstChar(field.getKeyName());
            TypeName typeName = field.getTypeName();
            String typeNameString = field.getTypeStringName();

            String putMethodName = "put" + fieldName;
            String putMethodImplName = "put" + typeNameString;

            String getMethodName = "get" + fieldName;
            String getMethodImplName = "get" + typeNameString;
            //put
            MethodSpec putMethodSpec = MethodSpec.methodBuilder(putMethodName)
                    .addModifiers(PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(typeName, "value", FINAL)
                    .addStatement("$N(\"$N\", value)", putMethodImplName, fieldName.toUpperCase())
                    .build();
            //get 有 defValue
            MethodSpec.Builder getMethodDefBuilder = MethodSpec.methodBuilder(getMethodName).addModifiers(PUBLIC);
            createGetMethodSpec(getMethodDefBuilder, typeName, getMethodImplName, fieldName, true);
            MethodSpec getMethodDef = getMethodDefBuilder.build();
            //get 没 defValue
            MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(getMethodName).addModifiers(Modifier.PUBLIC);
            createGetMethodSpec(getMethodBuilder, typeName, getMethodImplName, fieldName, false);
            MethodSpec getMethod = getMethodBuilder.build();

            methodSpecList.add(putMethodSpec);
            methodSpecList.add(getMethodDef);
            methodSpecList.add(getMethod);
        }
        return methodSpecList;
    }

    /**
     * 创建 Object 方法
     */
    private List<MethodSpec> createObjectPutAndGet() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<AnnotationEntityField> fieldList = mEntityClass.getKeyFields();
        for (AnnotationEntityField keyField : fieldList) {
            if (keyField.isObjectField()) {
                ClassName converterClazz = ClassName.get(keyField.getConverterPackage(), keyField.getConverter());
                ClassName encryptionClazz = ClassName.get(GeneratorHelper.CODE_PACKAGE_NAME, "AESEncryption");
                String typeName = keyField.getTypeName().box().toString();

                String upperKeyName = GeneratorHelper.toUpperFirstChar(keyField.getKeyName());
                String putMethodName = "put" + upperKeyName;
                String getMethodName = "get" + upperKeyName;

                methodSpecs.add(MethodSpec.methodBuilder(putMethodName)
                        .addAnnotation(Override.class)
                        .addModifiers(PUBLIC)
                        .addParameter(keyField.getTypeName(), keyField.getKeyName())
                        .addStatement("$T parser = new $N($N.class)", converterClazz, keyField.getConverter(), typeName)
                        .addStatement("String json = parser.convertObject($N)", keyField.getKeyName())
                        .addStatement("putString(\"$N\",$T.encrypt(json,\"1234567890ABCDFG\"))",
                                keyField.getKeyName().toUpperCase(), encryptionClazz)
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
            }
        }
        return methodSpecs;
    }

    private List<MethodSpec> createRemoveAndContains() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        List<AnnotationEntityField> fieldList = mEntityClass.getKeyFields();
        for (AnnotationEntityField field : fieldList) {
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

    private String getPackageName() {
        return mEntityClass.getPackageName();
    }

    private ClassName getIOClass(String simpleName) {
        return ClassName.get("java.io", simpleName);
    }
}
