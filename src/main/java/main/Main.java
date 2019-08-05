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

enum ParseMode {
    None,
    File,
    Method,
}

enum FormatType {
    YAML,
    XML,
}

public class Main {

    static FormatType format;
    static Path projectPath;
    static ParseMode mode;

    public static void main(String[] args) {
        System.out.println("Hello bouffier java!");
        projectPath = getProjectPath();
        format = getFormat();
        mode = getParseMode();

        switch (mode) {
            case File:
                ParseByFile();
                break;
            case Method:
                ParseByMethod();
                break;
            default:
                System.out.println("other mode");
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
     * 環境変数 ( BOUFFIER_JAVA_PARSE_MODE ) 生成するASTの分割方法を取得する
     *
     * @return parse mode
     */
    private static ParseMode getParseMode() {
        String mode = System.getenv("BOUFFIER_JAVA_PARSE_MODE");
        if (mode == null) {
            return ParseMode.None;
        }

        switch (mode){
            case "file":
                return ParseMode.File;
            case "method":
                return ParseMode.Method;
        }

        return ParseMode.None;
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
            System.out.println("write to: " + filePath.toString());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * ASTをファイルレベルで分割
     */
    private static void ParseByFile() {
        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");

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
     * ASTをメソッドレベルで分割
     */
    private static void ParseByMethod() {
        return;
    }
}
