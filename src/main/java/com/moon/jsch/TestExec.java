package com.moon.jsch;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class TestExec {
    public static void main(String[] args) {
        JSch jsch = new JSch();
        Session session;
        {
            try {
                session = jsch.getSession("liupei", "127.0.0.1");
                session.setPassword("liupei0210");
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand("diff /home/liupei/liupei/test/passwd/passwd1 /home/liupei/liupei/test/passwd/passwd");
                InputStream is = null;
                List<String> results = new ArrayList<>();
                try {
                    is = channel.getInputStream();
                    channel.connect();
                    String line;
                    assert is != null;
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null) {
                        results.add(line);
                    }
                    results.forEach(System.out::println);
                    channel.disconnect();
                    System.out.println(channel.getExitStatus());
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
