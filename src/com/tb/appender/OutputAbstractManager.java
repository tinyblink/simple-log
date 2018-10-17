package com.tb.appender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class OutputAbstractManager {
    private static Map<String, OutputAbstractManager> store = new HashMap<String, OutputAbstractManager>();
    private static final Lock lock = new ReentrantLock();
    protected String name;


    protected volatile OutputStream os;

    protected OutputAbstractManager(String name, OutputStream os) {
        this.name = name;
        this.os = os;
    }

    //职责分离，抽出factory和manager
    protected static <T extends OutputAbstractManager, D> T getManager(final String name, D data, ManagerFactory<T, D> factory) {
        lock.lock();
        T manager = (T) store.get(name);
        if (manager == null) {
            manager = factory.createManager(name, data);
            if (manager == null) {
                throw new IllegalStateException("fail to init manager " + name);
            }
            store.put(name, manager);
        }
        lock.unlock();
        return manager;
    }

    public void release() {
        store.remove(name);
        close();
    }

    protected void close() {
        flush();
        try {
            os.close();
        } catch (IOException e) {
        }
    }

    protected void flush() {
        try {
            os.flush();
        } catch (IOException e) {
        }
    }

    protected void write(byte[] bytes, int off, int len) {
        try {
            os.write(bytes, off, len);
        } catch (IOException e) {

        }
    }


}
