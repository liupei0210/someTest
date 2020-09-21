package com.moon.jsch.sunsheen.sms.sysbackup;

import groovy.lang.Script;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SyncFtp {
    Logger log=Logger.getLogger(SyncFtp.class);
    private StringBuffer script = new StringBuffer("#!/bin/bash\n");
    private ConnBean cbPrimary;
    private ConnBean cbBackup;
    public boolean isChange=false;

    public SyncFtp(ConnBean cbPrimary, ConnBean cbBackup) {
        this.cbPrimary=cbPrimary;
        this.cbBackup=cbBackup;
    }
    public boolean start(){
        ConnBean cb=new ConnBean();
        cb.initByPasswd("172.18.194.191","root","admin");
        SSHExec ssh=SSHExec.getInstance(cb);
        ssh.connect();
        CustomTask task_ftpusers=new ExecCommand("diff /root/ftpusers1 /root/ftpusers");
        CustomTask task_user_list=new ExecCommand("diff /home/liupei/liupei/test/passwd1 /home/liupei/liupei/test/passwd");
        CustomTask task_conf=new ExecCommand("diff /home/liupei/liupei/test/passwd1 /home/liupei/liupei/test/passwd");
        Result rs=null;
        try {
            rs=ssh.exec(task_ftpusers);
            parseResults(rs,"ftpusers");
            System.out.println(script);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        ssh.disconnect();
        return true;
    }
    private void parseResults(Result rs,String fileName){
        if(rs.rc==0){
            log.info(fileName+"文件内容一致.");
        }else if(rs.rc==1){
            isChange=true;
            List<String> results= Arrays.asList(rs.sysout.split("\n"));
            script.append("sed -i ");
            int i=0;
            String[] splitLine;
            String front;
            String rear;
            String line;
            while(i<results.size()){
                StringBuilder rows= new StringBuilder();
                line=results.get(i);
                switch (Objects.requireNonNull(which_acd(line))){
                    case "add":
                        splitLine=line.split("a");
                        front=splitLine[0];
                        rear=splitLine[1];
                        if(rear.contains(",")){
                            String[] rearSplit=rear.split(",");
                            int length=Integer.parseInt(rearSplit[1])-Integer.parseInt(rearSplit[0])+1;
                            int ii=i+1;
                            for(;ii<=i+length;ii++){
                                rows.append(results.get(ii).substring(2));
                                if(ii+1<=i+length) rows.append("\\n");
                            }
                            script.append("-e '"+(Integer.parseInt(front)+1)+"i "+rows+"' ");
                            i=ii;
                        }else {
                            script.append("-e '"+(Integer.parseInt(front)+1)+"i "+results.get(i+1).substring(2)+"' ");
                            i+=2;
                        }
                        break;
                    case "delete":
                        splitLine=line.split("d");
                        front=splitLine[0];
                        if(front.contains(",")){
                            String[] frontSplit=front.split(",");
                            int length=Integer.parseInt(frontSplit[1])-Integer.parseInt(frontSplit[0])+1;
                            script.append("-e '"+front+"d' ");
                            i+=length+1;
                        }else {
                            script.append("-e '"+front+"d' ");
                            i+=2;
                        }
                        break;
                    case "change":
                        splitLine=line.split("c");
                        front=splitLine[0];
                        rear=splitLine[1];
                        if (!front.contains(",") && rear.contains(",")) {
                            String[] rearSplit=rear.split(",");
                            int length=Integer.parseInt(rearSplit[1])-Integer.parseInt(rearSplit[0])+1;
                            int ii=i+3;
                            for(;ii<i+length+3;ii++){
                                rows.append(results.get(ii).substring(2));
                                if(ii+1<i+length+3) rows.append("\\n");
                            }
                            script.append("-e '"+front+"c "+rows+"' ");
                            i=ii;
                        } else if(front.contains(",") && rear.contains(",")){
                            String[] frontSplit = front.split(",");
                            String[] rearSplit = rear.split(",");
                            int frontLength = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int rearLength = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            i+=2+frontLength;
                            int ii=i;
                            for(;ii<i+rearLength;ii++){
                                rows.append(results.get(ii).substring(2));
                                if(ii+1<i+rearLength) rows.append("\\n");
                            }
                            script.append("-e '"+front+"c "+rows+"' ");
                            i=ii;
                        }else if(front.contains(",") && !rear.contains(",")){
                            String[] frontSplit = front.split(",");
                            int frontLength = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            i+=2+frontLength;
                            rows.append(results.get(i).substring(2));
                            script.append("-e '"+front+"c "+rows+"' ");
                            i+=1;
                        }else {
                            i+=3;
                            rows.append(results.get(i).substring(2));
                            script.append("-e '"+front+"c "+rows+"' ");
                            i+=1;
                        }
                        break;
                    default:break;
                }
            }
            script.append(fileName+"\n");
        }else {
            log.error(fileName+"此次对比出现错误!");
        }
    }
    private String which_acd(String str) {
        if (str.contains("a")) {
            return "add";
        } else if (str.contains("d")) {
            return "delete";
        } else if (str.contains("c")) {
            return "change";
        } else return null;
    }
}
