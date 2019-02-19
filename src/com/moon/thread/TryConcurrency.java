package com.moon.thread;

import java.util.concurrent.TimeUnit;

public class TryConcurrency {
    public static void main(String[] agrs){
        new Thread(){
            @Override
            public void run(){
                enjoyMusic();
            }
        }.start();
        browseNews();
    }
    /*
    * Browse the latest news
    */
    private static void browseNews(){
        while(true){
            System.out.println("Uh-huh,the good news.");
            sleep(1);
        }
    }
    /*
    * Listening and enjoy the music
    */
    private static void enjoyMusic(){
        while(true){
            System.out.println("Un-huh,the nice music.");
            sleep(1);
        }
    }
    /*
    * Simulate
    * */
    private static void sleep(int seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
