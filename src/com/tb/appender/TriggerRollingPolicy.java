package com.tb.appender;


import com.tb.LogEvent;

import java.io.File;

public interface TriggerRollingPolicy {
    boolean triggerRolling(final File currentFile, LogEvent event);
}
