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
        String baseDir = args[0];
        String format = args[1];
        String envBaseDir = System.getenv("");
        String envOutFormat = System.getenv("");
        // argsで指定されたディレクトリを再帰的に走査してjavaファイルを探す
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
}
