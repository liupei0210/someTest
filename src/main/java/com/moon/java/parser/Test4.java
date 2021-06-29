package com.moon.java.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;

public class Test4 {
    public static void main(String[] args) {
        CompilationUnit compilationUnit = new CompilationUnit();
        //导入import
        compilationUnit.setPackageDeclaration("com.sunsheen.jfids.das.app.hearken.component.controller");
        compilationUnit.addImport("com.sunsheen.jfids.das.app.hearken.component.instance.ConvertToMapComponent", false, false);
        compilationUnit.addImport("com.sunsheen.jfids.das.core.annotation.Bean");
        compilationUnit.addImport("com.sunsheen.jfids.das.core.log.LogUtils");
        compilationUnit.addImport("com.sunsheen.jfids.system.bizass.port.IDataPort");
        compilationUnit.addImport("org.slf4j.Logger");
        compilationUnit.addImport("javax.inject.Inject");
        compilationUnit.addImport("javax.inject.Named");
        compilationUnit.addImport("javax.servlet.http.HttpServletRequest");
        compilationUnit.addImport("javax.ws.rs.POST");
        compilationUnit.addImport("javax.ws.rs.Path");
        compilationUnit.addImport("javax.ws.rs.Produces");
        compilationUnit.addImport("javax.ws.rs.core.Context");
        compilationUnit.addImport("javax.ws.rs.core.MediaType");
        compilationUnit.addImport("javax.ws.rs.core.Response");
        compilationUnit.addImport("java.util.HashMap");
        compilationUnit.addImport("java.util.Map");
        //创建类
        ClassOrInterfaceDeclaration clazz = compilationUnit.addClass("AddController", Modifier.Keyword.PUBLIC);
        clazz.addAnnotation(new NormalAnnotationExpr().addPair("value", "\"/table\"").setName("Path"));
        clazz.addAnnotation("Bean");
        //属性声明
        clazz.addFieldWithInitializer("Logger", "logger",
                new MethodCallExpr(new NameExpr("LogUtils"), "logger",
                        NodeList.nodeList(new FieldAccessExpr(new NameExpr("AddController"), "class"))),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
        clazz.addField("HttpServletRequest", "servletRequest", Modifier.Keyword.PRIVATE).addAnnotation("Context");
        clazz.addField("ConvertToMapComponent", "convertToMapComponent", Modifier.Keyword.PRIVATE).addAnnotation("Inject");
        clazz.addField("IDataPort", "save", Modifier.Keyword.PRIVATE).addAnnotation("Inject").
                addAnnotation(new NormalAnnotationExpr().addPair("value", "\"database#1.0.0\"").setName("Named"));
        //创建方法
        MethodDeclaration method = clazz.addMethod("addOne", Modifier.Keyword.PUBLIC);
        method.setType("Response");
        method.addAnnotation("Post");
        method.addAnnotation(new NormalAnnotationExpr().addPair("value", "\"/add\"").setName("Path"));
        method.addAnnotation(new NormalAnnotationExpr().addPair("value", new FieldAccessExpr(new NameExpr("MediaType"),
                "APPLICATION_JSON")).setName("Produces"));
        //try语句
        TryStmt tryStmt = new TryStmt();
        tryStmt.getTryBlock().getStatements().add(new ExpressionStmt(new VariableDeclarationExpr().setVariables(
                NodeList.nodeList(new VariableDeclarator().setType("Map<String,Object>").setName("param").
                        setInitializer(new ObjectCreationExpr().setType("HashMap<String, Object>")))
        )));
        //catch语句
        tryStmt.setCatchClauses(NodeList.nodeList(
                new CatchClause(new Parameter(new TypeParameter("Exception"), "e"), new BlockStmt())));
        method.getBody().get().getStatements().add(tryStmt);
        System.out.println(compilationUnit.toString());
    }
}
