package com.moon.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
//log4j测试，使用读取指定配置文件测试
public class TestLog4j_2 {
    public static void main(String[] args) throws InterruptedException {
//        PropertyConfigurator.configure("/home/liupei/IdeaProjects/someTest/src/main/resources/log4j.properties");
          for(int i=0;i<5;i++){
              new ThreadTest().start();
          }
    }
}
