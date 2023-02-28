package com.jy.webssh.entity;

import com.jy.webssh.validator.IpPatten;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author JungleLin
 * @date 2023/2/261:40
 */
@Data
@AllArgsConstructor
public class User {
    @IpPatten
    String ip;
    @NotNull
    String name;
    @NotNull
    String password;
    int port;
    //保活时间，单位s
    int keepAlive;

    public User(String ip, String name, String password) {
        this(ip, name, password, 22, 30 * 60);
    }

    public User(int port, String ip, String name, String password) {
        this(ip, name, password, port, 30 * 60);
    }

    public User(String ip, String name, String password, int keepAlive) {
        this(ip, name, password, 22, keepAlive);
    }
}
