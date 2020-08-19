package com.moon.jsch.sunsheen.sms.sysbackup;

import com.jcraft.jsch.*;
import com.moon.jsch.TestSftp;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SshUtils {
    public static final String SFTP_GET = "get";
    public static final String SFTP_PUT= "put";
    public static final int TIME_OUT=3*1000;
    Logger log = Logger.getLogger(TestSftp.class);
    private Session session=null;
    public Session getSession(MyUserInfo user) {
        log.setLevel(Level.INFO);
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(user.getUser(), user.getHost());
            session.setUserInfo(user);
            session.setConfig("StrictHostKeyChecking", "no");
            log.info("Create jsch session successfully.");
        } catch (JSchException e) {
            e.printStackTrace();
            log.error("Create jsch session error!");
        }
        return session;
    }

    private Channel getChannel( String type) {
        Channel channel = null;
        try {
            session.connect(TIME_OUT);
            channel = session.openChannel(type);
            log.info("Create jsch channel successfully.");
            return type.equals("exec") ? (ChannelExec) channel : (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            log.error("Create jsch channel error!");
            return null;
        }
    }

    public List<String> Exec(String command) {
        ChannelExec channelExec = (ChannelExec) getChannel("exec");
        assert channelExec != null;
        channelExec.setCommand(command);
        InputStream is = null;
        List<String> results = new ArrayList<>();
        try {
            is = channelExec.getInputStream();
            channelExec.connect(TIME_OUT);
            String line;
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                results.add(line);
            }
            channelExec.disconnect();
            results.add(0,String.valueOf(channelExec.getExitStatus()));
        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }
        return results;
    }
    public void Sftp(String src,String dst,String sftp_type){
        ChannelSftp channelSftp=(ChannelSftp) getChannel("sftp");
        try {
            assert channelSftp != null;
            channelSftp.connect(TIME_OUT);
            if(sftp_type.equals(SshUtils.SFTP_GET)){
                channelSftp.get(src,dst);
            }else {
                channelSftp.put(src,dst);
            }
            channelSftp.quit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }
    public void closeSession(){
        if(session.isConnected()) session.disconnect();
    }
}
