package com.tb.appender;


import com.tb.LifeCycle;
import com.tb.LogEvent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractAppender<M extends OutputAbstractManager> implements Appender, LifeCycle {

    protected M manager;
    protected String name;
    private AtomicReference<State> state = new AtomicReference<State>(State.INIT);
    private Lock lock = new ReentrantLock();
    private boolean eagleFlush;

    public AbstractAppender(M manager, String name, boolean eagleFlush) {
        this.manager = manager;
        this.name = name;
        this.eagleFlush = eagleFlush;
    }


    public void start() {
        if (state.compareAndSet(State.INIT, State.STARTING)) {
            state.compareAndSet(State.STARTING, State.STARTED);
        }
    }

    public void stop() {
        if (state.compareAndSet(State.STARTED, State.STOPPING)) {
            manager.release();
            state.compareAndSet(State.STOPPING, State.STOPPED);
        }
    }

    public State getState() {
        return state.get();
    }

    public void append(LogEvent event) {
        lock.lock();
        try {
            byte[] input = event.toBytes();
            if (input.length > 0) {
                manager.write(input, 0, input.length);
                if (eagleFlush) {
                    manager.flush();
                }
            }
        } finally {
            lock.unlock();
        }

    }
}
