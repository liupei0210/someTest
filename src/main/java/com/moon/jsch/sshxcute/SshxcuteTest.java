package com.moon.jsch.sshxcute;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class SshxcuteTest {
    public static void main(String[] args){
        ConnBean cb=new ConnBean();
        cb.initByPasswd("172.18.194.117","root","admin");
        SSHExec ssh=SSHExec.getInstance(cb);
        ssh.connect();
        CustomTask task=new ExecCommand("bash -v /root/test/sh01.sh");
        Result rs=null;
        try {
            rs=ssh.exec(task);
            System.out.println(rs.isSuccess);
            ssh.disconnect();
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
    }
}
