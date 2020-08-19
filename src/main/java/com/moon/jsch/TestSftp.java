package com.moon.jsch;

import com.jcraft.jsch.*;
import com.moon.jsch.sunsheen.sms.sysbackup.MyUserInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class TestSftp {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(TestSftp.class);
        log.setLevel(Level.INFO);
        JSch jsch = new JSch();
        Session session;
            try {
                MyUserInfo myUserInfo=new MyUserInfo("test","172.18.194.117",22);
                myUserInfo.setPassword("test");
                if(Files.exists(Paths.get(myUserInfo.getIdentity()))){
                    log.info("There has an identity");
                    jsch.addIdentity(myUserInfo.getIdentity(),myUserInfo.getPassphrase());
                }
                session = jsch.getSession(myUserInfo.getUser(),myUserInfo.getHost());
                session.setUserInfo(myUserInfo);
//                session.setPassword("jufeng2010");
                session.setConfig("StrictHostKeyChecking", "no");
                Properties config = new Properties();
                config.put("userauth.gssapi-with-mic", "no");// SSH连接慢的问题
                session.setConfig(config);
                session.connect(3000);
                log.info("session connected.");
                Channel channel = session.openChannel("sftp");
                try {
                    channel.connect();
                    ((ChannelSftp) channel).get( "/etc/passwd","/home/liupei/liupei/test/passwd/");
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
