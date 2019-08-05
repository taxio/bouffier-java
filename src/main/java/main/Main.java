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
import java.util.concurrent.atomic.AtomicInteger;

enum FormatType {
    YAML,
    XML,
    ;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}

public class Main {

    static FormatType format;
    static Path projectPath;
    static final String version = "v0.0.1";

    public static void main(String[] args) {
        System.out.println("---------------------------------------------------\n");
        System.out.println("Bouffier Java " + version);
        System.out.println("\n---------------------------------------------------\n");

        projectPath = getProjectPath();
        System.out.println("Project Path: " + projectPath.toString());

        format = getFormat();
        System.out.println("Output Format: " + format.toString());

        ParseByFile();
    }

    /**
     * 環境変数 ( BOUFFIER_JAVA_PROJECT_PATH ) から読み込むJavaプロジェクトが保存されているディレクトリのパスを取得する
     *
     * @return プロジェクトディレクトリのPathオブジェクト
     */
    private static Path getProjectPath() {
        String projectPath = System.getenv("BOUFFIER_JAVA_PROJECT_PATH");
        if (projectPath == null) {
            projectPath = "/bouffier-java-project";
        }

        return Paths.get(projectPath);
    }

    /**
     * 環境変数 ( BOUFFIER_JAVA_FORMAT ) からASTの出力フォーマットを取得する
     *
     * @return フォーマット名
     */
    private static FormatType getFormat() {
        String format = System.getenv("BOUFFIER_JAVA_FORMAT");
        if (format == null) {
            throw new IllegalArgumentException("no file format specified");
        }

        switch (format) {
            case "yaml":
                return FormatType.YAML;
            case "xml":
                return FormatType.XML;
        }

        throw new IllegalArgumentException("invalid file format specified");
    }

    /**
     * AST Unitを指定のフォーマットでファイルに出力する
     *
     * @param format
     * @param outFilePath
     * @param unit
     */
    private static void writeAST(FormatType format, Path outFilePath, CompilationUnit unit) {
        Path filePath = Paths.get(outFilePath.toString() + "." + format);
        try {
            File file = filePath.toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            switch (format) {
                case YAML:
                    YamlPrinter yamlPrinter = new YamlPrinter(true);
                    writer.write(yamlPrinter.output(unit));
                    break;
                case XML:
                    XmlPrinter xmlPrinter = new XmlPrinter(true);
                    writer.write(xmlPrinter.output(unit));
                    break;
            }
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * ASTをファイルレベルで分割
     */
    private static void ParseByFile() {
        System.out.println("\nSTART TO PARSE\n");
        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");

        AtomicInteger parsed = new AtomicInteger();

        try {
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .forEach(src -> {
                        System.out.print("parse: " + src.toString() + " ... ");
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
                        System.out.println("done");
                        parsed.addAndGet(1);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n\nDONE.");
        System.out.printf("parsed %d java files\n", parsed.get());
    }
}
