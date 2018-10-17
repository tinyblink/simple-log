package com.tb.appender;

public interface ManagerFactory<M, D> {
    M createManager(String name, D data);
}
