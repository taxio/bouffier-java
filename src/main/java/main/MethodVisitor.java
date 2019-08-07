package main;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class MethodVisitor extends VoidVisitorAdapter<String> {

    private FormatType format;
    private String writeBuff;
    private Path outFilePath;

    MethodVisitor(FormatType _format, Path _outFilePath) {
        format = _format;
        writeBuff = "";
        outFilePath = _outFilePath;
    }

    @Override
    public void visit(MethodDeclaration n, String arg) {
        if(format == FormatType.YAML) {
            YamlPrinter printer = new YamlPrinter(true);
            writeBuff += printer.output(n) + "\n";
        } else if (format == FormatType.XML) {
            XmlPrinter printer = new XmlPrinter(true);
            writeBuff += printer.output(n) + "\n";
        }
    }

    public void flush() {
        try {
            File file = outFilePath.toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(writeBuff);
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
