package com.moon.jsch.sunsheen.sms.sysbackup;

import com.jcraft.jsch.Session;

import java.util.List;

public class test {
    public static void main(String[] args){
        MyUserInfo user=new MyUserInfo("172.18.194.117");
        user.setUser("root");
        user.setPassword("admin");
        MyUserInfo user1=new MyUserInfo("172.18.194.191");
        user1.setUser("root");
        user1.setPassword("admin");
        new SyncPasswd(user,user1).start();
    }
}
