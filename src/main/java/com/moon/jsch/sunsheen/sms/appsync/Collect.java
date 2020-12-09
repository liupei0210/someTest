package com.moon.jsch.sunsheen.sms.appsync;

import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Collect {
    private SSHExec ssh;
    private String fileFilter;
    private String folderFilter;
    private List<String> folderLi=new ArrayList<>();
    private List<String> fileLi=Collections.synchronizedList(new ArrayList<>());
    public Collect(SSHExec ssh,String fileFilter,String folderFilter){
        this.ssh=ssh;
        this.fileFilter=fileFilter;
        this.folderFilter=folderFilter;
    }
    public void visit(String path) throws TaskExecFailException, InterruptedException {
        Stack<String> stack=new Stack<>();
        stack.push(path);
        List<String> files=Collections.synchronizedList(new ArrayList<>());
        String f="";
        while(!stack.isEmpty()){
            f=stack.pop();
            Set<String> ret=getLeaves(f);
            for(String s:ret){
                //文件夹
                if(s.charAt(s.length()-1)=='/'){
                    folderLi.add(s);
                    stack.push(s);
                }
                //文件
                else{
                    files.add(s);
                }
            }
        }
        //单线程
//        String command="";
//        Result rs=null;
//        for(String file:files){
//            command="md5sum "+file;
//            rs=ssh.exec(new ExecCommand(command));
//            if(rs.isSuccess){
//                fileLi.add(rs.sysout);
//            }else {
//                System.out.println("获取"+file+"md5值失败");
//                throw new TaskExecFailException("获取"+file+"md5值失败");
//            }
//        }
        //多线程
        ExecutorService pool=Executors.newFixedThreadPool(12);
        int n=100;
        int size=files.size();
        int length;
        for(int i=0;i<size/n+1;i++){
            length= Math.min((i + 1) * n, size);
            System.out.println("i:"+i);
            System.out.println("length:"+length);
            pool.execute(new Md5Thread(ssh,files,i*n,length));
        }
        pool.shutdown();
        while(!pool.isTerminated()){
        }
        folderLi.forEach(System.out::println);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        fileLi.forEach(System.out::println);
        System.out.println("文件夹个数:"+folderLi.size());
        System.out.println("文件个数:"+files.size());
        System.out.println("文件个数:"+fileLi.size());
    }
    private Set<String> getLeaves(String path) throws TaskExecFailException {
        Set<String> ret=new HashSet<>();
        Result rs=null;
        //获取文件
        String command="ls -F --file-type "+path+"|grep -v '/$'|grep -v '@$'"+fileFilter;
        rs=ssh.exec(new ExecCommand(command));
        if(rs.rc==0){
            for(String f:rs.sysout.split("\n")){
                ret.add(path+f);
            }
        }else if(rs.rc==1){
//            System.out.println(path+"下没有文件");
        }else{
            System.out.println(rs.error_msg);
            throw new TaskExecFailException("获取"+path+"下文件失败");
        }
        //获取文件夹
        if(path.charAt(path.length()-1)=='/'){
            command="ls -F --file-type "+path+"|grep '/$'"+folderFilter;
            rs=ssh.exec(new ExecCommand(command));
            if(rs.rc==0){
                for(String f:rs.sysout.split("\n")){
                    ret.add(path+f);
                }
            }else if(rs.rc==1){
//            System.out.println(path+"下没有文件夹");
            } else{
                System.out.println(rs.error_msg);
                throw new TaskExecFailException("获取"+path+"下文件夹失败");
            }
        }
        return ret;
    }
    class Md5Thread implements Runnable{
        private SSHExec ssh;
        private List<String> files;
        private int i;
        private int n;
        public Md5Thread(SSHExec ssh,List<String> files,int i,int n){
            this.ssh=ssh;
            this.files=files;
            this.i=i;
            this.n=n;
        }
        @Override
        public void run() {
            Result rs=null;
            String command="";
            for(;i<n;i++){
                command="md5sum "+files.get(i);
                try {
                    rs=ssh.exec(new ExecCommand(command));
                    if(rs.isSuccess){
                        fileLi.add(rs.sysout);
                    }else{
                        throw new RuntimeException("获取"+files.get(i)+"md5值失败");
                    }
                } catch (TaskExecFailException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
