package main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Log {
    public String Name;
    public String ParseMode;
    public String ProjectPath;
    public String OutputFormat;

    public Integer ParsedFiles;
    public Integer ParsedMethods;
    public Integer ParseFailedFiles;

    public String ErrorMessage;

    public Log() {
        Name = genLogName();
    }

    class Failure {
        public String Filename;
        public String ErrorMessage;

        public Failure(String _filename, String _errMsg) {
            Filename = _filename;
            ErrorMessage = _errMsg;
        }
    }

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
}
