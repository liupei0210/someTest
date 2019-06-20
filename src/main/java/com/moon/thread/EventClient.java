package com.moon.thread;

import java.util.concurrent.TimeUnit;

public class EventClient {
    public static void main(String[] args){
        final EventQueue eventQueue=new EventQueue(10);
        new Thread(()->{
            for(;;){
                eventQueue.offer(new EventQueue.Event());
            }
        },"Producer").start();
        new Thread(()->{
            for(;;) {
                eventQueue.take();
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"Consumer").start();
    }
}
