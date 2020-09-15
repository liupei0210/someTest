package com.moon.jsch.sunsheen.sms.sysbackup;

import com.jcraft.jsch.Session;

import java.util.List;

public class test {
    public static void main(String[] args){
        MyUserInfo user=new MyUserInfo("172.18.194.117");
        user.setUser("root");
        user.setIdentity("/home/liupei/.ssh/id_rsa");
//        user.setPassword("admin");
//        MyUserInfo user1=new MyUserInfo("172.18.194.191");
//        user1.setUser("root");
//        user1.setPassword("admin");
//        new SyncPasswd(user,user1).start();
//         MyUserInfo myself=new MyUserInfo("127.0.0.1");
//         myself.setUser("liupei");
//         myself.setPassword("liupei0210");
//         new SyncFolder(user,myself).start("/home/liupei/");
        SshUtils ssh=new SshUtils();
        ssh.createSession(user);
        List<String> results=ssh.exec("ls /");
        results.forEach(System.out::println);
//        ssh.sftp("~/sshxcute_err.msg","~/",SshUtils.SFTP_PUT);
        results=ssh.exec("pwd");
        results.forEach(System.out::println);
        ssh.closeSession();
    }
}
