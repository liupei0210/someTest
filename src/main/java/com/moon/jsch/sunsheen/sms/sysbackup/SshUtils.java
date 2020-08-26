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
/*
*@author lp
*ssh连接服务器执行命令操作或文件传输
*在调用了createSession完成相关操作后,要记得调用closeSession关闭连接.
*
* */
public class SshUtils {
    public static final String SFTP_GET = "get";
    public static final String SFTP_PUT= "put";
    public static final int TIME_OUT=20*1000;
    private final Logger log = Logger.getLogger(TestSftp.class);
    private Session session=null;
    public void createSession(MyUserInfo user) {
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(user.getUser(), user.getHost());
            session.setUserInfo(user);
            session.setConfig("StrictHostKeyChecking", "no");
//            session.setConfig("userauth.gssapi-with-mic", "no");
            log.info("Create jsch session successfully. "+user.getHost());
        } catch (JSchException e) {
            e.printStackTrace();
            log.error("Create jsch session error! "+user.getHost());
        }
    }

    private Channel getChannel( String type) {
        Channel channel = null;
        try {
            if(!session.isConnected())session.connect(TIME_OUT);
            channel = session.openChannel(type);
            log.info("Create jsch channel "+type+" successfully.");
            return type.equals("exec") ? (ChannelExec) channel : (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            log.error("Create jsch channel "+type+" error!");
            return null;
        }
    }

    public List<String> exec(String command) {
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
            //results 第0个数据存储的是命令执行返回码
            results.add(0,String.valueOf(channelExec.getExitStatus()));
            log.info("Channel is opened.");
        } catch (IOException | JSchException e) {
            log.error("Channel is not opened!");
            e.printStackTrace();
        }
        return results;
    }
    public void sftp(String src,String dst,String sftp_type){
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
        if(session.isConnected()) {
            session.disconnect();
            log.info("Session is closed. "+session.getHost());
        }
    }
}
