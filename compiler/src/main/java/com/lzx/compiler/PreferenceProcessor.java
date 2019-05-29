package com.lzx.compiler;

import com.google.auto.service.AutoService;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.PreferenceEntity;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class) //自动注册
public class PreferenceProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Messager mMessager;
    private Map<String, List<Element>> classMap = new HashMap<>();

    private JavacTrees mTrees;
    private TreeMaker mTreeMaker;
    private Names mNames;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();

        mTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.mTreeMaker = TreeMaker.instance(context);
        mNames = Names.instance(context);


    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add(PreferenceEntity.class.getCanonicalName());
        supportedTypes.add(IgnoreField.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    /**
     * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     *
     * @param annotations 请求处理的注解类型
     * @param roundEnv    有关当前和以前的信息环境
     * @return 如果返回 true， 则这些注解已声明并且不要求后续 Processor 处理它们；
     * 如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     * <p>
     * <p>
     * roundEnv.getElementsAnnotatedWith() 返回使用给定注解类型的元素
     * for (Element element : roundEnv.getElementsAnnotatedWith(MyAnnotation.class)) {
     * <p>
     * if (element.getKind() == ElementKind.CLASS) {   // 判断元素的类型为Class
     * <p>
     * TypeElement typeElement = (TypeElement) element;  // 显示转换元素类型
     * <p>
     * typeElement.getSimpleName()  // 输出元素名称
     * <p>
     * typeElement.getAnnotation(MyAnnotation.class).value() //输出注解属性值
     * <p>
     * }
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 返回所有被注解了@PreferenceEntity的元素的列表
        Set<? extends Element> annotation = roundEnv.getElementsAnnotatedWith(PreferenceEntity.class);
        for (Element element : annotation) {
            JCTree treesTree = mTrees.getTree(element);
            treesTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    //过滤属性
                    Map<Name, JCTree.JCVariableDecl> treeMap = new HashMap<>();
                    List<JCTree> jcTrees = jcClass.defs;
                    for (JCTree jcTree : jcTrees) {
                        if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) jcTree;
                            treeMap.put(decl.getName(), decl);
                        }
                    }
                    //处理变量
                    for (Map.Entry<Name, JCTree.JCVariableDecl> entry : treeMap.entrySet()) {
                        showLog(String.format("fields:%s", entry.getKey()));
                        //增加get方法
                        jcClass.defs = jcClass.defs.prepend(generateGetterMethod(entry.getValue()));
                        //增加set方法
                        jcClass.defs = jcClass.defs.prepend(generateSetterMethod(entry.getValue()));
                    }
                    super.visitClassDef(jcClass);
                }

                @Override
                public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                    super.visitMethodDef(jcMethodDecl);
                }
            });
        }
        return true;
    }

    private JCTree generateGetterMethod(JCTree.JCVariableDecl value) {
        JCTree.JCModifiers jcModifiers = mTreeMaker.Modifiers(Flags.PUBLIC);
    }

    private JCTree generateSetterMethod(JCTree.JCVariableDecl value) {
        return null;
    }

    private void processEntity(TypeElement type) {
        PreferenceEntity preferenceEntity = type.getAnnotation(PreferenceEntity.class);
    }

    /**
     * 检查合法性
     * 1. PreferenceEntity只能加在类上面
     * 2. 字段不能有 final 修饰
     * 3. 字段不能有 private 修饰
     */
    private void checkValidEntityType(TypeElement annotatedType) throws IllegalAccessException {
        if (!annotatedType.getKind().isClass()) {
            throw new IllegalAccessException("Only classes can be annotated with @PreferenceEntity");
        } else if (annotatedType.getModifiers().contains(Modifier.FINAL)) {
            showErrorLog("class modifier should not be final", annotatedType);
        } else if (annotatedType.getModifiers().contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be private", annotatedType);
        }
    }

    private void showErrorLog(String message, Element element) {
        mMessager.printMessage(ERROR, "Error:" + message, element);
    }


    private void showLog(String message) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, message);
    }


}
