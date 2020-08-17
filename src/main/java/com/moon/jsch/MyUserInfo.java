package com.moon.jsch;

import com.jcraft.jsch.UserInfo;

public class MyUserInfo implements UserInfo {
    private String user="root";
    private String host="127.0.0.1";
    private int port=22;
    private String password="";
    private String identity="~/.ssh/id_rsa";
    private String passphrase="";
    @Override
    public String getPassphrase() {
        return passphrase;
    }

    public MyUserInfo(String user, String host, int port, String password, String identity, String passphrase) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.password = password;
        this.identity = identity;
        this.passphrase = passphrase;
    }

    public MyUserInfo(String user, String host, int port, String password, String identity) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.password = password;
        this.identity = identity;
    }
    public MyUserInfo(String user, String host, int port, String password) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public MyUserInfo(String user, String host, int port) {
        this.user = user;
        this.host = host;
        this.port = port;
    }

    public MyUserInfo(String user, String host) {
        this.user = user;
        this.host = host;
    }

    public MyUserInfo(String host) {
        this.host = host;
    }

    public MyUserInfo() {
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getIdentity() {
        return identity;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String s) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String s) {
        return true;
    }

    @Override
    public boolean promptYesNo(String s) {
        return true;
    }

    @Override
    public void showMessage(String s) {

    }
}
