package com.lzx.compiler;

import com.google.common.base.Throwables;
import com.lzx.annoation.PreferenceEntity;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.xml.crypto.Data;

/**
 * create by lzx
 * 2019-05-28
 */
public class Test extends AbstractProcessor {

    /**
     * 抽象语法树
     */
    private JavacTrees trees;

    /**
     * AST
     */
    private TreeMaker treeMaker;

    /**
     * 标识符
     */
    private Names names;

    /**
     * 日志处理
     */
    private Messager messager;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        messager = processingEnvironment.getMessager();
        this.names = Names.instance(context);
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        Set<? extends Element> annotation = roundEnv.getElementsAnnotatedWith(PreferenceEntity.class);
        for (Element element : annotation) {
            JCTree treesTree = trees.getTree(element);
            treesTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    //过滤属性
                    Map<Name, JCTree.JCVariableDecl> treeMap =
                            jcClass.defs.stream().filter(k -> k.getKind().equals(Tree.Kind.VARIABLE))
                                    .map(tree -> (JCTree.JCVariableDecl) tree)
                                    .collect(Collectors.toMap(JCTree.JCVariableDecl::getName, Function.identity()));
                    //处理变量
                    treeMap.forEach((k, jcVariable) -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, String.format("fields:%s", k));
                        try {
                            //增加get方法
                            jcClass.defs = jcClass.defs.prepend(generateGetterMethod(jcVariable));
                            //增加set方法
                            jcClass.defs = jcClass.defs.prepend(generateSetterMethod(jcVariable));
                        } catch (Exception e) {
                            messager.printMessage(Diagnostic.Kind.ERROR, Throwables.getStackTraceAsString(e));
                        }
                    });
                    //增加toString方法
                    jcClass.defs = jcClass.defs.prepend(generateToStringBuilderMethod());
                    super.visitClassDef(jcClass);
                }

                @Override
                public void visitMethodDef(JCMethodDecl jcMethod) {
                    //打印所有方法
                    messager.printMessage(Diagnostic.Kind.NOTE, jcMethod.toString());
                    //修改方法
                    if ("getTest".equals(jcMethod.getName().toString())) {
                        result = treeMaker
                                .MethodDef(jcMethod.getModifiers(), getNameFromString("testMethod"), jcMethod.restype,
                                        jcMethod.getTypeParameters(), jcMethod.getParameters(), jcMethod.getThrows(),
                                        jcMethod.getBody(), jcMethod.defaultValue);
                    }
                    super.visitMethodDef(jcMethod);
                }
            });
        }
        return true;
    }
}
