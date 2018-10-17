package com.tb.appender;


import com.tb.LogEvent;

public interface Appender {
    void append(LogEvent event);
}
