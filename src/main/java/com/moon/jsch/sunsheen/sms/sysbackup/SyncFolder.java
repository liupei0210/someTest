package com.moon.jsch.sunsheen.sms.sysbackup;

import org.apache.log4j.Logger;

import java.util.*;

public class SyncFolder {
    Logger log = Logger.getLogger(SyncFolder.class);
    private final MyUserInfo userPrimary;
    private final MyUserInfo userBackup;

    public SyncFolder(MyUserInfo userPrimary, MyUserInfo userBackup) {
        this.userPrimary = userPrimary;
        this.userBackup = userBackup;
    }

    public Map<String, String> start(String path) {
        List<String> primary = getFolderInfo(path, "primary");
        List<String> backup = getFolderInfo(path, "backup");
        parseResults(primary).forEach(System.out::println);
//        parseResults(backup).forEach(System.out::println);
        return null;
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
                while (i < chars.length) {
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
        List<String> results = ssh.exec("ls -al " + directory + "|grep -w " + folder);
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
}
