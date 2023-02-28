package com.jy.webssh.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jy.webssh.exception.FileSizeException;
import com.jy.webssh.entity.User;
import com.jy.webssh.exception.UserNullException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author JungleLin
 * @date 2023/2/261:24
 */
public interface SSHService {
    void putSessionToMap(String ip, Session session, int keepAlive);

    boolean connect(User user) throws JSchException, UserNullException;

    Session getSession(User user) throws JSchException, UserNullException;

    String execCommand(Session session, String command) throws JSchException, IOException;

    void downloadFile(Session session, String path, HttpServletResponse outputStream) throws JSchException, SftpException, IOException, FileSizeException;

    void uploadFile(Session session, String path, MultipartFile file) throws FileSizeException, IOException, JSchException, SftpException;
}
