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
        cb.setHost("127.0.0.1");
        cb.setUser("liupei");
//        cb.setPublicKey("/home/liupei/liupei/test/id_rsa.rsa");
        cb.setPassword("liupei0210");
        SSHExec ssh=SSHExec.getInstance(cb);
        ssh.connect();
        CustomTask task=new ExecCommand("pwd");
        Result rs=null;
        try {
            rs=ssh.exec(task);
            System.out.println(rs.isSuccess);
            System.out.println(rs.sysout);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        ssh.disconnect();
    }
}
