package main;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
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
    static Log log;

    static final String version = "v0.2.2";

    public static void main(String[] args) throws InterruptedException {
        log = new Log();
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        System.out.println("---------------------------------------------------\n");
        System.out.println("Bouffier Java " + version);
        System.out.println("\n---------------------------------------------------\n");

        projectPath = getProjectPath();
        System.out.println("Project Path: " + projectPath.toString());
        log.ProjectPath = projectPath.toString();

        format = getFormat();
        System.out.println("Output Format: " + format.toString());
        log.OutputFormat = format.toString();

        mode = getParseMode();
        System.out.println("Parse Mode: " + mode.toString());
        log.ParseMode = mode.toString();

        log.StartTimer();
        switch (mode) {
            case FILE:
                ParseByFile();
                break;
            case METHOD:
                ParseByMethod();
                break;
        }
        log.StopTimer();

        log.PrintSummary();

        String logJson = gson.toJson(log);
        String logFilename = projectPath.resolve(log.Name + ".json").toString();
        System.out.println("\n" + logJson);
        try {
            FileWriter logf = new FileWriter(logFilename);
            logf.write(logJson);
            logf.close();
            System.out.println("wrote to " + logFilename);
        } catch (Exception e) {
            e.printStackTrace();
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
    private static void writeAST(FormatType format, Path outFilePath, CompilationUnit unit) throws Exception {
        Path filePath = Paths.get(outFilePath.toString() + "." + format);
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
    }

    /**
     * convert source to AST by file unit.
     */
    private static void ParseByFile() {
        System.out.println("\nSTART TO PARSE\n");
        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");

        AtomicInteger parsed = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

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
                            System.out.println("done");
                            parsed.addAndGet(1);
                        } catch (Exception e) {
                            log.AppendFailure(src.toString(), e);
                            System.out.println("failed");
                            failed.addAndGet(1);
                        }
                    });
        } catch (IOException e) {
            log.ErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        System.out.println("\n\nDONE.");

        log.ParsedFiles = parsed.get();
        log.ParseFailedFiles = failed.get();
    }

    private static void ParseByMethod() {
        System.out.println("\nSTART TO PARSE\n");

        Path sourcePath = projectPath.resolve("source");
        Path outPath = projectPath.resolve("out");

        AtomicInteger parsedFiles = new AtomicInteger();
        AtomicInteger parsedMethods = new AtomicInteger();
        AtomicInteger failedFiles = new AtomicInteger();

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
                            parsedFiles.addAndGet(1);
                            parsedMethods.addAndGet(visitor.getNumOfParsedMethods());
                            System.out.println("done");
                        } catch (Exception e) {
                            log.AppendFailure(src.toString(), e);
                            failedFiles.addAndGet(1);
                            System.out.println("failed");
                        }
                    });
        } catch (IOException e) {
            log.ErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        System.out.println("\n\nDONE.");

        log.ParsedFiles = parsedFiles.get();
        log.ParsedMethods = parsedMethods.get();
        log.ParseFailedFiles = failedFiles.get();
    }
}
