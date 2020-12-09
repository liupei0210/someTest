package com.moon.jsch.sunsheen.sms.appsync;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) {
        long start=System.currentTimeMillis();
        ConnBean cb=new ConnBean();
        cb.initByPasswd("172.18.194.117","root","admin");
        SSHExec ssh=SSHExec.getInstance(cb);
        if(ssh.connect()){
            try {
                new Collect(ssh,"","").visit("/etc/");
            } catch (TaskExecFailException | InterruptedException e) {
                e.printStackTrace();
            }
            ssh.disconnect();
        }
        long end=System.currentTimeMillis();
        System.out.println("scends:"+(end-start)/1000);
    }
}
