package com.moon.log4j;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//基本的log4j测试，默认配置
public class TestLog4j {
    static Logger log=Logger.getLogger(TestLog4j.class);
    public static void main(String[] args) throws InterruptedException {
        BasicConfigurator.configure();
        log.setLevel(Level.DEBUG);
        log.trace("跟踪信息");
        log.debug("调试信息");
        log.info("输出信息");
        Thread.sleep(1000);
        log.warn("警告信息");
        log.error("错误信息");
        log.fatal("致命信息");
    }
}
