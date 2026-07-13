package com.guardpoint.android.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "guardpoint.log";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    public FileLoggingTree(Context context) {
        this.context = context;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        File logDir = new File(context.getExternalFilesDir(null), LOG_DIR);
        if (!logDir.exists() && !logDir.mkdirs()) return;

        File logFile = new File(logDir, LOG_FILE);
        if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
            File rotated = new File(logDir, "guardpoint_old.log");
            logFile.renameTo(rotated);
        }

        String priorityStr = priorityToString(priority);
        String timestamp = dateFormat.format(new Date());
        String line = String.format(Locale.US, "%s [%s] %s: %s%s",
                timestamp, priorityStr, tag, message,
                t != null ? " | " + Log.getStackTraceString(t) : "");

        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(line);
            fw.write("\n");
            fw.flush();
        } catch (IOException ignored) {
        }
    }

    private String priorityToString(int priority) {
        switch (priority) {
            case Log.VERBOSE: return "VERBOSE";
            case Log.DEBUG:   return "DEBUG";
            case Log.INFO:    return "INFO";
            case Log.WARN:    return "WARN";
            case Log.ERROR:   return "ERROR";
            case Log.ASSERT:  return "ASSERT";
            default:          return "UNKNOWN";
        }
    }
}
