//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.neoremind.sshxcute.core;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.exception.UploadFileNotSuccessException;
import net.neoremind.sshxcute.task.CustomTask;

import org.apache.log4j.Logger;

public class SSHExec {
    static Logger logger = Logger.getLogger(SSHExec.class.getName());
    private Session session;
    private Channel channel;
    private ConnBean conn;
    private List<ConnBean> connArr = null;
    private static SSHExec ssh;
    private JSch jsch;
    private Session[] sessionArr;
    private static Object o = new Object();
    protected Map<String, String> dataList = null;
    protected static String PARENT_DIR = "";

    private SSHExec(ConnBean conn) {
        try {
            logger.info("SSHExec initializing ...");
            this.conn = conn;
            this.jsch = new JSch();
            this.sessionArr = new Session[1];
        } catch (Exception var3) {
            logger.error("Init SSHExec fails with the following exception: " + var3);
        }

    }

    private SSHExec(List<ConnBean> conn, int sessionLength) {
        try {
            logger.info("SSHExec initializing ...");
            this.connArr = conn;
            this.jsch = new JSch();
            this.sessionArr = new Session[sessionLength];
        } catch (Exception var4) {
            logger.error("Init SSHExec fails with the following exception: " + var4);
        }

    }

    public static SSHExec getInstance(ConnBean conn) {
        ssh = new SSHExec(conn);
        return ssh;
    }

    public static SSHExec getMultInstance(List<ConnBean> conn) {
        if (conn instanceof List) {
            ssh = new SSHExec(conn, conn.size());
        }

        return ssh;
    }

    public Boolean connect() {
        ConnCredential ui = null;

        try {
            if (this.conn.getPublicKey() != null && !"".equals(this.conn.getPublicKey())) {
                if (this.conn.getPassphrase() != null && "".equals(this.conn.getPassphrase())) {
                    this.jsch.addIdentity(this.conn.getPublicKey(), this.conn.getPassphrase());
                } else {
                    this.jsch.addIdentity(this.conn.getPublicKey());
                }
            }

            this.session = this.jsch.getSession(this.conn.getUser(), this.conn.getHost(), SysConfigOption.SSH_PORT_NUMBER);
            this.sessionArr[0] = this.session;
            if (this.conn.getPassword() != null && !"".equals(this.conn.getPassword())) {
                this.session.setPassword(this.conn.getPassword());
                logger.info("Session initialized and associated with user credential " + this.conn.getPassword());
            } else {
                ui = new ConnCredential();
                this.session.setUserInfo(ui);
            }

            this.session.setConfig("StrictHostKeyChecking", "no");
            logger.info("SSHExec initialized successfully");
            logger.info("SSHExec trying to connect " + this.conn.getUser() + "@" + this.conn.getHost());
            synchronized(o) {
                this.session.connect(10000);
            }

            logger.info("SSH connection established");
        } catch (Exception var4) {
            logger.error("Connect fails with the following exception: " + var4);
            return false;
        }

        return true;
    }

    public Session getSession() {
        return this.session;
    }

