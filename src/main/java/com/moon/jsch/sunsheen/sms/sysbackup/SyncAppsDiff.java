package com.moon.jsch.sunsheen.sms.sysbackup;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncAppsDiff {
    private final StringBuffer script=new StringBuffer("#!/bin/bash\n");
    private boolean isChange=false;
    private String localPath;
    public SyncAppsDiff(String localPath){
        this.localPath=localPath;
    }
    public void loop(SSHExec ssh,String path){
        //获取主和备path中的文件名

    }
    //获取path下所有文件或文件夹
    private Set<String> getList(SSHExec ssh, String absPath, String type){
        Set<String> ret = new HashSet<>();
        CustomTask task = null;
        Result rs = null;
        String command="";
        if(type.equals("file"))
            command="ls -l " + absPath + "|grep ^-";
        else
            command="ls -l " + absPath + "|grep ^d";
        task=new ExecCommand(command);
        try {
            rs = ssh.exec(task);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        if (rs.isSuccess) {
            for (String results : rs.sysout.split("\n")) {
                String[] fields = results.split(" ");
                ret.add(fields[fields.length - 1]);
            }
        }
        return ret;
    }
}
