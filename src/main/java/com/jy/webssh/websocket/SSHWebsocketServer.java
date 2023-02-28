package com.jy.webssh.websocket;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jy.webssh.entity.User;
import com.jy.webssh.service.SSHService;
import com.jy.webssh.exception.UserNullException;
import lombok.SneakyThrows;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author JungleLin
 * @date 2023/2/261:55
 */
@Component
@ServerEndpoint("/ssh/websocket/{ip}")
public class SSHWebsocketServer {

    private PrintWriter commandPrintWriter;
    private ChannelShell shell;
    private User user;
    private static final long UNIT_TIME = 60;
    private static final int MAX_COUNT_UNIT_TIME = 20;
    private static final ExpiringMap<String, Integer> map = ExpiringMap.builder().variableExpiration().build();

    static SSHService sshService;

    @Autowired
    public void setSshService(SSHService sshService) {
        SSHWebsocketServer.sshService = sshService;
    }

    static class MyThread implements Runnable {

        InputStream inputStream;
        Session session;
        List<Byte> list = new LinkedList<>();

        public MyThread(InputStream inputStream, Session session) {
            this.inputStream = inputStream;
            this.session = session;
        }

        @SneakyThrows
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                int i = 0;
                while ((i = inputStream.read(buffer)) != -1) {
                    for (int j = 0; j < i; j++) {
                        list.add(buffer[j]);
                    }
                    if (session != null && session.isOpen())
                        session.getBasicRemote().sendBinary(ByteBuffer.wrap(buffer, 0, i));
                }
            } finally {
                if (inputStream != null)
                    inputStream.close();
                byte[] str = new byte[list.size()];
                int i = 0;
                for (Byte aByte : list) {
                    str[i++] = aByte;
                }
                System.out.println(new String(str));
            }
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("ip") String ip) throws UserNullException, JSchException, IOException {
        Map<String, List<String>> paramMap = session.getRequestParameterMap();
        int port = 22;
        if (paramMap.containsKey("port"))
            port = Integer.parseInt(paramMap.get("port").get(0));
        int keepAlive = 30 * 60;
        if (paramMap.containsKey("keepAlive"))
            keepAlive = Integer.parseInt(paramMap.get("keepAlive").get(0));
        User user = new User(ip, paramMap.get("name").get(0), paramMap.get("password").get(0), port, keepAlive);
        this.user = user;

        sshService.putSessionToMap(user.getIp(), sshService.getSession(user), user.getKeepAlive());
        session.getAsyncRemote().sendText("success");

        shell = (ChannelShell) sshService.getSession(user).openChannel("shell");
        shell.connect();
        commandPrintWriter = new PrintWriter(shell.getOutputStream());

        new Thread(new MyThread(shell.getInputStream(), session)).start();
    }


    @OnClose
    public void onClose() {
        try {
            this.commandPrintWriter.println("exit");
            this.commandPrintWriter.flush();
        } finally {
            if (commandPrintWriter != null)
                commandPrintWriter.close();
            if (shell != null && shell.isConnected())
                this.shell.disconnect();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String ip = this.user.getIp();
        Integer count = map.getOrDefault(ip, 0);
        if (count >= MAX_COUNT_UNIT_TIME) {
            session.getAsyncRemote().sendText("Requests are too frequent, please try again later");
            return;
        } else if (count == 0) {
            /**
             * map.put(
             * key, value , ExpirationPolicy(过期策略),duration(持续时间), TimeUnit(时间格式: 日、时、分、秒、毫秒)
             * )
             */
            map.put(ip, count + 1, ExpirationPolicy.CREATED, UNIT_TIME, TimeUnit.SECONDS);
        } else {
            map.put(ip, count + 1);
        }
        this.commandPrintWriter.print(message + "\n");
        this.commandPrintWriter.flush();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        if (session != null && session.isOpen())
            session.getAsyncRemote().sendText(error.getMessage());
    }
}
