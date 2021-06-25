package com.moon.java.parser;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;

public class Test {
    public static void main(String[] args) {
        ClassOrInterfaceDeclaration myClass = new ClassOrInterfaceDeclaration();
        myClass.setComment(new LineComment("A very cool class!"));
        myClass.addAndGetAnnotation("Node");
        myClass.addAnnotation(new NormalAnnotationExpr().addPair("value","\"123\"").setName("Bean"));
        String v=myClass.getAnnotationByName("Bean").get().asNormalAnnotationExpr().getPairs().getFirst().get().getValue().toString();
        System.out.println(v);
        myClass.setName("MyClass");
        myClass.setModifier(Modifier.Keyword.PUBLIC,true);
        myClass.addField("String", "foo", Modifier.Keyword.PUBLIC);
        System.out.println(myClass);

    }
}