    public Boolean multConnect() {
        UserInfo ui = null;
        if (this.connArr.size() > 0) {
            ConnBean conBean = (ConnBean)this.connArr.get(0);

            try {
                if (conBean.getPublicKey() != null && !"".equals(conBean.getPublicKey())) {
                    if (conBean.getPassphrase() != null && "".equals(conBean.getPassphrase())) {
                        this.jsch.addIdentity(conBean.getPublicKey(), conBean.getPassphrase());
                    } else {
                        this.jsch.addIdentity(conBean.getPublicKey());
                    }
                }

                this.sessionArr[0] = this.session = this.jsch.getSession(conBean.getUser(), conBean.getHost(), SysConfigOption.SSH_PORT_NUMBER);
                if (conBean.getPassword() != null && "".equals(conBean.getPassword())) {
                    this.session.setPassword(conBean.getPassword());
                    logger.info("Session initialized and associated with user credential " + conBean.getPassword());
                } else {
                    ui = new ConnCredential();
                    this.session.setUserInfo(ui);
                }

                this.session.setConfig("StrictHostKeyChecking", "no");
                logger.info("SSHExec initialized successfully");
                logger.info("SSHExec trying to connect " + conBean.getUser() + "@" + conBean.getHost());
                synchronized(o) {
                    this.session.connect(10000);
                }

                logger.info("SSH connection established");

                for(int i = 1; i < this.connArr.size(); ++i) {
                    UserInfo uich = null;
                    ConnBean connBean = (ConnBean)this.connArr.get(i);
                    if (connBean.getPublicKey() != null && !"".equals(connBean.getPublicKey())) {
                        if (connBean.getPassphrase() != null && "".equals(connBean.getPassphrase())) {
                            this.jsch.addIdentity(connBean.getPublicKey(), connBean.getPassphrase());
                        } else {
                            this.jsch.addIdentity(connBean.getPublicKey());
                        }
                    }

                    int assinged_port = this.session.setPortForwardingL(0, connBean.getHost(), SysConfigOption.SSH_PORT_NUMBER);
                    logger.info("portforwarding: localhost:" + assinged_port + " -> " + connBean.getHost() + ":" + SysConfigOption.SSH_PORT_NUMBER);
                    this.sessionArr[i] = this.session = this.jsch.getSession(connBean.getUser(), "127.0.0.1", assinged_port);
                    if (connBean.getPassword() != null && !"".equals(connBean.getPassword())) {
                        this.session.setPassword(connBean.getPassword());
                        logger.info("Session initialized and associated with user credential " + connBean.getPassword());
                    } else {
                        uich = new ConnCredential();
                        this.session.setUserInfo(uich);
                    }

                    this.session.setConfig("StrictHostKeyChecking", "no");
                    this.session.setHostKeyAlias(connBean.getHost());
                    synchronized(o) {
                        this.session.connect(10000);
                    }

                    logger.info("The session has been established to " + connBean.getUser() + "@" + connBean.getHost());
                }
            } catch (Exception var10) {
                logger.error("Connect fails with the following exception: " + var10);
                return false;
            }
        }

        return true;
    }

    public Boolean disconnect() {
        try {
            Session[] var4;
            int var3 = (var4 = this.sessionArr).length;

            for(int var2 = 0; var2 < var3; ++var2) {
                Session ss = var4[var2];
                ss.disconnect();
            }

            this.session = null;
            ssh = null;
            logger.info("SSH connection shutdown");
            return true;
        } catch (Exception var5) {
            logger.error("Disconnect fails with the following exception: " + var5);
            return false;
        }
    }

