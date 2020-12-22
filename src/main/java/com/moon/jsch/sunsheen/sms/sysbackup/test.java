package com.moon.jsch.sunsheen.sms.sysbackup;


import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;

public class test {
    public static void main(String[] args){
        ConnBean cb=new ConnBean();
//        cb.initByPasswd("172.18.194.191","root","admin");
        SSHExec ssh=SSHExec.getInstance(cb);
//        ssh.connect();
//        SyncAppsAcq sa=new SyncAppsAcq("/home/liupei/liupei/test/172.18.194.191");
//        sa.loop(ssh,"/root/apps");
//        ssh.disconnect();
//        sa.binFiles.forEach((k,v)->{
//            System.out.println(k+"123:"+v);
//        });
//
//        cb.initByPasswd("172.18.194.117","root","admin");
//        ssh=SSHExec.getInstance(cb);
//        ssh.connect();
//        sa=new SyncAppsAcq("/home/liupei/liupei/test/172.18.194.117");
//        sa.loop(ssh,"/root/apps");
//        ssh.disconnect();
//        sa.binFiles.forEach((k,v)->{
//            System.out.println(k+"123:"+v);
//        });
//
        cb.initByPasswd("127.0.0.1","liupei","liupei0210");
        ssh=SSHExec.getInstance(cb);
        ssh.connect();
        SyncAppsDiff sad=new SyncAppsDiff("/home/liupei/liupei/test/");
        sad.loop(ssh,"/root/apps");
        ssh.disconnect();
        System.out.println(sad.script);
        String s=null;
        s.equals("-1");
    }
}
