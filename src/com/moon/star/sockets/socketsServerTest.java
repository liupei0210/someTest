package com.moon.star.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

//sockets练习
public class socketsServerTest {
    public static void main(String[] args){
        int port=12345;
        try {
            ServerSocket ss=new ServerSocket(port);
            System.out.println("Waitting for messages......");
            Socket s=ss.accept();
            InputStream is=s.getInputStream();
            byte[] bytes=new byte[1024];
            int len;
            StringBuilder sb=new StringBuilder();
            while((len=is.read(bytes))!=-1){
                sb.append(new String(bytes,0,len,"UTF-8"));
            }
            System.out.println("Get messages from client:"+sb);
            s.close();
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
