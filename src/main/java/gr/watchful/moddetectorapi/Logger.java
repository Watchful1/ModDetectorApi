package gr.watchful.moddetectorapi;

import java.io.*;

public class Logger {
    private static volatile Logger instance = null;

    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    public int logLevel;
    public boolean consoleOutput;
    private StringBuilder bldr;

    public Logger() {
        logLevel = WARNING;
        consoleOutput = false;
        bldr = new StringBuilder();
    }

    public static void init() {
        synchronized (Logger.class) {
            instance = new Logger();
        }
    }

    public static Logger getInstance() {
        return instance;
    }

    public void info(String message) {
        if(logLevel <= INFO) {
            System.out.println(message);
        }
    }

    public void warn(String message) {
        if(logLevel <= WARNING) {
            System.out.println(message);
        }
    }

    public void error(String message) {
        System.out.println(message);
    }

    public void message(String message) {
        System.out.println(message);
    }

    public void output(String message) {
        if(consoleOutput) {
            System.out.println(message);
        } else {
            bldr.append(message);
            bldr.append("\n");
        }
    }

    public void flushFile() {
        if(!consoleOutput) {
            File location = new File("ModList.txt");
            try{
                if(location.exists()) location.delete();
                location.createNewFile();

                FileWriter fstream = new FileWriter(location);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(bldr.toString());
                out.close();
            } catch (Exception e){
                error("Could not write to file");
            }
        }
    }
}
