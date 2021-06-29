package com.moon.java.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

public class Test3 {
    public static void main(String[] args) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration("my.example.javaparser");

        // Add an asterisk import for java.util
        compilationUnit.addImport("java.util", false, true);

        // Create a class (not an interface, so the 2nd parameter is false)
        ClassOrInterfaceDeclaration myClass = compilationUnit.addClass("MyClass", Modifier.Keyword.PUBLIC);
        myClass.addField("List<String>", "elements", Modifier.Keyword.PRIVATE);

        // Method to add an element to the field
        MethodDeclaration addElement = myClass.addMethod("addElement", Modifier.Keyword.PUBLIC);
        // our method get a parameter: the value to add to the field
        addElement.addParameter("String", "newElement");
        // the body consists in one expression wrapped into a statement
        // the expression is in invocation of elements.add to which we
        // pass the parameter
        addElement.getBody().get().getStatements().add(new ExpressionStmt(
                new MethodCallExpr(new NameExpr("elements"), new SimpleName("add"),
                        NodeList.nodeList(new NameExpr("newElement")))
        ));
        addElement.getBody().get().getStatements().add(new ExpressionStmt(
                new MethodCallExpr( new MethodCallExpr(new NameExpr("elements"), new SimpleName("add"),
                        NodeList.nodeList(new NameExpr("newElement"))),new SimpleName("add"))
        ));
        addElement.getBody().get().getStatements().add(new BlockStmt()
                .addStatement(new ExpressionStmt(new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "title"),
                        new NameExpr("title"),
                        AssignExpr.Operator.ASSIGN)))
                .addStatement(new ExpressionStmt(new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "author"),
                        new NameExpr("author"),
                        AssignExpr.Operator.ASSIGN))));
        MethodDeclaration getElements = myClass.addMethod("getElements", Modifier.Keyword.PUBLIC);
        getElements.setType("Collection<String>");
        getElements.getBody().get().getStatements().add(new ReturnStmt(
                new NameExpr("elements")));

        System.out.println(compilationUnit);
    }
}
