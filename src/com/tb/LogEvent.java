package com.tb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEvent{
    private String message;

    private Throwable throwable;

    private String prefix;

    public LogEvent(String message, LogLevel level, Throwable throwable) {
        DateFormat prefixDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        prefix = String.format("%s [%s] ", level.name(), prefixDateformat.format(new Date()));
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }
        if (level == LogLevel.ERROR) {
            this.throwable = throwable;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(message);
        sb.append("\n");
        if (throwable != null) {
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                sb.append("\n       ").append(stackTraceElement);
            }
            sb.append("\n");
        }else{

        }
        return sb.toString();
    }

    //codec
    public byte[] toBytes() {
        return toString().getBytes();
    }

}
