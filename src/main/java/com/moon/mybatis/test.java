package com.moon.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class test {
    public static void main(String[] args){
        String resource="mybatis-config.xml";
        InputStream is=null;
        SqlSessionFactory sqlSessionFactory=null;
        try {
            is= Resources.getResourceAsStream(resource);
            sqlSessionFactory=new SqlSessionFactoryBuilder().build(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSession session=sqlSessionFactory.openSession();
        Map<String,Object> map=new HashMap<>();
//        map.put("id",0);
//        map.put("name","目录配置");
        List<HashMap> listm=session.selectList("com.moon.mybatis.sms.ipMapping",1);
        listm.forEach(x->x.forEach((k, v) -> System.out.println("key:value = " + k + ":" + v)));
        session.close();
    }
}
