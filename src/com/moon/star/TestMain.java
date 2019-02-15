package com.moon.star;

import java.util.List;
import java.util.Map;

public class TestMain {
    public static void main(String[] args){
        AnalysisOracleSqlScriptToCreateGPTable a=new AnalysisOracleSqlScriptToCreateGPTable();
        List<Map<String,Object>> l= a.getTableImfo("D:\\test\\MDM.sql");
        for (Map<String,Object> map:l) {
            for (String key:map.keySet()) {
                System.out.println(key+":"+map.get(key));
            }
            System.out.println("\n");
        }
    }
}
