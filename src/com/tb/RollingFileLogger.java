package com.tb;


import com.tb.appender.RollingFileAppender;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RollingFileLogger implements Logger, LifeCycle {
    private RollingFileAppender appender;

    private AtomicReference<State> state = new AtomicReference<>(State.INIT);

    private volatile LogLevel level;

    private RollingFileConfig rollingFileConfig;

    private RollingFileLogger(RollingFileConfig rollingFileConfig) {
        this.rollingFileConfig = rollingFileConfig;
        this.level = rollingFileConfig.level;
    }

    public static RollingFileConfig createDefaultRollingFileConfig() {
        RollingFileConfig rollingFileConfig = new RollingFileConfig();
        rollingFileConfig.appendMode = true;
        rollingFileConfig.buffSize = 4096;
        rollingFileConfig.eagleFlush = true;
        rollingFileConfig.name = "simpleLog";
        rollingFileConfig.filePath = "./log/simple.log";
        rollingFileConfig.maxFileSize = 1024 * 1024 * 4;
        rollingFileConfig.level = LogLevel.INFO;
        return rollingFileConfig;
    }


    public void debug(String msg) {
        log(msg, LogLevel.DEBUG, null);
    }


    public void info(String msg) {
        log(msg, LogLevel.INFO, null);
    }

    public void error(String msg) {
        log(msg, LogLevel.ERROR, null);
    }

    public void error(String msg, Throwable throwable) {
        log(msg, LogLevel.ERROR, throwable);
    }

    @Override
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    @Override
    public void start() {
        if (state.compareAndSet(State.INIT, State.STARTING)) {
            appender = RollingFileAppender.createAppender(rollingFileConfig.name, rollingFileConfig.eagleFlush, rollingFileConfig.filePath, rollingFileConfig.appendMode, rollingFileConfig.buffSize, rollingFileConfig.maxFileSize);
            appender.start();
            state.compareAndSet(State.STARTING, State.STARTED);
        }
    }

    @Override
    public void stop() {
        if (state.compareAndSet(State.STARTED, State.STOPPING)) {
            appender.stop();
            state.compareAndSet(State.STOPPING, State.STOPPED);
        }
    }

    @Override
    public State getState() {
        return state.get();
    }

    private void log(String msg, LogLevel level, Throwable throwable) {
        if (getState() == State.STARTED) {
            if (this.level.ordinal() <= level.ordinal()) {
                appender.append(new LogEvent(msg, level, throwable));
            }
        }
    }

    public static class RollingFileConfig {
        private String name;
        private boolean eagleFlush;
        private String filePath;
        private int maxFileSize;
        private boolean appendMode;
        private int buffSize;
        private LogLevel level;
    }

    public static void main(String[] args) throws InterruptedException {
//        Logger logger = new SimpleLogger();

        RollingFileLogger logger = new RollingFileLogger(createDefaultRollingFileConfig());
        logger.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.stop();
            }
        }));
        for (int i = 0; i < 100000000; i++) {
            logger.info(" info");
            logger.error( "error", new RuntimeException());
            System.out.println("print " + i);
            TimeUnit.MICROSECONDS.sleep(5);
        }
        logger.stop();
    }
}
