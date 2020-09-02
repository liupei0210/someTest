package com.moon.xxl;

import java.sql.*;

public class DBUtil {
    private String url="jdbc:postgresql://127.0.0.1:5432/public";
    private String user="postgres";
    private String passwd="123456";
    private Connection conn;
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public Connection getConn(){
        try {
            conn= DriverManager.getConnection(url,user,passwd);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return conn;
    }
    public ResultSet query(Connection conn,String sql){
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            ps=conn.prepareStatement(sql);
            rs=ps.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rs;
    }
    public boolean update(Connection conn,String sql){
        PreparedStatement ps=null;
        boolean rs=false;
        try {
            ps=conn.prepareStatement(sql);
            rs=ps.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rs;
    }
}
