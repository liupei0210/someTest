package com.moon.jsch.sunsheen.sms.sysbackup;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SyncAppsAcq {
    private String localPath;
    public Map<String,String> binFiles=new HashMap<>();
    public SyncAppsAcq(String localPath){
        this.localPath=localPath;
    }
    public void loop(SSHExec ssh, String path) {
        //获取path下的所有文件名称
        List<String> files = getList(ssh,path,"file");
        //获取path下的所有文件夹名称
        List<String> folders = getList(ssh,path,"folder");
        //在服务器创建path文件存放路径
        if(!Files.exists(Paths.get(localPath+path))){
            try {
                Files.createDirectories(Paths.get(localPath+path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //拷贝path下所有文件
        files.forEach(x->{
            String content=getFileContent(ssh,path,x);
            try {
                Files.write(Paths.get(localPath+"/"+path+"/"+x), Arrays.asList(content.split("\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //递归循环文件夹
        folders.forEach(x->loop(ssh,path+"/"+x));
    }
    //获取path下所有文件或文件夹
    private List<String> getList(SSHExec ssh,String path,String type){
        List<String> ret = new ArrayList<>();
        CustomTask task = null;
        Result rs = null;
        String command="";
        if(type.equals("file"))
            command="ls -l " + path + "|grep ^-";
        else
            command="ls -l " + path + "|grep ^d";
        task=new ExecCommand(command);
        try {
            rs = ssh.exec(task);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        if (rs.isSuccess) {
            for (String results : rs.sysout.split("\n")) {
                String[] fields = results.split(" ");
                String fileName=fields[fields.length - 1];
                if(type.equals("file")){
                    task=new ExecCommand("file "+path+"/"+fileName+"|grep 'text'");
                    try {
                        Result r=ssh.exec(task);
                        if(r.rc==0){
                            ret.add(fileName);
                        }else {
                            task=new ExecCommand("md5sum "+path+"/"+fileName);
                            String md5=ssh.exec(task).sysout;
                            binFiles.put(path+"/"+fileName,md5);
                        }

                    } catch (TaskExecFailException e) {
                        e.printStackTrace();
                    }
                }else {
                    ret.add(fileName);
                }
            }
        }
        return ret;
    }
    //获取文件内容
    private String getFileContent(SSHExec ssh,String path,String fileName){
        CustomTask task = task = new ExecCommand("cat "+path+"/"+fileName);
        Result rs = null;
        try {
            rs = ssh.exec(task);
        } catch (TaskExecFailException e) {
            e.printStackTrace();
        }
        return rs.sysout;
    }
}