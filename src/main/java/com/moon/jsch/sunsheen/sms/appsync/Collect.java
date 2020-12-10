package com.moon.jsch.sunsheen.sms.appsync;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Collect {
    private final SSHExec ssh;
    private final String fileFilter;
    private final String folderFilter;
    private final ConnBean cb;
    private final List<String> folderLi = new ArrayList<>();
    private final List<String> fileLi = Collections.synchronizedList(new ArrayList<>());

    public Collect(SSHExec ssh, String fileFilter, String folderFilter, ConnBean cb) {
        this.ssh = ssh;
        this.fileFilter = fileFilter;
        this.folderFilter = folderFilter;
        this.cb = cb;
    }

    public void visit(String path) throws TaskExecFailException, InterruptedException {
        Stack<String> stack = new Stack<>();
        stack.push(path);
        List<String> files = Collections.synchronizedList(new ArrayList<>());
        String f;
        while (!stack.isEmpty()) {
            f = stack.pop();
            Set<String> ret = getLeaves(f);
            for (String s : ret) {
                //文件夹
                if (s.charAt(s.length() - 1) == '/') {
                    folderLi.add(s);
                    stack.push(s);
                }
                //文件
                else {
                    files.add(s);
                }
            }
        }
        //单线程
//        String command;
//        Result rs;
//        for(String file:files){
//            command="md5sum '"+file.trim()+"'";
//            rs=ssh.exec(new ExecCommand(command));
//            if(rs.isSuccess){
//                fileLi.add(rs.sysout);
//            }else {
//                System.out.println(rs.error_msg);
//                System.out.println(rs.rc);
//                System.out.println("获取"+file+" md5值失败");
//                throw new TaskExecFailException("获取"+file+" md5值失败");
//            }
//        }
        //多线程
        ExecutorService pool = Executors.newFixedThreadPool(12);
        int n = 100;
        int size = files.size();
        int length;
        for (int i = 0; i < size / n + 1; i++) {
            length = Math.min((i + 1) * n, size);
            System.out.println("i:" + i);
            System.out.println("length:" + length);
            pool.execute(new Md5Thread(files, i * n, length));
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
        }

        for (String s : files) {
            if (s.equals("")) {
                System.out.println("files空数据");
                break;
            }
        }
        folderLi.forEach(System.out::println);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (String s : fileLi) {
            if (s.equals("")) {
                System.out.println("空数据");
                break;
            } else {
                System.out.println(s);
            }
        }
        System.out.println("文件夹个数:" + folderLi.size());
        System.out.println("文件个数:" + files.size());
        System.out.println("文件个数:" + fileLi.size());
    }

    private Set<String> getLeaves(String path) throws TaskExecFailException {
        Set<String> ret = new HashSet<>();
        Result rs;
        //获取文件
        String command = "ls -F --file-type " + path + "|grep -v '/$'|grep -v '@$'|grep -v '^$'" + fileFilter;
        rs = ssh.exec(new ExecCommand(command));
        if (rs.rc == 0) {
            for (String f : rs.sysout.split("\n")) {
                if (!"".equals(f)) {
                    ret.add(path + f);
                }
            }
        } else if (rs.rc == 1) {
//            System.out.println(path+"下没有文件");
        } else {
            System.out.println(rs.error_msg);
            throw new TaskExecFailException("获取" + path + "下文件失败");
        }
        //获取文件夹
        if (path.charAt(path.length() - 1) == '/') {
            command = "ls -F --file-type " + path + "|grep '/$'|grep -v '^$'" + folderFilter;
            rs = ssh.exec(new ExecCommand(command));
            if (rs.rc == 0) {
                for (String f : rs.sysout.split("\n")) {
                    if (!"".equals(f)) {
                        ret.add(path + f);
                    }
                }
            } else if (rs.rc == 1) {
//            System.out.println(path+"下没有文件夹");
            } else {
                System.out.println(rs.error_msg);
                throw new TaskExecFailException("获取" + path + "下文件夹失败");
            }
        }
        return ret;
    }

    class Md5Thread implements Runnable {
        private final List<String> files;
        private int i;
        private final int n;

        public Md5Thread(List<String> files, int i, int n) {
            this.files = files;
            this.i = i;
            this.n = n;
        }

        @Override
        public void run() {
            SSHExec ssh = SSHExec.getInstance(cb);
            Result rs;
            String command;
            if (ssh.connect()) {
                for (; i < n; i++) {
                    command = "md5sum '" + files.get(i).trim() + "'";
                    try {
                        rs = ssh.exec(new ExecCommand(command));
                        if (rs.isSuccess) {
                            fileLi.add(rs.sysout);
                        } else {
                            System.out.println(rs.error_msg);
                            ssh.disconnect();
                        }
                    } catch (TaskExecFailException e) {
                        e.printStackTrace();
                    }
                }
                ssh.disconnect();
            } else {
                System.out.println("连接到" + cb.getHost() + "失败");
            }
        }
    }
}
