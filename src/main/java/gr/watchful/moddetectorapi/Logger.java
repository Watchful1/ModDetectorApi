package gr.watchful.moddetectorapi;

import java.io.*;

public class Logger {
    private static volatile Logger instance = null;

    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    public int logLevel;
    public boolean consoleOutput;

    public Logger() {
        logLevel = WARNING;
        consoleOutput = false;
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
}
