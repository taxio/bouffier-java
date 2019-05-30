package main;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("argument not correct");
            System.exit(1);
        }
        String baseDir = getProjectDirName();
        String format = getFormat();
        try {
            Files.walk(Paths.get(baseDir))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .forEach(src -> {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(src);
                            cu.accept(new MyVisitor(format), "");
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

    private static String getProjectDirName() {
        String baseDir = System.getenv("BOUFFIER_JAVA_BASE_DIR");
        if (baseDir.length() == 0) {
            baseDir = "/bouffier-java";
        }
        // TODO: validate directory exists
        return baseDir;
    }

    private static String getFormat() {
        String format = System.getenv("BOUFFIER_JAVA_FORMAT");
        if (format.length() == 0) {
            format = "yaml";
        }
        // TODO: validate format
        return format;
    }
}
