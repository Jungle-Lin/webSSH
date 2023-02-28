package com.jy.webssh.controller;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jy.webssh.entity.User;
import com.jy.webssh.exception.FileSizeException;
import com.jy.webssh.service.SSHService;
import com.jy.webssh.exception.UserNullException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 不会将ip、name、password存在session中，需要每次都提交
 *
 * @author JungleLin
 * @date 2023/2/261:08
 */
@Controller
@RequestMapping("/ssh")
public class SSHController {

    @Autowired
    SSHService sshService;

    @PostMapping("/connect")
    @ResponseBody
    public String connect(@ModelAttribute("user") User user, HttpServletRequest request) throws JSchException, UserNullException, InterruptedException {
        boolean connect = sshService.connect(user);
        if (connect) {
            HttpSession session = request.getSession();
            session.setAttribute("sessionUser", user);
            session.setMaxInactiveInterval(user.getKeepAlive());
            System.out.println(session.getId());
        }
        Thread.sleep(1000);

        return connect + "";
    }

    /**
     * 执行单条命令
     *
     * @param user
     * @param command
     * @return
     * @throws JSchException
     * @throws IOException
     */
    @PostMapping("/exec")
    @ResponseBody
    public String execCommand(@ModelAttribute("user") User user, String command) throws JSchException, IOException, UserNullException {
        return sshService.execCommand(sshService.getSession(user), command);
    }

    /**
     * 前端做一定配合，将接收类型设定为Blob
     *
     * @param user
     * @param path
     * @throws JSchException
     * @throws IOException
     */
    @GetMapping("/file/download")
    public void downloadFile(@ModelAttribute("user") User user, String path, HttpServletResponse response) throws JSchException, IOException, SftpException, FileSizeException, UserNullException {
        sshService.downloadFile(sshService.getSession(user), path, response);
    }

    /**
     * 上传文件
     *
     * @param user
     * @param path
     * @throws JSchException
     * @throws IOException
     */
    @PostMapping("/file/upload")
    @ResponseBody
    public String uploadFile(@ModelAttribute("user") User user, String path, MultipartFile file) throws JSchException, IOException, SftpException, FileSizeException, UserNullException {
        sshService.uploadFile(sshService.getSession(user), path, file);
        return true + "";
    }


    @ModelAttribute("user")
    public User getUser(@RequestParam String ip, @RequestParam String name, @RequestParam String password, @RequestParam(required = false, defaultValue = "22") Integer port, @RequestParam(required = false, defaultValue = "1800") Integer keepAlive) {
        return new User(ip, name, password, port, keepAlive);
    }

}
