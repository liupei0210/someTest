package com.moon.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class NodeInfo {
    public static JsonArray getInfo(String ip, String user, String passwd, String path) {
        //为path最后添加"/"
        path = path.charAt(path.length() - 1) == '/' ? path : path + "/";
        JsonArray ret = new JsonArray();
        ConnBean cb = new ConnBean();
        cb.initByPasswd(ip, user, passwd);
        SSHExec ssh = SSHExec.getInstance(cb);
        if (ssh.connect()) {
            CustomTask task = null;
            Result rs = null;
            try {
                String command = "ls -l " + path + "|grep '^d'|awk '{print $9}'|grep -v '^$'";
                task = new ExecCommand(command);
                rs = ssh.exec(task);
                if (rs.isSuccess) {
                    String[] directories = rs.sysout.split("\n");
                    for (String directory : directories) {
                        JsonObject node = new JsonObject();
                        node.addProperty("id", path + directory+"/");
                        node.addProperty("text", directory);
                        node.addProperty("leaf", false);
                        ret.add(node);
                    }
                }
                command = "ls -l " + path + "|grep '^-'|awk '{print $9}'|grep -v '^$'";
                task = new ExecCommand(command);
                rs = ssh.exec(task);
                if (rs.isSuccess) {
                    String[] directories = rs.sysout.split("\n");
                    for (String file : directories) {
                        JsonObject node = new JsonObject();
                        node.addProperty("id", path + file);
                        node.addProperty("text", file);
                        node.addProperty("leaf", true);
                        ret.add(node);
                    }
                }
            } catch (TaskExecFailException e) {
                e.printStackTrace();
            } finally {
                ssh.disconnect();
            }
        } else {
            return null;
        }
        return ret;
    }

    public static JsonArray getDirectoryInfo(String ip, String user, String passwd, String path) {
        //为path最后添加"/"
        path = path.charAt(path.length() - 1) == '/' ? path : path + "/";
        JsonArray ret = new JsonArray();
        ConnBean cb = new ConnBean();
        cb.initByPasswd(ip, user, passwd);
        SSHExec ssh = SSHExec.getInstance(cb);
        if (ssh.connect()) {
            CustomTask task = null;
            Result rs = null;
            try {
                String command = "ls -l " + path + "|grep '^d'|awk '{print $9}'|grep -v '^$'";
                task = new ExecCommand(command);
                rs = ssh.exec(task);
                if (rs.isSuccess) {
                    String[] directories = rs.sysout.split("\n");
                    for (String directory : directories) {
                        JsonObject node = new JsonObject();
                        node.addProperty("id", path + directory+"/");
                        node.addProperty("text", directory);
                        node.addProperty("leaf", false);
                        ret.add(node);
                    }
                }
            } catch (TaskExecFailException e) {
                e.printStackTrace();
            } finally {
                ssh.disconnect();
            }
        } else {
            return null;
        }
        return ret;
    }
}
