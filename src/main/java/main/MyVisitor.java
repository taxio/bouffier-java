package main;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyVisitor extends VoidVisitorAdapter<String> {
    private String format;
    private Path filePath;
    private String writeBuff;

    MyVisitor(String format, Path outFileName) {
        this.format = format;
        if (!format.equals("yaml") && !format.equals("xml")) {
            throw new IllegalArgumentException("output format is not correct");
        }
        filePath = Paths.get(outFileName.toString() + "." + format);
        writeBuff = "";
    }

    @Override
    public void visit(MethodDeclaration n, String arg) {
        if (this.format.equals("yaml")) {
            YamlPrinter printer = new YamlPrinter(true);
            writeBuff += printer.output(n) + "\n";
        } else if (this.format.equals("xml")) {
            XmlPrinter printer = new XmlPrinter(true);
            writeBuff += printer.output(n) + "\n";
        }
    }

    public void flush() {
        try {
            File file = filePath.toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(writeBuff);
            writer.close();
            System.out.println("write to: " + filePath.toString());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
