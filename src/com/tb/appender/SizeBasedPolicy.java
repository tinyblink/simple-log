package com.tb.appender;


import com.tb.LogEvent;

import java.io.File;

public class SizeBasedPolicy implements TriggerRollingPolicy {
    private int maxSize;

    public SizeBasedPolicy(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean triggerRolling(File currentFile, LogEvent event) {
        return currentFile.length() + event.getMessage().length() > maxSize;
    }
}
