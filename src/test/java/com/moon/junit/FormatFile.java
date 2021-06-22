package com.moon.junit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FormatFile {
    public static void main(String[] args) throws IOException {
        List<String> lines=Files.readAllLines(Paths.get("C:\\Users\\liupei\\Desktop\\新建文本文档 (2).txt"));
        String line=lines.get(0);
        String[] fileds=line.split(";");
        List<String> newFile=new ArrayList<>();
        for(int i=0;i<fileds.length;i++){
            newFile.add(fileds[i]);
        }
        Files.write(Paths.get("C:\\Users\\liupei\\Desktop\\新建文本文档 (3).txt"),newFile);
    }
}
