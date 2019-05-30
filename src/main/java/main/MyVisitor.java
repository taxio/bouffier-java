package main;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;

public class MyVisitor extends VoidVisitorAdapter<String> {
    private String format;

    MyVisitor(String format) {
        this.format = format;
        if (!format.equals("yaml") && !format.equals("xml")) {
            throw new IllegalArgumentException("output format is not correct");
        }
    }

    @Override
    public void visit(MethodDeclaration n, String arg) {
        if (this.format.equals("yaml")) {
            YamlPrinter printer = new YamlPrinter(true);
            System.out.println(printer.output(n));
        } else if (this.format.equals("xml")) {
            XmlPrinter printer = new XmlPrinter(true);
            System.out.println(printer.output(n));
        }
        System.out.println("------------------------------------\n");
    }
}
