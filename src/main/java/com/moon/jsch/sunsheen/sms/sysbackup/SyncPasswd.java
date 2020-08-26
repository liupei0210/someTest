package com.moon.jsch.sunsheen.sms.sysbackup;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    public SyncPasswd(MyUserInfo userPrimary, MyUserInfo userBackup) {
        this.userPrimary = userPrimary;
        this.userBackup = userBackup;
    }

    public Map<String, String> start() {
        copyFile();
        sortFileContent();
        diffAndParseGroup();
        diffAndParsePasswd();
        script.append(groupAdd).append(groupChg).append(userAdd).append(userChg).append(userDel).append(groupDel).append("exit 0");
        System.out.println(script);
        return null;
    }

    //去主中心和备份中心拷贝group,passwd文件
    private void copyFile() {
        SshUtils ssh = new SshUtils();
        ssh.createSession(userPrimary);
        try {
            if (!Files.exists(Paths.get("~/liupei/test/sms/passwd/")))
                Files.createDirectories(Paths.get("~/liupei/test/sms/passwd/"));
            else
                Files.walk(Paths.get("~/liupei/test/sms/passwd/")).filter(Files::isRegularFile).forEach(x -> {
                    try {
                        Files.deleteIfExists(x);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        ssh.sftp("/etc/group", "~/liupei/test/sms/passwd/group_p", SshUtils.SFTP_GET);
        ssh.sftp("/etc/passwd", "~/liupei/test/sms/passwd/passwd_p", SshUtils.SFTP_GET);
        ssh.closeSession();
        ssh.createSession(userBackup);
        ssh.sftp("/etc/group", "~/liupei/test/sms/passwd/group_b", SshUtils.SFTP_GET);
        ssh.sftp("/etc/passwd", "~/liupei/test/sms/passwd/passwd_b", SshUtils.SFTP_GET);
        ssh.closeSession();
    }

    //对文件内容进行排序
    private void sortFileContent() {
        try {
            Files.walk(Paths.get("~/liupei/test/sms/passwd/")).filter(Files::isRegularFile).collect(Collectors.toSet()).forEach(x -> {
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
        }
    }

    //对比group文件并根据结果生成脚本命令
    private void diffAndParseGroup() {
        MyUserInfo myself = new MyUserInfo();
        myself.setUser("liupei");
        myself.setPassword("liupei0210");
        SshUtils ssh = new SshUtils();
        ssh.createSession(myself);
        List<String> results = ssh.exec("diff /home/liupei/IdeaProjects/someTest/~/liupei/test/sms/passwd/group_b /home/liupei/IdeaProjects/someTest/~/liupei/test/sms/passwd/group_p");
        ssh.closeSession();
        results.forEach(System.out::println);
        int exitStatus = Integer.parseInt(results.get(0));
        if (exitStatus == 0) {
            log.info("No difference was found in the comparison results. SyncPasswd:diffAndParseGroup()");
        } else if (exitStatus == 1) {
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
                                groupAdd.append("groupadd -fo -g " + fields[2] + " " + fields[0] + "\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            groupAdd.append("groupadd -fo -g " + fields[2] + " " + fields[0] + "\n");
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
                                groupDel.append("groupdel " + fields[0] + "\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            groupDel.append("groupdel " + fields[0] + "\n");
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
                                        groupChg.append("groupmod -o -g " + rearList.get(iii)[2] + " " + frontList.get(0)[0] + "\n");
                                    }
                                    frontList.remove(0);
                                    rearList.remove(iii);
                                    break;
                                }
                            }
                            frontList.forEach(x -> {
                                groupDel.append("groupdel " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                groupAdd.append("groupadd -fo -g " + x[2] + " " + x[0] + "\n");
                            });
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
                                            groupChg.append("groupmod -o -g " + rearList.get(iiii)[2] + " " + frontList.get(iii)[0] + "\n");
                                        }
                                        frontList.remove(iii);
                                        rearList.remove(iiii);
                                        iii--;
                                        break;
                                    }
                                }
                            }
                            frontList.forEach(x -> {
                                groupDel.append("groupdel " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                groupAdd.append("groupadd -fo -g " + x[2] + " " + x[0] + "\n");
                            });
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
                                        groupChg.append("groupmod -o -g " + rearList.get(0)[2] + " " + frontList.get(iii)[0] + "\n");
                                    }
                                    frontList.remove(iii);
                                    rearList.remove(0);
                                    break;
                                }
                            }
                            frontList.forEach(x -> {
                                groupDel.append("groupdel " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                groupAdd.append("groupadd -fo -g " + x[2] + " " + x[0] + "\n");
                            });
                        } else {
                            String[] fields_b = results.get(i + 1).substring(2).split(":");
                            String[] fields_p = results.get(i + 3).substring(2).split(":");
                            if (!fields_b[0].equals(fields_p[0]) && fields_b[2].equals(fields_p[2])) {
                                groupChg.append("groupmod -n " + fields_p[0] + " " + fields_b[0] + "\n");
                            } else if (fields_b[0].equals(fields_p[0]) && !fields_b[2].equals(fields_p[2])) {
                                groupChg.append("groupmod -o -g " + fields_p[2] + " " + fields_b[0] + "\n");
                            } else if (!fields_b[0].equals(fields_p[0]))
                                groupChg.append("groupmod -o -g " + fields_p[2] + " -n " + fields_p[0] + " " + fields_b[0] + "\n");
                            i += 1 + 3;
                        }
                        break;
                    default:
                        log.error("Error!");
                }
            }
        } else log.error("This comparison is wrong! SyncPasswd:diffAndParseGroup()");
    }

    //对比passwd文件并根据结果生成脚本命令
    private void diffAndParsePasswd() {
        MyUserInfo myself = new MyUserInfo();
        myself.setUser("liupei");
        myself.setPassword("liupei0210");
        SshUtils ssh = new SshUtils();
        ssh.createSession(myself);
        List<String> results = ssh.exec("diff /home/liupei/IdeaProjects/someTest/~/liupei/test/sms/passwd/passwd_b /home/liupei/IdeaProjects/someTest/~/liupei/test/sms/passwd/passwd_p");
        ssh.closeSession();
        results.forEach(System.out::println);
        int exitStatus = Integer.parseInt(results.get(0));
        if (exitStatus == 0) {
            log.info("No difference was found in the comparison results. SyncPasswd:diffAndParsePasswd()");
        } else if (exitStatus == 1) {
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
                                userAdd.append("useradd -o -u " + fields[2] + " -d " + fields[5] + " -s " + fields[6] + " -g " + fields[3] + " -c \"" + fields[4] + "\" " + fields[0] + "\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            userAdd.append("useradd -o -u " + fields[2] + " -d " + fields[5] + " -s " + fields[6] + " -g " + fields[3] + " -c \"" + fields[4] + "\" " + fields[0] + "\n");
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
                                userDel.append("userdel -r " + fields[0] + "\n");
                            }
                            i = ii;
                        } else {
                            fields = results.get(i + 1).substring(2).split(":");
                            userDel.append("userdel -r " + fields[0] + "\n");
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
                                    userChg.append("usermod -o -u " + rearList.get(iii)[2] + " -d " + rearList.get(iii)[5] + " -s " + rearList.get(iii)[6] + " -g " + rearList.get(iii)[3] + " -c \"" + rearList.get(iii)[4] + "\" " + rearList.get(iii)[0] + "\n");
                                    frontList.remove(0);
                                    rearList.remove(iii);
                                    break;
                                }
                            }
                            frontList.forEach(x -> {
                                userDel.append("userdel -r " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                userAdd.append("useradd -o -u " + x[2] + " -d " + x[5] + " -s " + x[6] + " -g " + x[3] + " -c \"" + x[4] + "\" " + x[0] + "\n");
                            });
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
                                        userChg.append("usermod -o -u " + rearList.get(iiii)[2] + " -d " + rearList.get(iiii)[5] + " -s " + rearList.get(iiii)[6] + " -g " + rearList.get(iiii)[3] + " -c \"" + rearList.get(iiii)[4] + "\" " + rearList.get(iiii)[0] + "\n");
                                        frontList.remove(iii);
                                        rearList.remove(iiii);
                                        iii--;
                                        break;
                                    }
                                }
                            }
                            frontList.forEach(x -> {
                                userDel.append("userdel -r " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                userAdd.append("useradd -o -u " + x[2] + " -d " + x[5] + " -s " + x[6] + " -g " + x[3] + " -c \"" + x[4] + "\" " + x[0] + "\n");
                            });
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
                                    userChg.append("usermod -o -u " + rearList.get(0)[2] + " -d " + rearList.get(0)[5] + " -s " + rearList.get(0)[6] + " -g " + rearList.get(0)[3] + " -c \"" + rearList.get(0)[4] + "\" " + rearList.get(0)[0] + "\n");
                                    frontList.remove(iii);
                                    rearList.remove(0);
                                    break;
                                }
                            }
                            frontList.forEach(x -> {
                                userDel.append("userdel -r " + x[0] + "\n");
                            });
                            rearList.forEach(x -> {
                                userAdd.append("useradd -o -u " + x[2] + " -d " + x[5] + " -s " + x[6] + " -g " + x[3] + " -c \"" + x[4] + "\" " + x[0] + "\n");
                            });
                        } else {
                            String[] fields_b = results.get(i + 1).substring(2).split(":");
                            String[] fields_p = results.get(i + 3).substring(2).split(":");
                            if (fields_b[0].equals(fields_p[0])) {
                                userChg.append("usermod -o -u " + fields_p[2] + " -d " + fields_p[5] + " -s " + fields_p[6] + " -g " + fields_p[3] + " -c \"" + fields_p[4] + "\" " + fields_p[0] + "\n");
                            } else {
                                userDel.append("userdel -r " + fields_b[0] + "\n");
                                userAdd.append("useradd -o -u " + fields_p[2] + " -d " + fields_p[5] + " -s " + fields_p[6] + " -g " + fields_p[3] + " -c \"" + fields_p[4] + "\" " + fields_p[0] + "\n");
                            }
                            i += 1 + 3;
                        }
                        break;
                    default:
                        log.error("Error!");
                }
            }
        } else log.error("This comparison is wrong! SyncPasswd:diffAndParsePasswd()");
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