package com.moon.jsch;

import com.jcraft.jsch.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import java.util.Properties;

public class TestSftp {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(TestSftp.class);
        log.setLevel(Level.INFO);
        JSch jsch = new JSch();
        Session session;
        {
            try {
                session = jsch.getSession("root", "172.18.195.214");
                session.setPassword("jufeng2010");
                session.setConfig("StrictHostKeyChecking", "no");
                Properties config = new Properties();
                config.put("userauth.gssapi-with-mic", "no");// SSH连接慢的问题
                session.setConfig(config);
                session.connect();
                Channel channel = session.openChannel("sftp");
                try {
                    channel.connect();
                    ((ChannelSftp) channel).get("/root/test/test.txt", "/home/liupei/liupei/test/test.txt");
                    ((ChannelSftp) channel).quit();
                    System.out.println(channel.getExitStatus());
                } catch (SftpException e) {
                    e.printStackTrace();
                    log.fatal("致命信息");
                }
                session.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }
    }
}
