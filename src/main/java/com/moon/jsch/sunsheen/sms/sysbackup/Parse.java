package com.moon.jsch.sunsheen.sms.sysbackup;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Objects;
/*
* 解析diff结果并生成shell脚本
* */
public class Parse {
    private final Logger log=Logger.getLogger(Parse.class);
    private final StringBuffer script=new StringBuffer("#!/bin/bash\n");
    //解析/etc/passwd文件
    public void parsePasswdDiffResults(List<String> results,String path){
        int exitStatus=Integer.parseInt(results.get(0));
        if(exitStatus==0){
            log.info("No difference was found in the comparison results.");
        }else if(exitStatus==1){
            results.remove(0);
            int i=0;
            while(i<results.size()){
                String line_0=results.get(i);
                switch(Objects.requireNonNull(which_acd(line_0))){
                    case "add":
                        String[] arr_0=line_0.split("a");
                        String rear=arr_0[1];
                        if(rear.contains(",")){
                            String[] arr_1=rear.split(",");
                            int length=Integer.parseInt(arr_1[1])-Integer.parseInt(arr_1[0])+1;
                            int ii=i+1;
                            for(;ii<=length;ii++){
                                parsePasswdLineAdd(results.get(ii));
                            }
                            i=ii;
                        }else {
                            parsePasswdLineAdd(results.get(i+1));
                            i+=1+1;
                        }
                        break;
                    case "delete":
                        String[] arr_2=line_0.split("d");
                        String front=arr_2[0];
                        if(front.contains(",")){
                            String[] arr_3=front.split(",");
                            int length=Integer.parseInt(arr_3[1])-Integer.parseInt(arr_3[0])+1;
                            int ii=i+1;
                            for(;ii<=length;ii++){
                                parsePasswdLineDelete(results.get(ii));
                            }
                            i=ii;
                        }else {
                            parsePasswdLineDelete(results.get(i+1));
                            i+=1+1;
                        }
                        break;
                    case "change":
                        break;
                }
            }
            script.append("exit 0");
            System.out.println(script);
        }else {
            log.error("This comparison is wrong!");
        }
    }
    //常规文件对比结果处理
    public void parseDiffResults(List<String> results,String path){
        int exitStatus=Integer.parseInt(results.get(0));
        if(exitStatus==0){
            log.info("No difference was found in the comparison results.");
        }else if(exitStatus==1){
        //生成同步脚本

        }else {
            log.error("This comparison is wrong!");
        }
    }
    //文件夹对比结果处理
    public void parseFolderResults(){


    }
    private String which_acd(String str){
        if(str.contains("a")){
            return "add";
        }else if(str.contains("d")){
            return "delete";
        }else if(str.contains("c")){
            return "change";
        }else return null;
    }
    private void parsePasswdLineAdd(String line){
        String[] fields=line.substring(2).split(":");
        script.append("useradd -u "+fields[2]+" -d "+fields[5]+" -s "+fields[6]+" "+fields[0]+"\n");
    }
    private void parsePasswdLineDelete(String line){
        String[] fields=line.substring(2).split(":");
        script.append("userdel -r "+fields[0]+"\n");
    }
}
