package com.tb;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger implements Logger {
    private volatile LogLevel level = LogLevel.DEBUG;

    @Override
    public void debug(String msg) {
        log(msg, LogLevel.DEBUG);
    }

    @Override
    public void info(String msg) {
        log(msg, LogLevel.INFO);
    }

    @Override
    public void error(String msg) {
        log(msg, LogLevel.ERROR);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        log(msg, LogLevel.ERROR);
        if (throwable != null) {
            for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                System.err.println(stackTraceElement.toString());
            }
        }
    }

    private void log(String msg, LogLevel level) {
        if (this.level.ordinal() <= level.ordinal()) {
            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            System.out.println(String.format("%s [%s] %s", level, timeStr, msg));
        }
    }

    @Override
    public void setLevel(LogLevel level) {
        this.level = level;
    }
}
