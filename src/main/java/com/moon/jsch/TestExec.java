package com.moon.jsch;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class TestExec {
    public static void main(String[] args){
        JSch jsch=new JSch();
        Session session;
        {
            try {
                session = jsch.getSession("root","172.18.195.214");
                session.setPassword("jufeng2010");
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                Channel channel= session.openChannel("exec");
                ((ChannelExec)channel).setCommand("ls");
                InputStream is=null;
                List<String> results=new ArrayList<>();
                try {
                    is=channel.getInputStream();
                    channel.connect();
                    String line;
                    assert is != null;
                    BufferedReader br=new BufferedReader(new InputStreamReader(is));
                    while((line=br.readLine())!=null){
                        results.add(line);
                    }
                    results.forEach(System.out::println);
                    System.out.println(channel.getExitStatus());
                    channel.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                session.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }
    }
}
