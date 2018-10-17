package com.tb;

public interface Logger {
    void debug(String msg);

    void info(String msg);

    void error(String msg);

    void error(String msg, Throwable throwable);

    void setLevel(LogLevel level);

}
