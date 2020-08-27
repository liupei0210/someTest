package com.moon.jsch.sunsheen.sms.sysbackup;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class SyncFolder {
    Logger log = Logger.getLogger(SyncFolder.class);
    private final MyUserInfo userPrimary;
    private final MyUserInfo userBackup;
    private final StringBuffer script=new StringBuffer("#!/bin/bash\n");
    private boolean isChange=false;

    public SyncFolder(MyUserInfo userPrimary, MyUserInfo userBackup) {
        this.userPrimary = userPrimary;
        this.userBackup = userBackup;
    }

    public Map<String, String> start(String path) {
        Map<String, String> ret = new HashMap<>();
        List<String> primary = parseResults(getFolderInfo(path, "primary"));
        List<String> backup =  parseResults(getFolderInfo(path, "backup"));
        if(primary.get(0).equals("1")&&backup.get(0).equals("1")){
            if(!primary.get(1).equals(backup.get(1))){
                script.append("chmod ").append(primary.get(1)).append(" ").append(path).append("\n");
                isChange=true;
                log.info("发现文件夹权限不一致,生成权限一致命令.");
            }
            if(!primary.get(2).equals(backup.get(2))){
                script.append("chown -R ").append(primary.get(2)).append(" ").append(path).append("\n");
                log.info("发现文件夹属主不一致,生成属主一致命令.");
                isChange=true;
            }
            ret.put("retCode", "1");
            ret.put("retMsg", "主/备中心文件夹对比完成.");
            log.info("主/备中心文件夹对比完成.");
        }else if(!primary.get(0).equals("1")&&backup.get(0).equals("1")){
            ret.put("retCode", "0");
            ret.put("retMsg", "主中心:"+path+"不存在或不是文件夹!");
            log.error("主中心:"+path+"不存在或不是文件夹!");
        }else if(primary.get(0).equals("1")&&!backup.get(0).equals("1")){
            ret.put("retCode", "0");
            ret.put("retMsg", "备份中心:"+path+"不存在或不是文件夹!");
            log.error("备份中心:"+path+"不存在或不是文件夹!");
        }else {
            ret.put("retCode", "0");
            ret.put("retMsg", "主中心和备份中心:"+path+"不存在或不是文件夹!");
            log.error("主中心和备份中心:"+path+"不存在或不是文件夹!");
        }
        generateScript();
        return ret;
    }

    private List<String> parseResults(List<String> results) {
        List<String> ret = new ArrayList<>();
        int exitStatus = Integer.parseInt(results.get(0));
        if (exitStatus == 0) {
            results.forEach(System.out::println);
            List<String> fields = new ArrayList<>(Arrays.asList(results.get(1).split(" ")));
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).equals("")) {
                    fields.remove(i);
                    i--;
                }
            }
            char[] chars = fields.get(0).toCharArray();
            if (chars[0] != 'd') {
                ret.add("0");
                return ret;
            } else {
                ret.add("1");
                int u = 0, g = 0, o = 0, i = 1;
                while (i < 10) {
                    if (chars[i] != '-') {
                        if (i <= 3) {
                            switch (i % 3) {
                                case 1:
                                    u += 4;
                                    break;
                                case 2:
                                    u += 2;
                                    break;
                                case 0:
                                    u += 1;
                                    break;
                            }
                        } else if (i <= 6) {
                            switch (i % 3) {
                                case 1:
                                    g += 4;
                                    break;
                                case 2:
                                    g += 2;
                                    break;
                                case 0:
                                    g += 1;
                                    break;
                            }
                        } else {
                            switch (i % 3) {
                                case 1:
                                    o += 4;
                                    break;
                                case 2:
                                    o += 2;
                                    break;
                                case 0:
                                    o += 1;
                                    break;
                            }
                        }
                    }
                    i++;
                }
                ret.add(String.valueOf(u)+g+o);
                ret.add(fields.get(2)+":"+fields.get(3));
            }
        } else {
            log.error("Error!");
            ret.add("0");
            return ret;
        }
        return ret;
    }

    private List<String> getFolderInfo(String path, String type) {
        String[] arr = getPathAndFolder(path);
        String directory = arr[0];
        String folder = arr[1];
        SshUtils ssh = new SshUtils();
        if (type.equals("primary")) ssh.createSession(userPrimary);
        else ssh.createSession(userBackup);
        List<String> results = ssh.exec("ls -alF " + directory + "|grep -w " + folder+"/");
        ssh.closeSession();
        return results;
    }

    private String[] getPathAndFolder(String path) {
        path = Objects.requireNonNull(path);
        String[] arr = new String[2];
        String[] str = path.split("/");
        String folderName = str[str.length - 1];
        arr[0] = path.substring(0, path.length() - folderName.length() - 1);
        arr[1] = folderName;
        return arr;
    }
    public boolean generateScript(){
        if(isChange){
            script.append("exit 0");
        System.out.println(script);
            String fileName="changeFolder_"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".sh";
            try {
                if (!Files.exists(Paths.get("system_sync/folder/")))
                    Files.createDirectories(Paths.get("system_sync/folder/"));
                else
                    Files.walk(Paths.get("system_sync/folder/")).filter(Files::isRegularFile).forEach(x -> {
                        try {
                            Files.deleteIfExists(x);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                Files.write(Paths.get("system_sync/folder/"+fileName),script.toString().getBytes());
                log.info("生成脚本:"+fileName+"成功.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                log.error("生成脚本:"+fileName+"失败!");
                return false;
            }
        }else {
            log.info("本次比对没有发现不同.");
            return true;
        }
    }
}
