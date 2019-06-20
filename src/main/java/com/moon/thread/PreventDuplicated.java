package com.moon.thread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PreventDuplicated {
    private final static String LOCK_PATH="/home/liupei/test/";
    private final static String LOCK_FILE="lock";
    private final static String PERMISSIONS="rw--------";
    public static void main(String[] args)throws IOException{
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                System.out.println(" The program received kill SIGNAL.");
                getLockFile().toFile().delete();
            }
        });
        chcekRunning();
        for(;;){
            try{
                TimeUnit.SECONDS.sleep(1);
                System.out.println("The program is running.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void chcekRunning() throws IOException{
        Path path=getLockFile();
        if(path.toFile().exists()){
            throw new RuntimeException("The program is running.");
        }
        Set<PosixFilePermission> perms= PosixFilePermissions.fromString(PERMISSIONS);
        Files.createFile(path,PosixFilePermissions.asFileAttribute(perms));
    }
    private static Path getLockFile(){
        return Paths.get(LOCK_PATH,LOCK_FILE);
    }
}
