package com.moon.jsch.sshxcute;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SshxcuteTest {
    public static void main(String[] args){
        ConnBean cb=new ConnBean();
        cb.initByPasswd("172.18.194.117","root","admin");
        SSHExec ssh=SSHExec.getInstance(cb);
        ssh.connect();
        CustomTask task_group=new ExecCommand("cat /etc/group");
        CustomTask task_passwd=new ExecCommand("cat /etc/passwd");
        Result rs=null;
        try {
            rs=ssh.exec(task_group);
//            ssh.uploadSingleDataToServer();
//            System.out.println(rs.sysout);
            String group=rs.sysout;
            rs=ssh.exec(task_passwd);
            String passwd=rs.sysout;
            List<String> list_group= Arrays.asList(group.split("\n"));
            List<String> list_passwd= Arrays.asList(passwd.split("\n"));
//            list.forEach(System.out::println);
//            System.out.println(list.get(1));
            ssh.disconnect();
            Collections.sort(list_group);
            Collections.sort(list_passwd);
            Files.write(Paths.get("/home/liupei/liupei/test/group"),list_group);
            Files.write(Paths.get("/home/liupei/liupei/test/passwd"),list_passwd);
        } catch (TaskExecFailException | IOException e) {
            e.printStackTrace();
        }
    }
}
