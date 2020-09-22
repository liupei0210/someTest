package com.moon.jsch.sunsheen.sms.sysbackup;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SyncRoute {
    Logger log=Logger.getLogger(SyncRoute.class);
    private StringBuffer script = new StringBuffer("#!/bin/bash\n");
    private ConnBean cbPrimary;
    private ConnBean cbBackup;
    public boolean isChange=false;

    public SyncRoute(ConnBean cbPrimary, ConnBean cbBackup) {
        this.cbPrimary=cbPrimary;
        this.cbBackup=cbBackup;
    }
    public boolean start(){
        SSHExec ssh=SSHExec.getInstance(cbPrimary);
        ssh.connect();
        CustomTask task=new ExecCommand("route -n");
        Result rs=null;
        try {
            rs=ssh.exec(task);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        ssh.disconnect();
        List<String> list=new ArrayList<>(Arrays.asList(rs.sysout.split("\n")));
        list.remove(0);
        list.remove(0);
/*        list.stream().map(x->{
            String[] strs=x.replaceAll("\\s+"," ").split(" ");
            String str=strs[0]+" "+strs[1]+" "+strs[2];
            return str;
        }).collect(Collectors.toList()).forEach(System.out::println);*/
        for(int i=0;i<list.size();i++){
            String[] strs=list.get(i).replaceAll("\\s+"," ").split(" ");
            list.set(i,strs[0]+" "+strs[1]+" "+strs[2]);
        }
        Collections.sort(list);
        list.forEach(System.out::println);
        return false;
    }
}
