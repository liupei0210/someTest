package com.moon.thread.threadpool;

public class RunnableDenyException extends RuntimeException{
    public RunnableDenyException(String messages){
        super(messages);
    }
}