    public synchronized Result exec(CustomTask task) throws TaskExecFailException {
        Result r = new Result();

        try {
            this.channel = this.session.openChannel("exec");
            String command = task.getCommand();
            logger.info("Command is " + command);
            ((ChannelExec)this.channel).setCommand(command);
            this.channel.setInputStream((InputStream)null);
            this.channel.setOutputStream(System.out);
            FileOutputStream fos = new FileOutputStream(SysConfigOption.ERROR_MSG_BUFFER_TEMP_FILE_PATH);
            ((ChannelExec)this.channel).setErrStream(fos);
            InputStream in = this.channel.getInputStream();
            this.channel.connect(5000);
            logger.info("Connection channel established succesfully");
            logger.info("Start to run command");
            while(!this.channel.isClosed()){
                TimeUnit.MILLISECONDS.sleep(100);
                logger.info("Waiting for connection channel to close ... ");
            }
            StringBuilder sb = new StringBuilder();
//            byte[] tmp = new byte[1024];
//            do {
//                while(in.available() > 0) {
//                    int i = in.read(tmp, 0, 1024);
//                    if (i < 0) {
//                        break;
//                    }
//
//                    String str = new String(tmp, 0, i);
//                    sb.append(str);
//                    logger.info(str);
//                }
//            } while(!this.channel.isClosed());
            String line="";
            BufferedReader br=new BufferedReader(new InputStreamReader(in));
            while((line=br.readLine())!=null){
                sb.append(line).append("\n");
            }
            System.out.println(command);
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(sb.toString());
            logger.info("Connection channel closed");
            logger.info("Check if exec success or not ... ");
            r.rc = this.channel.getExitStatus();
            r.sysout = sb.toString();
            if (task.isSuccess(sb.toString(), this.channel.getExitStatus())) {
                logger.info("Execute successfully for command: " + task.getCommand());
                r.error_msg = "";
                r.isSuccess = true;
            } else {
                r.error_msg = SSHExecUtil.getErrorMsg(SysConfigOption.ERROR_MSG_BUFFER_TEMP_FILE_PATH);
                r.isSuccess = false;
                logger.info("Execution failed while executing command: " + task.getCommand());
                logger.info("Error message: " + r.error_msg);
                if (SysConfigOption.HALT_ON_FAILURE) {
                    logger.error("The task has failed to execute :" + task.getInfo() + ". So program exit.");
                    throw new TaskExecFailException(task.getInfo());
                }
            }

            this.channel.disconnect();
            logger.info("Connection channel disconnect");
        } catch (JSchException var10) {
            logger.error(var10.getMessage());
        } catch (IOException var11) {
            logger.error(var11.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return r;
    }

    public void uploadAllDataToServer(String fromLocalDir, String toServerDir) throws Exception {
        if (!(new File(fromLocalDir)).isDirectory()) {
            throw new UploadFileNotSuccessException(fromLocalDir);
        } else {
            this.dataList = new LinkedHashMap();
            String staticRootDir = "";
            if (fromLocalDir.lastIndexOf(47) > 0) {
                staticRootDir = fromLocalDir.substring(0, fromLocalDir.lastIndexOf(47));
            } else if (fromLocalDir.lastIndexOf(92) > 0) {
                staticRootDir = fromLocalDir.substring(0, fromLocalDir.lastIndexOf(92));
            } else {
                staticRootDir = fromLocalDir;
            }

            staticRootDir = staticRootDir.replace('\\', '/');
            this.traverseDataDir(new File(fromLocalDir), staticRootDir);
            Iterator it = this.dataList.entrySet().iterator();

            while(it.hasNext()) {
                Entry entry = (Entry)it.next();
                this.uploadSingleDataUnderDirToServer(entry.getKey().toString(), toServerDir + entry.getValue().toString());
            }

        }
    }

    private void uploadSingleDataUnderDirToServer(String fromLocalFile, String toServerFile) throws Exception {
        FileInputStream fis = null;
        logger.info("Ready to transfer local file '" + fromLocalFile + "' to server directory '" + toServerFile + "'");
        String command = "mkdir -p " + toServerFile + "; scp -p -t " + toServerFile;
        Channel channel = this.session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();
        logger.info("Connection channel established succesfully");
        logger.info("Start to upload");
        if (SSHExecUtil.checkAck(in) != 0) {
            System.exit(0);
        }

        long filesize = (new File(fromLocalFile)).length();
        command = "C0644 " + filesize + " ";
        if (fromLocalFile.lastIndexOf(47) > 0) {
            command = command + fromLocalFile.substring(fromLocalFile.lastIndexOf(47) + 1);
        } else {
            command = command + fromLocalFile;
        }

        command = command + "\n";
        out.write(command.getBytes());
        out.flush();
        if (SSHExecUtil.checkAck(in) != 0) {
            logger.error(fromLocalFile + "check fails");
        } else {
            fis = new FileInputStream(fromLocalFile);
            byte[] buf = new byte[1024];

            while(true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    fis.close();
                    fis = null;
                    buf[0] = 0;
                    out.write(buf, 0, 1);
                    out.flush();
                    if (SSHExecUtil.checkAck(in) != 0) {
                        logger.error(toServerFile + "check fails");
                        return;
                    } else {
                        out.close();
                        logger.info("Upload success");
                        channel.disconnect();
                        logger.info("channel disconnect");
                        return;
                    }
                }

                out.write(buf, 0, len);
            }
        }
    }

    public Result execAsync(CustomTask task) throws TaskExecFailException {
        Result r = new Result();

        try {
            Channel channel = this.session.openChannel("exec");
            String command = task.getCommand();
            logger.info("Command is " + command);
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream((InputStream)null);
            channel.setOutputStream(System.out);
            FileOutputStream fos = new FileOutputStream(SysConfigOption.ERROR_MSG_BUFFER_TEMP_FILE_PATH);
            ((ChannelExec)channel).setErrStream(fos);
            InputStream in = channel.getInputStream();
            channel.connect();
            logger.info("Connection channel established succesfully");
            logger.info("Start to run command");
            StringBuilder sb = new StringBuilder();
            byte[] tmp = new byte[1024];

            do {
                while(in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }

                    String str = new String(tmp, 0, i);
                    sb.append(str);
                    logger.info(str);
                }
            } while(!channel.isClosed());

            logger.info("Connection channel closed");
            logger.info("Check if exec success or not ... ");
            r.rc = channel.getExitStatus();
            r.sysout = sb.toString();
            if (task.isSuccess(sb.toString(), channel.getExitStatus())) {
                logger.info("Execute successfully for command: " + task.getCommand());
                r.error_msg = "";
                r.isSuccess = true;
            } else {
                r.error_msg = SSHExecUtil.getErrorMsg(SysConfigOption.ERROR_MSG_BUFFER_TEMP_FILE_PATH);
                r.isSuccess = false;
                logger.info("Execution failed while executing command: " + task.getCommand());
                logger.info("Error message: " + r.error_msg);
                if (SysConfigOption.HALT_ON_FAILURE) {
                    logger.error("The task has failed to execute :" + task.getInfo() + ". So program exit.");
                    throw new TaskExecFailException(task.getInfo());
                }
            }

            channel.disconnect();
            logger.info("Connection channel disconnect");
        } catch (JSchException var11) {
            logger.error(var11.getMessage());
        } catch (IOException var12) {
            logger.error(var12.getMessage());
        }

        return r;
    }

