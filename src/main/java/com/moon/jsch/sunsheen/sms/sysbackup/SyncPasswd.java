package com.moon.jsch.sunsheen.sms.sysbackup;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
/*
* 执行过程:
* 1.将需要对比的group\passwd分别从主中心和备份中心拷贝到web服务器system_sync/passwd/下
* 2.将文件内容进行排序
* 3.执行diff命令对比,并获取对比结果
* 4.对比结果解析,a即是新增,d即是删除,c即是修改
* 5.修改策略:先将字符串按":"切割,第0个数据是名称.备份中心和主中心一行一行按照名称对比,发现相同的则生成修改命令,并将相同的数据移除.最后备份中心剩下的数据
* 生成删除命令,主中心剩下的数据生成新增数据.
* */
public class SyncPasswd {
    private final static Logger log = Logger.getLogger(SyncPasswd.class);
    private final StringBuffer script = new StringBuffer("#!/bin/bash\n");
    private final StringBuffer groupAdd = new StringBuffer();
    private final StringBuffer groupChg = new StringBuffer();
    private final StringBuffer groupDel = new StringBuffer();
    private final StringBuffer userAdd = new StringBuffer();
    private final StringBuffer userChg = new StringBuffer();
    private final StringBuffer userDel = new StringBuffer();
    private final MyUserInfo userPrimary;
    private final MyUserInfo userBackup;
    private boolean isChange=false;

    public SyncPasswd(MyUserInfo userPrimary, MyUserInfo userBackup) {
        this.userPrimary = userPrimary;
        this.userBackup = userBackup;
    }

    public Map<String, String> start() {
        Map<String, String> ret = new HashMap<>();
        if (copyFile()) {
            log.info("拷贝文件完成.");
            if (sortFileContent()) {
                log.info("文件内容排序完成.");
                if (diffAndParseGroup()) {
                    log.info("比对group文件完成.");
                    if (diffAndParsePasswd()) {
                        ret.put("retCode", "1");
                        ret.put("retMsg", "比对passwd文件成功.");
                        log.info("比对passwd文件成功.");
                    } else {
                        ret.put("retCode", "0");
                        ret.put("retMsg", "比对passwd文件时出现错误!");
                        log.error("比对passwd文件时出现错误!");
                    }
                } else {
                    ret.put("retCode", "0");
                    ret.put("retMsg", "比对group文件时出现错误!");
                    log.error("比对group文件时出现错误!");
                }
            } else {
                ret.put("retCode", "0");
                ret.put("retMsg", "文件内容排序时出现错误!");
                log.error("文件内容排序时出现错误!");
            }
        } else {
            ret.put("retCode", "0");
            ret.put("retMsg", "拷贝文件时出现错误!");
            log.error("拷贝文件时出现错误!");
        }
        generateScript();
        return ret;
    }

