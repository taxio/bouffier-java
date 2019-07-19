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

    /**
     * 環境変数 ( BOUFFIER_JAVA_PROJECT_PATH ) から読み込むJavaプロジェクトが保存されているディレクトリのパスを取得する
     *
     * @return プロジェクトディレクトリのPathオブジェクト
     */
    private static Path getProjectPath() {
        String projectPath = System.getenv("BOUFFIER_JAVA_PROJECT_PATH");
        if (projectPath == null) {
            projectPath = "/bouffier-java";
        }
        Path p = Paths.get(projectPath);
        return p;
    }

    /**
     * 環境変数 ( BOUFFIER_JAVA_FORMAT ) からASTの出力フォーマットを取得する
     *
     * @return フォーマット名
     */
    private static String getFormat() {
        String format = System.getenv("BOUFFIER_JAVA_FORMAT");
        if (format == null) {
            format = "yaml";
        }
        return format;
    }

    /**
     * フォーマットが想定されたものかどうかチェックする
     *
     * @param format フォーマット名
     */
    private static void validateFormat(String format) {
        if (!format.equals("yaml") && !format.equals("xml")) {
            throw new IllegalArgumentException("output format is not correct");
        }
    }

    /**
     * AST Unitを指定のフォーマットでファイルに出力する
     *
     * @param format
     * @param outFilePath
     * @param unit
     */
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