    public void uploadSingleDataToServer(String fromLocalFile, String toServerFile) throws Exception {
        if ((new File(fromLocalFile)).isDirectory()) {
            throw new UploadFileNotSuccessException(fromLocalFile);
        } else {
            FileInputStream fis = null;
            logger.info("Ready to transfer local file '" + fromLocalFile + "' to server directory '" + toServerFile + "'");
            String command = "scp -p -t " + toServerFile;
            Channel channel = this.session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();
            logger.info("Connection channel established succesfully");
            logger.info("Start to upload");
            if (SSHExecUtil.checkAck(in) != 0) {
                System.exit(0);
            }

            long filesize = (new File(fromLocalFile)).length();
            command = "C0644 " + filesize + " ";
            if (fromLocalFile.lastIndexOf(47) > 0) {
                command = command + fromLocalFile.substring(fromLocalFile.lastIndexOf(47) + 1);
            } else if (fromLocalFile.lastIndexOf(92) > 0) {
                command = command + fromLocalFile.substring(fromLocalFile.lastIndexOf(92) + 1);
            } else {
                command = command + fromLocalFile;
            }

            command = command + "\n";
            out.write(command.getBytes());
            out.flush();
            if (SSHExecUtil.checkAck(in) != 0) {
                logger.info(fromLocalFile + "check fails");
            } else {
                fis = new FileInputStream(fromLocalFile);
                byte[] buf = new byte[1024];

                while(true) {
                    int len = fis.read(buf, 0, buf.length);
                    if (len <= 0) {
                        fis.close();
                        fis = null;
                        buf[0] = 0;
                        out.write(buf, 0, 1);
                        out.flush();
                        if (SSHExecUtil.checkAck(in) != 0) {
                            logger.error(toServerFile + "check fails");
                            return;
                        } else {
                            out.close();
                            logger.info("Upload success");
                            channel.disconnect();
                            logger.info("channel disconnect");
                            return;
                        }
                    }

                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void setOption(String option, String value) {
        Class optionClass = SysConfigOption.class;
        Field[] field = optionClass.getDeclaredFields();

        for(int i = 0; i < field.length; ++i) {
            if (field[i].getName().equals(option) && field[i].getType().getName().equals("java.lang.String")) {
                try {
                    logger.info("Set system configuration parameter '" + option + "' to new value '" + value + "'");
                    field[i].set(option, value);
                    break;
                } catch (IllegalAccessException var6) {
                    logger.error("Unable to set global configuration param " + option + " to value " + value);
                }
            }
        }

    }

    public static void setOption(String option, int value) {
        Class optionClass = SysConfigOption.class;
        Field[] field = optionClass.getDeclaredFields();

        for(int i = 0; i < field.length; ++i) {
            if (field[i].getName().equals(option) && field[i].getType().getName().equals("int")) {
                try {
                    logger.info("Set system configuration parameter '" + option + "' to new value '" + value + "'");
                    field[i].set(option, value);
                    break;
                } catch (IllegalAccessException var6) {
                    logger.error("Unable to set global configuration param " + option + " to value " + value);
                }
            }
        }

    }

    public static void setOption(String option, long value) {
        Class optionClass = SysConfigOption.class;
        Field[] field = optionClass.getDeclaredFields();

        for(int i = 0; i < field.length; ++i) {
            if (field[i].getName().equals(option) && field[i].getType().getName().equals("long")) {
                try {
                    logger.info("Set system configuration parameter '" + option + "' to new value '" + value + "'");
                    field[i].set(option, value);
                    break;
                } catch (IllegalAccessException var7) {
                    logger.error("Unable to set global configuration param " + option + " to value " + value);
                }
            }
        }

    }

    public static void setOption(String option, boolean value) {
        Class optionClass = SysConfigOption.class;
        Field[] field = optionClass.getDeclaredFields();

        for(int i = 0; i < field.length; ++i) {
            if (field[i].getName().equals(option) && field[i].getType().getName().equals("boolean")) {
                try {
                    logger.info("Set system configuration parameter '" + option + "' to new value '" + value + "'");
                    field[i].set(option, value);
                    break;
                } catch (IllegalAccessException var6) {
                    logger.error("Unable to set global configuration param " + option + " to value " + value);
                }
            }
        }

    }

    public static void showEnvConfig() throws Exception {
        Class optionClass = SysConfigOption.class;
        Field[] field = optionClass.getDeclaredFields();
        logger.info("******************************************************");
        logger.info("The list below shows sshxcute configuration parameter");
        logger.info("******************************************************");

        for(int i = 0; i < field.length; ++i) {
            logger.info(field[i].getName() + " => " + field[i].get(optionClass));
        }

    }

    protected Map<String, String> traverseDataDir(File parentDir, String parentRootPath) throws Exception {
        if (parentDir.isDirectory()) {
            String[] subComponents = SSHExecUtil.getFiles(parentDir);

            for(int j = 0; j < subComponents.length; ++j) {
                PARENT_DIR = PARENT_DIR + File.separator + parentDir.getName();
                this.traverseDataDir(new File(parentDir + "/" + subComponents[j]), parentRootPath);
            }
        } else if (parentDir.isFile()) {
            logger.info("Find " + parentDir.getPath());
            this.dataList.put(parentDir.getPath().toString().replace('\\', '/'), parentDir.getParent().toString().replace('\\', '/').split(parentRootPath)[1]);
        }

        return this.dataList;
    }

    public static void setSsh(SSHExec ssh) {
        SSHExec.ssh = ssh;
    }
}