    //去主中心和备份中心拷贝group,passwd文件
    private boolean copyFile() {
        SshUtils ssh = new SshUtils();
        ssh.createSession(userPrimary);
        try {
            if (!Files.exists(Paths.get("system_sync/passwd/")))
                Files.createDirectories(Paths.get("system_sync/passwd/"));
            else
                Files.walk(Paths.get("system_sync/passwd/")).filter(Files::isRegularFile).forEach(x -> {
                    try {
                        Files.deleteIfExists(x);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        ssh.sftp("/etc/group", "system_sync/passwd/group_p", SshUtils.SFTP_GET);
        ssh.sftp("/etc/passwd", "system_sync/passwd/passwd_p", SshUtils.SFTP_GET);
        ssh.closeSession();
        ssh.createSession(userBackup);
        ssh.sftp("/etc/group", "system_sync/passwd/group_b", SshUtils.SFTP_GET);
        ssh.sftp("/etc/passwd", "system_sync/passwd/passwd_b", SshUtils.SFTP_GET);
        ssh.closeSession();
        try {
            return Files.walk(Paths.get("system_sync/passwd/")).filter(Files::isRegularFile).collect(Collectors.toSet()).size() == 4;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //对文件内容进行排序
    private boolean sortFileContent() {
        try {
            Files.walk(Paths.get("system_sync/passwd/")).filter(Files::isRegularFile).collect(Collectors.toSet()).forEach(x -> {
                try {
                    List<String> list = Files.readAllLines(x);
                    Collections.sort(list);
                    Files.write(x, list);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //对比group文件并根据结果生成脚本命令
    private boolean diffAndParseGroup() {
        MyUserInfo myself = new MyUserInfo();
        myself.setUser("liupei");
        myself.setPassword("liupei0210");
        SshUtils ssh = new SshUtils();
        ssh.createSession(myself);
        List<String> results = ssh.exec("diff /home/liupei/IdeaProjects/someTest/system_sync/passwd/group_b /home/liupei/IdeaProjects/someTest/system_sync/passwd/group_p");
        ssh.closeSession();
//        results.forEach(System.out::println);
        int exitStatus = Integer.parseInt(results.get(0));
        if (exitStatus == 0) {
            log.info("No difference was found in the comparison results. SyncPasswd:diffAndParseGroup()");
        } else if (exitStatus == 1) {
            isChange=true;
            results.remove(0);
            int i = 0;
            String[] splitLine;
            String[] fields;
            String front, rear, line;
            while (i < results.size()) {
                line = results.get(i);
                switch (Objects.requireNonNull(which_acd(line))) {
                    case "add":
                        splitLine = line.split("a");
                        rear = splitLine[1];
                        if (rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            int length = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                fields = results.get(ii).substring(2).split(":");
                                groupAdd.append("groupadd -fo -g ").append(fields[2]).append(" ").append(fields[0]).append("\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            groupAdd.append("groupadd -fo -g ").append(fields[2]).append(" ").append(fields[0]).append("\n");
                            i += 1 + 1;
                        }
                        break;
                    case "delete":
                        splitLine = line.split("d");
                        front = splitLine[0];
                        if (front.contains(",")) {
                            String[] frontSplit = front.split(",");
                            int length = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                fields = results.get(ii).substring(2).split(":");
                                groupDel.append("groupdel ").append(fields[0]).append("\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            groupDel.append("groupdel ").append(fields[0]).append("\n");
                            i += 1 + 1;
                        }
                        break;
                    case "change":
                        splitLine = line.split("c");
                        front = splitLine[0];
                        rear = splitLine[1];
                        if (!front.contains(",") && rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            frontList.add(results.get(i + 1).substring(2).split(":"));
                            int length = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 3;
                            for (; ii < i + length + 3; ii++) {
                                rearList.add(results.get(ii).substring(2).split(":"));
                            }
                            i = ii;
                            for (int iii = 0; iii < rearList.size(); iii++) {
                                if (rearList.get(iii)[0].equals(frontList.get(0)[0])) {
                                    if (!rearList.get(iii)[2].equals(frontList.get(0)[2])) {
                                        groupChg.append("groupmod -o -g ").append(rearList.get(iii)[2]).append(" ").append(frontList.get(0)[0]).append("\n");
                                    }
                                    frontList.remove(0);
                                    rearList.remove(iii);
                                    break;
                                }
                            }
                            generateCommand(frontList, rearList, "group");
                        } else if (front.contains(",") && rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            String[] rearSplit = rear.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            int frontLength = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int rearLength = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + frontLength; ii++) {
                                frontList.add(results.get(ii).substring(2).split(":"));
                            }
                            ii += 1;
                            i = ii;
                            for (; ii < i + rearLength; ii++) {
                                rearList.add(results.get(ii).substring(2).split(":"));
                            }
                            i = ii;
                            for (int iii = 0; iii < frontList.size(); iii++) {
                                for (int iiii = 0; iiii < rearList.size(); iiii++) {
                                    if (frontList.get(iii)[0].equals(rearList.get(iiii)[0])) {
                                        if (!frontList.get(iii)[2].equals(rearList.get(iiii)[2])) {
                                            groupChg.append("groupmod -o -g ").append(rearList.get(iiii)[2]).append(" ").append(frontList.get(iii)[0]).append("\n");
                                        }
                                        frontList.remove(iii);
                                        rearList.remove(iiii);
                                        iii--;
                                        break;
                                    }
                                }
                            }
                            generateCommand(frontList, rearList, "group");
                        } else if (front.contains(",") && !rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            int length = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                frontList.add(results.get(ii).substring(2).split(":"));
                            }
                            ii += 1;
                            rearList.add(results.get(ii).substring(2).split(":"));
                            i = ii + 1;
                            for (int iii = 0; iii < frontList.size(); iii++) {
                                if (frontList.get(iii)[0].equals(rearList.get(0)[0])) {
                                    if (!frontList.get(iii)[2].equals(rearList.get(0)[2])) {
                                        groupChg.append("groupmod -o -g ").append(rearList.get(0)[2]).append(" ").append(frontList.get(iii)[0]).append("\n");
                                    }
                                    frontList.remove(iii);
                                    rearList.remove(0);
                                    break;
                                }
                            }
                            generateCommand(frontList, rearList, "group");
                        } else {
                            String[] fields_b = results.get(i + 1).substring(2).split(":");
                            String[] fields_p = results.get(i + 3).substring(2).split(":");
                            if (!fields_b[0].equals(fields_p[0]) && fields_b[2].equals(fields_p[2])) {
                                groupChg.append("groupmod -n ").append(fields_p[0]).append(" ").append(fields_b[0]).append("\n");
                            } else if (fields_b[0].equals(fields_p[0]) && !fields_b[2].equals(fields_p[2])) {
                                groupChg.append("groupmod -o -g ").append(fields_p[2]).append(" ").append(fields_b[0]).append("\n");
                            } else if (!fields_b[0].equals(fields_p[0]))
                                groupChg.append("groupmod -o -g ").append(fields_p[2]).append(" -n ").append(fields_p[0]).append(" ").append(fields_b[0]).append("\n");
                            i += 1 + 3;
                        }
                        break;
                    default:
                        log.error("Error!");
                        return false;
                }
            }
        } else {
            log.error("This comparison is wrong! SyncPasswd:diffAndParseGroup()");
            return false;
        }
        return true;
    }

    //对比passwd文件并根据结果生成脚本命令
    private boolean diffAndParsePasswd() {
        MyUserInfo myself = new MyUserInfo();
        myself.setUser("liupei");
        myself.setPassword("liupei0210");
        SshUtils ssh = new SshUtils();
        ssh.createSession(myself);
        List<String> results = ssh.exec("diff /home/liupei/IdeaProjects/someTest/system_sync/passwd/passwd_b /home/liupei/IdeaProjects/someTest/system_sync/passwd/passwd_p");
        ssh.closeSession();
//        results.forEach(System.out::println);
        int exitStatus = Integer.parseInt(results.get(0));
        if (exitStatus == 0) {
            log.info("No difference was found in the comparison results. SyncPasswd:diffAndParsePasswd()");
        } else if (exitStatus == 1) {
            isChange=true;
            results.remove(0);
            int i = 0;
            String[] splitLine;
            String[] fields;
            String front, rear, line;
            while (i < results.size()) {
                line = results.get(i);
                switch (Objects.requireNonNull(which_acd(line))) {
                    case "add":
                        splitLine = line.split("a");
                        rear = splitLine[1];
                        if (rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            int length = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                fields = results.get(ii).substring(2).split(":");
                                userAdd.append("useradd -o -u ").append(fields[2]).append(" -d ").append(fields[5]).append(" -s ").append(fields[6]).append(" -g ").append(fields[3]).append(" -c \"").append(fields[4]).append("\" ").append(fields[0]).append("\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            userAdd.append("useradd -o -u ").append(fields[2]).append(" -d ").append(fields[5]).append(" -s ").append(fields[6]).append(" -g ").append(fields[3]).append(" -c \"").append(fields[4]).append("\" ").append(fields[0]).append("\n");
                            i += 1 + 1;
                        }
                        break;
                    case "delete":
                        splitLine = line.split("d");
                        front = splitLine[0];
                        if (front.contains(",")) {
                            String[] frontSplit = front.split(",");
                            int length = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                fields = results.get(ii).substring(2).split(":");
                                userDel.append("userdel -r ").append(fields[0]).append("\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            userDel.append("userdel -r ").append(fields[0]).append("\n");
                            i += 1 + 1;
                        }
                        break;
                    case "change":
                        splitLine = line.split("c");
                        front = splitLine[0];
                        rear = splitLine[1];
                        if (!front.contains(",") && rear.contains(",")) {
                            String[] rearSplit = rear.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            frontList.add(results.get(i + 1).substring(2).split(":"));
                            int length = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 3;
                            for (; ii < i + length + 3; ii++) {
                                rearList.add(results.get(ii).substring(2).split(":"));
                            }
                            i = ii;
                            for (int iii = 0; iii < rearList.size(); iii++) {
                                if (rearList.get(iii)[0].equals(frontList.get(0)[0])) {
                                    userChg.append("usermod -o -u ").append(rearList.get(iii)[2]).append(" -d ").append(rearList.get(iii)[5]).append(" -s ").append(rearList.get(iii)[6]).append(" -g ").append(rearList.get(iii)[3]).append(" -c \"").append(rearList.get(iii)[4]).append("\" ").append(rearList.get(iii)[0]).append("\n");
                                    frontList.remove(0);
                                    rearList.remove(iii);
                                    break;
                                }
                            }
                            generateCommand(frontList, rearList, "passwd");
                        } else if (front.contains(",") && rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            String[] rearSplit = rear.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            int frontLength = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int rearLength = Integer.parseInt(rearSplit[1]) - Integer.parseInt(rearSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + frontLength; ii++) {
                                frontList.add(results.get(ii).substring(2).split(":"));
                            }
                            ii += 1;
                            i = ii;
                            for (; ii < i + rearLength; ii++) {
                                rearList.add(results.get(ii).substring(2).split(":"));
                            }
                            i = ii;
                            for (int iii = 0; iii < frontList.size(); iii++) {
                                for (int iiii = 0; iiii < rearList.size(); iiii++) {
                                    if (frontList.get(iii)[0].equals(rearList.get(iiii)[0])) {
                                        userChg.append("usermod -o -u ").append(rearList.get(iiii)[2]).append(" -d ").append(rearList.get(iiii)[5]).append(" -s ").append(rearList.get(iiii)[6]).append(" -g ").append(rearList.get(iiii)[3]).append(" -c \"").append(rearList.get(iiii)[4]).append("\" ").append(rearList.get(iiii)[0]).append("\n");
                                        frontList.remove(iii);
                                        rearList.remove(iiii);
                                        iii--;
                                        break;
                                    }
                                }
                            }
                            generateCommand(frontList, rearList, "passwd");
                        } else if (front.contains(",") && !rear.contains(",")) {
                            String[] frontSplit = front.split(",");
                            List<String[]> frontList = new ArrayList<>();
                            List<String[]> rearList = new ArrayList<>();
                            int length = Integer.parseInt(frontSplit[1]) - Integer.parseInt(frontSplit[0]) + 1;
                            int ii = i + 1;
                            for (; ii <= i + length; ii++) {
                                frontList.add(results.get(ii).substring(2).split(":"));
                            }
                            ii += 1;
                            rearList.add(results.get(ii).substring(2).split(":"));
                            i = ii + 1;
                            for (int iii = 0; iii < frontList.size(); iii++) {
                                if (frontList.get(iii)[0].equals(rearList.get(0)[0])) {
                                    userChg.append("usermod -o -u ").append(rearList.get(0)[2]).append(" -d ").append(rearList.get(0)[5]).append(" -s ").append(rearList.get(0)[6]).append(" -g ").append(rearList.get(0)[3]).append(" -c \"").append(rearList.get(0)[4]).append("\" ").append(rearList.get(0)[0]).append("\n");
                                    frontList.remove(iii);
                                    rearList.remove(0);
                                    break;
                                }
                            }
                            generateCommand(frontList, rearList, "passwd");
                        } else {
                            String[] fields_b = results.get(i + 1).substring(2).split(":");
                            String[] fields_p = results.get(i + 3).substring(2).split(":");
                            if (fields_b[0].equals(fields_p[0])) {
                                userChg.append("usermod -o -u ").append(fields_p[2]).append(" -d ").append(fields_p[5]).append(" -s ").append(fields_p[6]).append(" -g ").append(fields_p[3]).append(" -c \"").append(fields_p[4]).append("\" ").append(fields_p[0]).append("\n");
                            } else {
                                userDel.append("userdel -r ").append(fields_b[0]).append("\n");
                                userAdd.append("useradd -o -u ").append(fields_p[2]).append(" -d ").append(fields_p[5]).append(" -s ").append(fields_p[6]).append(" -g ").append(fields_p[3]).append(" -c \"").append(fields_p[4]).append("\" ").append(fields_p[0]).append("\n");
                            }
                            i += 1 + 3;
                        }
                        break;
                    default:
                        log.error("Error!");
                        return false;
                }
            }
        } else {
            log.error("This comparison is wrong! SyncPasswd:diffAndParsePasswd()");
            return false;
        }
        return true;
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

    private void generateCommand(List<String[]> frontList, List<String[]> rearList, String type) {
        if (type.equals("group")) {
            frontList.forEach(x -> groupDel.append("groupdel ").append(x[0]).append("\n"));
            rearList.forEach(x -> groupAdd.append("groupadd -fo -g ").append(x[2]).append(" ").append(x[0]).append("\n"));
        } else {
            frontList.forEach(x -> userDel.append("userdel -r ").append(x[0]).append("\n"));
            rearList.forEach(x -> userAdd.append("useradd -o -u ").append(x[2]).append(" -d ").append(x[5]).append(" -s ").append(x[6]).append(" -g ").append(x[3]).append(" -c \"").append(x[4]).append("\" ").append(x[0]).append("\n"));
        }
    }
    public boolean generateScript(){
        if(isChange){
            script.append(groupAdd).append(groupChg).append(userAdd).append(userChg).append(userDel).append("exit 0");
//        System.out.println(script);
            String fileName="changepasswd_"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".sh";
            try {
                Files.write(Paths.get("system_sync/passwd/"+fileName),script.toString().getBytes());
                log.info("生成脚本:"+fileName+"成功.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                log.error("生成脚本:"+fileName+"失败!");
                return false;
            }
        }else {
            log.info("本次比对passwd文件没有发现不同.");
            return true;
        }
    }
}