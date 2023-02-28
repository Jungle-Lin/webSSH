package com.jy.webssh.entity;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Data;
import lombok.Setter;

import java.io.*;

/**
 * @author JungleLin
 * @date 2023/2/2610:38
 */
@Setter
public class ShellEntity {
    Session session;
    ChannelShell channelShell;
    InputStream resultInputStream;
    OutputStream commandOutputStream;
    PrintWriter commandWriter;

    public ShellEntity(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public ChannelShell getChannelShell() throws JSchException {
        if (channelShell == null || !channelShell.isConnected()) {
            this.channelShell = (ChannelShell) session.openChannel("shell");
            channelShell.connect();
        }
        return channelShell;
    }

    public OutputStream getCommandOutputStream() throws IOException, JSchException {
        if (commandOutputStream == null)
            this.commandOutputStream = getChannelShell().getOutputStream();

        return commandOutputStream;
    }

    public PrintWriter getCommandWriter() throws IOException, JSchException {
        if (commandWriter == null)
            this.commandWriter = new PrintWriter(getCommandOutputStream());
        return commandWriter;
    }

    public InputStream getResultInputStream() throws JSchException, IOException {
        if (resultInputStream == null)
            this.resultInputStream = getChannelShell().getInputStream();
        return resultInputStream;
    }

    public void close() throws IOException {
        if (resultInputStream != null)
            resultInputStream.close();
        if (commandWriter != null)
            commandWriter.close();
        if (commandOutputStream != null)
            commandOutputStream.close();
        if (channelShell != null && channelShell.isConnected())
            channelShell.disconnect();
    }
}
