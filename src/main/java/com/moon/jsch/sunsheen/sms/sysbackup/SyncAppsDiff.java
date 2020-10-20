package com.moon.jsch.sunsheen.sms.sysbackup;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.util.*;

public class SyncAppsDiff {
    public final StringBuffer script = new StringBuffer("#!/bin/bash\n");
    private boolean isChange = false;
    private String localPath;

    public SyncAppsDiff(String localPath) {
        this.localPath = localPath;
    }

    public void loop(SSHExec ssh, String path) {
        //获取主和备path中的文件名
        Set<String> mainFiles = getSet(ssh, localPath + "/172.18.194.117/" + path, "file");
        Set<String> backFiles = getSet(ssh, localPath + "/172.18.194.191/" + path, "file");
        //获取主和备path中的文件夹名
        Set<String> mainFolders = getSet(ssh, localPath + "/172.18.194.117/" + path, "folder");
        Set<String> backFolders = getSet(ssh, localPath + "/172.18.194.191/" + path, "folder");
        //对比文件内容
        mainFiles.forEach(x -> {
            if (backFiles.contains(x)) {
                backFiles.remove(x);
                CustomTask task = new ExecCommand("diff " + localPath + "/172.18.194.191/" + path + "/" + x + " " + localPath + "/172.18.194.117/" + path + "/" + x);
                Result rs=null;
                try {
                    rs=ssh.exec(task);
                } catch (TaskExecFailException e) {
                    e.printStackTrace();
                }
                parseResult(rs,path+"/"+x);
            } else {
                //scp文件到备份主机
                script.append("scp "+"172.18.194.117:"+path+"/"+x+" "+path+"/"+x+"\n");
            }
        });
        backFiles.forEach(x->{
            //删除多余的文件
            script.append("rm -rf "+path+"/"+x+"\n");
        });
        //对比文件夹
        mainFolders.forEach(x->{
            if(backFolders.contains(x)){
                loop(ssh,path+"/"+x);
                backFolders.remove(x);
            }else {
                script.append("scp -r "+"172.18.194.117:"+path+"/"+x+" "+path+"/"+x+"\n");
            }
        });
        backFolders.forEach(x->{
            script.append("rm -rf "+path+"/"+x+"\n");
        });
    }

    //获取path下所有文件或文件夹
    private Set<String> getSet(SSHExec ssh, String absPath, String type) {
        Set<String> ret = new HashSet<>();
        CustomTask task = null;
        Result rs = null;
        String command = "";
        if (type.equals("file"))
            command = "ls -l " + absPath + "|grep ^-";
        else
            command = "ls -l " + absPath + "|grep ^d";
        task = new ExecCommand(command);
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
    private boolean parseResult(Result rs,String pathAndName){
        if (rs.rc == 0) {
            return true;
        } else if (rs.rc == 1) {
            isChange = true;
            List<String> results = Arrays.asList(rs.sysout.split("\n"));
            script.append("sed -i ");
            int i = 0;
            String[] splitLine;
            String front;
            String rear;
            String line;
            while (i < results.size()) {
                StringBuilder rows = new StringBuilder();
                line = results.get(i);
                switch (Objects.requireNonNull(which_acd(line))) {
                    case "add":
                        splitLine = line.split("a");
                        front = splitLine[0];
                        rear = splitLine[1];
                        if (rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            int length = Integer.parseInt(rearSplit[1])
                                    - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                rows.append(results.get(ii).substring(2));
                                if (ii + 1 <= i + length)
                                    rows.append("\\n");
                            }
                            script.append("-e '" + (Integer.parseInt(front) + 1)
                                    + "i " + rows + "' ");
                            i = ii;
                        } else {
                            script.append("-e '" + (Integer.parseInt(front) + 1)
                                    + "i " + results.get(i + 1).substring(2) + "' ");
                            i += 2;
                        }
                        break;
                    case "delete":
                        splitLine = line.split("d");
                        front = splitLine[0];
                        if (front.contains(",")) {
                            String[] frontSplit = front.split(",");
                            int length = Integer.parseInt(frontSplit[1])
                                    - Integer.parseInt(frontSplit[0]) + 1;
                            script.append("-e '" + front + "d' ");
                            i += length + 1;
                        } else {
                            script.append("-e '" + front + "d' ");
                            i += 2;
                        }
                        break;
                    case "change":
                        splitLine = line.split("c");
                        front = splitLine[0];
                        rear = splitLine[1];
                        if (!front.contains(",") && rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            int length = Integer.parseInt(rearSplit[1])
                                    - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 3;
                            for (; ii < i + length + 3; ii++) {
                                rows.append(results.get(ii).substring(2));
                                if (ii + 1 < i + length + 3)
                                    rows.append("\\n");
                            }
                            script.append("-e '" + front + "c " + rows + "' ");
                            i = ii;
                        } else if (front.contains(",") && rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            String[] rearSplit = rear.split(",");
                            int frontLength = Integer.parseInt(frontSplit[1])
                                    - Integer.parseInt(frontSplit[0]) + 1;
                            int rearLength = Integer.parseInt(rearSplit[1])
                                    - Integer.parseInt(rearSplit[0]) + 1;
                            i += 2 + frontLength;
                            int ii = i;
                            for (; ii < i + rearLength; ii++) {
                                rows.append(results.get(ii).substring(2));
                                if (ii + 1 < i + rearLength)
                                    rows.append("\\n");
                            }
                            script.append("-e '" + front + "c " + rows + "' ");
                            i = ii;
                        } else if (front.contains(",") && !rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            int frontLength = Integer.parseInt(frontSplit[1])
                                    - Integer.parseInt(frontSplit[0]) + 1;
                            i += 2 + frontLength;
                            rows.append(results.get(i).substring(2));
                            script.append("-e '" + front + "c " + rows + "' ");
                            i += 1;
                        } else {
                            i += 3;
                            rows.append(results.get(i).substring(2));
                            script.append("-e '" + front + "c " + rows + "' ");
                            i += 1;
                        }
                        break;
                    default:
                        break;
                }
            }
            script.append(pathAndName + "\n");
            return true;
        } else {
            return false;
        }

    }
    private String which_acd(String str) {
        if (str.contains("a")) {
            return "add";
        } else if (str.contains("d")) {
            return "delete";
        } else if (str.contains("c")) {
            return "change";
        } else
            return null;
    }

}
