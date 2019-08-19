package main;

import com.google.gson.annotations.Expose;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Log {
    @Expose
    public String Name;
    @Expose
    public String ParseMode;
    @Expose
    public String ProjectPath;
    @Expose
    public String OutputFormat;

    @Expose
    public Integer ParsedFiles;
    @Expose
    public Integer ParsedMethods;
    @Expose
    public Integer ParseFailedFiles;

    @Expose
    public String ErrorMessage;

    @Expose
    public Integer DurationMs;
    @Expose(serialize = false)
    private long st;

    public Log() {
        Name = genLogName();
    }

    class Failure {
        @Expose
        public String Filename;
        @Expose
        public String ErrorMessage;

        public Failure(String _filename, String _errMsg) {
            Filename = _filename;
            ErrorMessage = _errMsg;
        }
    }

    @Expose
    public ArrayList<Failure> Failures;

    public void AppendFailure(String filename, Exception e) {
        if(Failures == null) {
            Failures = new ArrayList<Failure>();
        }
        Failure f = new Failure(filename, e.getMessage());
        Failures.add(f);
    }

    private static String genLogName() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return "log_" + sdf.format(cal.getTime());
    }

    public void StartTimer() {
        st = System.currentTimeMillis();
    }

    public void StopTimer() {
        DurationMs = Math.toIntExact(System.currentTimeMillis() - st);
    }

    public void PrintSummary() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("[Summary]");
        System.out.printf("parsed %d java files\n", ParsedFiles);
        System.out.printf("failed %d java files\n", ParsedMethods);
        if (ParseFailedFiles != null) {
            System.out.printf("parsed %d java methods\n", ParseFailedFiles);
        }
        System.out.println("------------------------------------------------------------------------");
    }
}
