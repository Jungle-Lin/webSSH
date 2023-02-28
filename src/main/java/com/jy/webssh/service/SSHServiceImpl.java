package com.jy.webssh.service;

import com.jcraft.jsch.*;
import com.jy.webssh.exception.FileSizeException;
import com.jy.webssh.entity.User;
import com.jy.webssh.exception.UserNullException;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author JungleLin
 * @date 2023/2/261:24
 */

@Service
public class SSHServiceImpl implements SSHService {

    //定时的Map，到期就释放
    private static final ExpiringMap<String, Session> shellSessionMap = ExpiringMap.builder().variableExpiration().expirationListener((ip, sessionObj)->{
        Session session = (Session)sessionObj;
        if(session.isConnected())
            session.disconnect();
    }).build();
    private static final int MAX_FILE_SIZE = 1024 * 1024;

    @Override
    public void putSessionToMap(String ip, Session session, int keepAlive) {
        shellSessionMap.put(ip, session, ExpirationPolicy.CREATED, keepAlive, TimeUnit.SECONDS);
    }

    @Override
    public boolean connect(User user) throws JSchException, UserNullException {
        Session session = getSession(user);
        if (session != null && session.isConnected()) {
            shellSessionMap.put(user.getIp(), session, ExpirationPolicy.CREATED, user.getKeepAlive(), TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    @Override
    public Session getSession(User user) throws JSchException, UserNullException {
        Session shellSession;
        if (user == null)
            throw new UserNullException("user is null, please connect or request with parameter");
        if (shellSessionMap.containsKey(user.getIp())) {//session已经存在
            shellSession = shellSessionMap.get(user.getIp());
            if (shellSession.isConnected()) {//已经正常连接上
                return shellSession;
            }
        }
        //Session不存在，或者Session已经断开了,需要重新连接上
        JSch jSch = new JSch();

        shellSession = jSch.getSession(user.getName(), user.getIp(), user.getPort());
        shellSession.setPassword(user.getPassword());

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        shellSession.setConfig(config);
        shellSession.connect(3000);

        return shellSession;
    }

    @Override
    public String execCommand(Session session, String command) throws JSchException, IOException {
        StringBuffer result = new StringBuffer();
        Channel jschChannel = null;
        InputStream in = null;
        try {
            jschChannel = session.openChannel("exec");
            ((ChannelExec) jschChannel).setCommand(command);
            // 执行命令，等待执行结果
            jschChannel.connect();
            // 获取命令执行结果
            in = jschChannel.getInputStream();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.append(new String(tmp, 0, i));
                }
                // 从channel获取全部信息之后，channel会自动关闭
                if (jschChannel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    break;
                }
            }
        } finally {
            if (in != null)
                in.close();
            if (jschChannel != null && jschChannel.isConnected()) {
                jschChannel.disconnect();
            }
        }
        return result.toString();
    }

    @Override
    public void downloadFile(Session session, String path, HttpServletResponse response) throws JSchException, SftpException, IOException, FileSizeException {

        long serverFileSize = getServerFileSize(session, path);
        if (serverFileSize > MAX_FILE_SIZE) {
            throw new FileSizeException("filepath: " + path + " size: " + serverFileSize);
        } else {
            response.reset();
            String[] dirs = path.split("/");
            String filename = dirs[dirs.length - 1];
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.setContentType("application/octet-stream");
            ServletOutputStream outputStream = response.getOutputStream();

            ChannelSftp sftp = null;
            InputStream downloadFileInputStream = null;
            try {
                sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();

                downloadFileInputStream = sftp.get(path);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = downloadFileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            } finally {
                if (downloadFileInputStream != null)
                    downloadFileInputStream.close();
                if (sftp != null && !sftp.isClosed())
                    sftp.disconnect();
            }
        }
    }

    public long getServerFileSize(Session session, String path) throws IOException, JSchException {
        String[] result = execCommand(session, "wc -c " + path).split(" ");
        return Integer.parseInt(result[0]);
    }

    @Override
    public void uploadFile(Session session, String path, MultipartFile file) throws FileSizeException, IOException, JSchException, SftpException {
        if (file.getSize() > MAX_FILE_SIZE)
            throw new FileSizeException("filename: " + file.getName() + " size: " + file.getSize());
        else {
            InputStream in = null;
            ChannelSftp sftp = null;
            try {
                in = file.getInputStream();
                sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();
                sftp.put(in, path);
            } finally {
                if (sftp != null && !sftp.isClosed())
                    sftp.disconnect();
            }
        }
    }
}
