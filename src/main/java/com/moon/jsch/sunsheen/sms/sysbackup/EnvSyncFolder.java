package com.moon.jsch.sunsheen.sms.sysbackup;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.util.*;

public class EnvSyncFolder {
    private ConnBean cbMain = new ConnBean();
    private ConnBean cbBack = new ConnBean();
    private List<List<String>> mainResults = new ArrayList<List<String>>();
    private List<List<String>> backResults = new ArrayList<List<String>>();
    private List<String> catalogs=new ArrayList<>();
    public EnvSyncFolder(){
         cbMain.setHost("172.18.194.117");
         cbMain.setUser("root");
         cbMain.setPassword("admin");
        cbBack.setHost("172.18.194.191");
        cbBack.setUser("root");
        cbBack.setPassword("admin");
        catalogs.add("/root/test");
        catalogs.add("/root/test1");
        catalogs.add("/root/test2");
    }
    private boolean getFolderInfo(String path, String type) {
        SSHExec ssh = null;
        if (type.equals("main"))
            ssh = SSHExec.getInstance(cbMain);
        else
            ssh = SSHExec.getInstance(cbBack);
        if (ssh.connect()) {
            for (String catalog : catalogs) {
                String[] arr = getPathAndFolder(path);
                String directory = arr[0];
                String folder = arr[1];
                String commnd = "ls -alF " + directory + "|grep -w " + folder
                        + "/";
                CustomTask task = new ExecCommand(commnd);
                Result rs = null;
                try {
                    rs = ssh.exec(task);
                } catch (TaskExecFailException e) {
                    e.printStackTrace();
                }
                if (type.equals("main"))
                    mainResults.add(parseResults(rs));
                else
                    backResults.add(parseResults(rs));
            }
            ssh.disconnect();
            return true;
        } else {
            System.out.println(type.equals("main") ? cbMain.getHost() : cbBack
                    .getHost() + ":连接失败");
            return false;
        }
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
    private List<String> parseResults(Result results) {
        List<String> ret = new ArrayList<>();
        if (results.isSuccess) {
            List<String> fields = new ArrayList<>(Arrays.asList(results.sysout
                    .split(" ")));
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
                ret.add(String.valueOf(u) + g + o);
                ret.add(fields.get(2) + ":" + fields.get(3));
            }
            return ret;
        } else {
            System.out.println("获取文件夹信息错误。");
            ret.add("0");
            return ret;
        }
    }


    public boolean collectMain() {
        for (String catalog : catalogs) {
            if (!getFolderInfo(catalog, "main")) {
                return false;
            }
        }
        mainResults.forEach(x->{
            x.forEach(System.out::print);
            System.out.println();
        });
        return true;
    }
}
