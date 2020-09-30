package com.moon.jsch.sunsheen.sms.sysbackup;

import com.jcraft.jsch.Session;
import net.neoremind.sshxcute.core.ConnBean;

import java.util.List;

public class test {
    public static void main(String[] args){
        EnvSyncFolder esf=new EnvSyncFolder();
        esf.collectMain();
    }
}
