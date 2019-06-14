package com.moon.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
//log4j测试，使用读取指定配置文件测试
public class TestLog4j_2 {
    static Logger log=Logger.getLogger(TestLog4j_2.class);
    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("/home/liupei/IdeaProjects/someTest/src/main/resources/log4j.properties");
        log.trace("跟踪信息");
        log.debug("调试信息");
        log.info("输出信息");
        Thread.sleep(1000);
        log.warn("警告信息");
        log.error("错误信息");
        log.fatal("致命信息");
    }
}
