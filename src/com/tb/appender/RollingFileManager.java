package com.tb.appender;


import com.tb.LogEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RollingFileManager extends OutputAbstractManager {
    public static RollingFileManagerFactory factory = new RollingFileManagerFactory();
    private static final int REMAIN_FILE_NUMBER = 5;
    private volatile File currentFile;
    private String fileName;
    private int suffixIndex;
    private String fileSuffix;
    private int buffSize;
    private final TriggerRollingPolicy policy;
    private ExecutorService purgeExecutor = Executors.newSingleThreadExecutor();
    private Future purgeFuture;

    protected RollingFileManager(String name, OutputStream os, String fileName, File file, TriggerRollingPolicy policy, int buffSize) {
        super(name, os);
        this.currentFile = file;
        this.policy = policy;
        this.fileName = fileName;
        suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex > 0) {
            this.fileSuffix = fileName.substring(suffixIndex);
        }
        this.buffSize = buffSize;
    }


    protected void checkRollover(LogEvent event) {
        synchronized (policy) {
            //todo avoid rolling too fast
            // check lastRollTimeStamp

            if (policy.triggerRolling(currentFile, event)) {
                rollover();
            }
        }
    }

    private void rollover() {
        close();
        if (purgeFuture == null || (purgeFuture.isDone())) {
            purgeFuture = purgeExecutor.submit(new purgeTask());
        }
        String rollOverFileName = new SimpleDateFormat("-yyyy-MM-dd-hh-mm").format(new Date());
        if (suffixIndex > 0) {
            rollOverFileName = fileName.substring(0, suffixIndex) + rollOverFileName + fileSuffix;
        } else {
            rollOverFileName = fileName + rollOverFileName;
        }
        try {
            File rollOverFile = new File(rollOverFileName);
            if (rollOverFile.exists()) {
                int i = 0;
                do {
                    rollOverFile = new File(rollOverFileName + "." + i);
                    i++;
                } while (rollOverFile.exists());
            }
            currentFile.renameTo(rollOverFile);
            File file = new File(fileName);
            file.createNewFile();
            currentFile = file;
            os = new BufferedOutputStream(new FileOutputStream(fileName, true), buffSize);
        } catch (Exception e) {

        }


    }

    @Override
    public void release() {
        super.release();
        purgeExecutor.shutdown();
        try {
            purgeExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
    }

    public static RollingFileManager getInstance(String name, TriggerRollingPolicy policy, String fileName, boolean isAppend, int buffSize) {
        return getManager(name, new RollingFileConfigData(policy, fileName, isAppend, buffSize), factory);
    }


    private class purgeTask implements Runnable {
        public void run() {
            try {
                if (currentFile != null && currentFile.exists()) {
                    File[] logFiles = currentFile.getAbsoluteFile().getParentFile().listFiles();
                    Arrays.sort(logFiles);
                    for (int i = 0; i < logFiles.length - REMAIN_FILE_NUMBER ; i++) {
                        logFiles[i].delete();
                    }
                }
            } catch (Throwable t) {

            }
        }
    }


    //分离配置
    private static class RollingFileConfigData {
        private TriggerRollingPolicy policy;
        private String fileName;
        private boolean append;
        private int buffSize;

        public RollingFileConfigData(TriggerRollingPolicy policy, String fileName, boolean append, int buffSize) {
            this.policy = policy;
            this.fileName = fileName;
            this.append = append;
            this.buffSize = buffSize;
        }

    }

    private static class RollingFileManagerFactory implements ManagerFactory<RollingFileManager, RollingFileConfigData> {

        public RollingFileManager createManager(String name, RollingFileConfigData data) {
            final File file = new File(data.fileName).getAbsoluteFile();
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (final IOException ioe) {
                return null;
            }
            OutputStream os;
            try {
                os = new BufferedOutputStream(new FileOutputStream(data.fileName, data.append), data.buffSize);
                return new RollingFileManager(name, os, data.fileName, file, data.policy, data.buffSize);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("fail to init manager: " + name);
            }
        }

    }
}
