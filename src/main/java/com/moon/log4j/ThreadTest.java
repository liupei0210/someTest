package com.moon.log4j;

import org.apache.log4j.MDC;

public class ThreadTest extends Thread{
    @Override
    public void run() {
        super.run();
        MDC.put("back_server_id",Thread.currentThread().getId());
        LogTest.info("跟踪信息");
        LogTest.warn("调试信息");
        LogTest.error("输出信息");
    }
}
