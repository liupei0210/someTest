package com.moon.star.sockets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class socketsClientTest {
    public static void main(String[] args){
        String host="127.0.0.1";
        int port=12345;
        try {
            Socket s=new Socket(host,port);
            OutputStream os=s.getOutputStream();
            String messges="Hello Server!";
            s.getOutputStream().write(messges.getBytes("UTF-8"));
            s.shutdownOutput();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
