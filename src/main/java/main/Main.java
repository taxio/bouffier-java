package main;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path projectPath = getProjectPath();
        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");
        String format = getFormat();
        validateFormat(format);

        try {
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .forEach(src -> {
                        System.out.println("convert: " + src.toString());
                        Path outFilePath = outPath.resolve(sourcePath.relativize(src));
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(src);
                            writeAST(format, outFilePath, cu);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Path getProjectPath() {
        String projectPath = System.getenv("BOUFFIER_JAVA_PROJECT_PATH");
        if (projectPath == null) {
            projectPath = "/bouffier-java";
        }
        Path p = Paths.get(projectPath);
        return p;
    }

    private static String getFormat() {
        String format = System.getenv("BOUFFIER_JAVA_FORMAT");
        if (format == null) {
            format = "yaml";
        }
        return format;
    }

    private static void validateFormat(String format) {
        if (!format.equals("yaml") && !format.equals("xml")) {
            throw new IllegalArgumentException("output format is not correct");
        }
    }

    private static void writeAST(String format, Path outFilePath, CompilationUnit unit) {
        Path filePath = Paths.get(outFilePath.toString() + "." + format);
        try {
            File file = filePath.toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            if (format.equals("yaml")) {
                YamlPrinter printer = new YamlPrinter(true);
                writer.write(printer.output(unit));
            } else if (format.equals("xml")) {
                XmlPrinter printer = new XmlPrinter(true);
                writer.write(printer.output(unit));
            }
            writer.close();
            System.out.println("write to: " + filePath.toString());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
