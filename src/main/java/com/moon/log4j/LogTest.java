package com.moon.log4j;

import org.apache.log4j.Logger;

public class LogTest {
    public static Logger logger=Logger.getLogger(LogTest.class);
    public static void info(Object messages){
        logger.info(messages);
    }
    public static void warn(Object messages){
        logger.warn(messages);
    }
    public static void error(Object messages){
        logger.error(messages);
    }
}
