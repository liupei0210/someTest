package com.moon.thread.threadpool;
//创建线程的工厂
@FunctionalInterface
public interface ThreadFactory {
    Thread createThread();
}
