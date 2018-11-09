package com.moon.star;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
* 传入oracle建表脚本，返回list<map> 一个map就是一张表的信息
*
* */
public class AnalysisOracleSqlScriptToCreateGPTable {
    public List<Map<String,Object>> getTableImfo(String filePath){
        List<Map<String,Object>> listMap=new ArrayList<>();
        try {
            File file = new File(filePath);
            Long fileLength = file.length();
            byte[] filecontent = new byte[fileLength.intValue()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(filecontent);
            fis.close();
            String fileDetail = new String(filecontent, "UTF-8");
            // String fileDetail="";
            //去除空格，制表符，回车等影响因素
            String fileClean = Pattern.compile("\\s").matcher(fileDetail).replaceAll("").toLowerCase();
            //匹配有用的建表语句
            Matcher m = Pattern.compile("createtable\"\\w+\".\"\\w+\"\\((\"\\w+\"[a-z0-9\\u2E80-\\u9FFF\\(\\),'\"_-]+)+\\)").matcher(fileClean);
            String creatTable = "";
            String tableName = "";
            while (m.find()) {
                //去除char|byte
                creatTable = Pattern.compile("(?<=\\d)(char|byte)(?=\\))").matcher(m.group()).replaceAll("");
                //匹配表名
                Matcher mat = Pattern.compile("\\.\"\\w+\"").matcher(creatTable);
                if (mat.find()) {
                    tableName = mat.group().substring(2, mat.group().length() - 1);
                }else continue;
//                System.out.println(tableName);
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put("tableName",tableName);
                //匹配创建列的语句
                mat = Pattern.compile("\"\\w+\"((n)?(var)?char(2)?|(long)?text|(tiny|small|medium|big)?int(eger)?|time(stamp)?|date(time)?|(tiny|long)?blob|" +
                        "decimal|(var)?binary|bit|float|real|double|num(eric|ber)|year|(n)?clob|raw)(\\(\\d+(,)?(\\d+)?\\))?(?=,)?").matcher(creatTable);
                while (mat.find()) {
                    String[] columns = mat.group().split("\"");
                    //判断列类型是否带有长度
                    Matcher matc=Pattern.compile("(\\()\\d+(,)?(\\d+)?\\)").matcher(columns[2]);
                    if(matc.find()){
                        String type=columns[2].split("\\(")[0];
                        String length=matc.group();
                        columnMap.put(columns[1],getGPType(type)+length);
//                        System.out.println("columnName:" + columns[1] + ",type:" + getGPType(type)+",length:"+length);
                    }else {
                        columnMap.put(columns[1],getGPType(columns[2]));
//                        System.out.println("columnName:" + columns[1] + ",type:" + getGPType(columns[2]));
                    }
                }
                listMap.add(columnMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMap;
    }
    private  String getGPType(String type) {
        type = type.toLowerCase();
        String retType = "";
        switch (type) {
            case "longtext":
                retType = "text";
                break;
            case "tinyint":
                retType = "int";
                break;
            case "timestamp":
                retType = "timestamp";
                break;
            case "datetime":
                retType = "timestamp";
                break;
            case "smallint":
                retType = "int";
                break;
            case "tinyblob":
                retType = "bytea";
                break;
            case "blob":
                retType = "bytea";
                break;
            case "mediumint":
                retType = "bytea";
                break;
            case "longblob":
                retType = "bytea";
                break;
            case "text":
                retType = "text";
                break;
            case "decimal":
                retType = "numeric";
                break;
            case "binary":
                retType = "bytea";
                break;
            case "varbinary":
                retType = "bytea";
                break;
            case "int":
                retType = "int";
                break;
            case "integer":
                retType = "integer";
                break;
            case "bigint":
                retType = "bigint";
                break;
            case "bit":
                retType = "bit";
                break;
            case "float":
                retType = "float";
                break;
            case "real":
                retType = "float";
                break;
            case "double":
                retType = "double precision";
                break;
            case "numeric":
                retType = "numeric";
                break;
            case "char":
                retType = "char";
                break;
            case "varchar":
                retType = "varchar";
                break;
            case "date":
                retType = "timestamp";
                break;
            case "time":
                retType = "time";
                break;
            case "year":
                retType = "datetime";
                break;
            case "number":
                retType = "numeric";
                break;
            case "varchar2":
                retType = "varchar";
                break;
            case "clob":
                retType = "text";
                break;
            case "nvarchar2":
                retType = "varchar";
                break;
            case "nclob":
                retType = "text";
                break;
            case "raw":
                retType = "bytea";
                break;
            default:
                retType = "varchar";
        }
        return retType;
    }
}
