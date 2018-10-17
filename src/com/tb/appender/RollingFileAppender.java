package com.tb.appender;


import com.tb.LogEvent;

public class RollingFileAppender extends AbstractAppender<RollingFileManager> {


    private RollingFileAppender(RollingFileManager manager, String name, boolean eagleFlush) {
        super(manager, name, eagleFlush);
    }

    @Override
    public void append(LogEvent event) {
        manager.checkRollover(event);
        super.append(event);
    }

    public static RollingFileAppender createAppender(String name, boolean eagleFlush, String filePath, boolean isAppend, int buffSize, int maxSize) {
        TriggerRollingPolicy policy = new SizeBasedPolicy(maxSize);
        RollingFileManager manager = RollingFileManager.getInstance(name, policy, filePath, isAppend, buffSize);
        return new RollingFileAppender(manager, name, eagleFlush);
    }
}
