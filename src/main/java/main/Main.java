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

enum ParseMode {
    FILE,
    METHOD,
    ;
}

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
    static ParseMode mode;
    static final String version = "v0.1.0";

    public static void main(String[] args) {
        System.out.println("---------------------------------------------------\n");
        System.out.println("Bouffier Java " + version);
        System.out.println("\n---------------------------------------------------\n");

        projectPath = getProjectPath();
        System.out.println("Project Path: " + projectPath.toString());

        format = getFormat();
        System.out.println("Output Format: " + format.toString());

        mode = getParseMode();
        System.out.println("Parse Mode: " + mode.toString());

        switch (mode) {
            case FILE:
                ParseByFile();
                break;
            case METHOD:
                ParseByMethod();
                break;
        }
    }

    /**
     * get the directory path where the Java project is stored from the environment variable ( BOUFFIER_JAVA_PROJECT_PATH )
     *
     * @return the Path of Java project
     */
    private static Path getProjectPath() {
        String projectPath = System.getenv("BOUFFIER_JAVA_PROJECT_PATH");
        if (projectPath == null) {
            projectPath = "/bouffier-java-project";
        }

        return Paths.get(projectPath);
    }

    /**
     * get AST output format from environment variable ( BOUFFIER_JAVA_FORMAT )
     *
     * @return format type
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
     * get the parse mode from the environment variable ( BOUFFIER_JAVA_PARSE_MODE )
     * default is FILE MODE
     *
     * @return ParseMode
     */
    private static ParseMode getParseMode() {
        String mode = System.getenv("BOUFFIER_JAVA_PARSE_MODE");
        switch (mode) {
            case "file":
                return ParseMode.FILE;
            case "method":
                return ParseMode.METHOD;
        }

        return ParseMode.FILE;
    }

    /**
     * output AST in specified format.
     *
     * @param format      file format (e.g. yaml)
     * @param outFilePath output file path
     * @param unit        AST unit
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
     * convert source to AST by file unit.
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

    private static void ParseByMethod() {
        System.out.println("\nSTART TO PARSE\n");

        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");

        AtomicInteger parsedFiles = new AtomicInteger();

        try {
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .forEach(src -> {
                        System.out.print("parse: " + src.toString() + " ... ");

                        Path outFilePath = Paths.get(outPath.resolve(sourcePath.relativize(src)).toString() + "." + format.toString());
                        MethodVisitor visitor = new MethodVisitor(format, outFilePath);
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(src);
                            cu.accept(visitor, null);
                            visitor.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }

                        System.out.println("done");
                        parsedFiles.addAndGet(1);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n\nDONE.");
        System.out.printf("parsed %d java files\n", parsedFiles.get());
    }
}
