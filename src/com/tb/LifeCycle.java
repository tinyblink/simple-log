package com.tb;

public interface LifeCycle {
    enum State {
        INIT, STARTING, STARTED, STOPPING, STOPPED
    }

    void start();

    void stop();

    State getState();

}
