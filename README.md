# webssh

使用web接口进行ssh连接，并执行相关命令

以jcsh为基础，实现了上传文件、下载文件、执行单条命令、使用websocket连接持续执行命令，相关代码仅供参考，菜鸟一枚!

这个放在了我的服务器上，如果临时使用可以考虑用用，但是不建议用，首先在单位时间内限制接口的访问次数，其次并不是很安全（虽然我并不会保存你的ssh账号和密码到本地）。如果临时使用，建议使用之后更改ssh密码

以下是放在我服务器上，只能支持80端口，所以是http和ws协议，相关参数请求的说明

## 一般的HTTP请求

### 1、公共请求参数说明

这些参数每次请求都要携带，使用json格式

ip：服务器的ip

name：服务器的ssh账户名

password：服务器的ssh密码

以上是必须携带的，以下是可以自行携带，不携带就是默认值

port：ssh的端口，不携带默认是22

keepAlive：存活时间，不携带默认是1800s



### 2、返回结果JSON说明

```json
{
    "code":200,
    "data":"",
    "msg":"success"
}
```

code是200代表执行成功，data会返回数据，如果报错，msg会返回错误信息

因为统一输出没有太多处理，有些错误显示格式不太统一，请见谅，临时用用还是可以的

### 3、连接

这个不是必须的，直接上传、下载、执行命令都是可以的，这个可以用来验证是否可以正常连接上

url：8.218.160.194/ssh/connect

type：post

requestData：

```json
/*后续就只用“公共参数”代替*/
{
    "ip":"1.1.1.1",
    "name":"root",
    "password":"123456",
    "port":22,
    "keepAlive":1800
}
```

resultData：

```json
{
    "code": 200,
    "data": "true",
    "msg": "success"
}
```



### 4、上传文件

url：8.218.160.194/ssh/file/upload

type：post

requestData：

```json
{
    公共参数
    "path":"filepath",
    "file":file
}
```

resultData：略



### 5、下载文件

url：8.218.160.194/ssh/file/download

type：get

requestData：

```json
{
    公共参数
    "path":"filepath"
}
```

resultData：二进制文件

### 6、执行单条命令

url：8.218.160.194/ssh/exec

type：post

requestData：

```json
{
    公共参数
    "command":"ls"
}
```

resultData：略

多条命令可以用&、&& 、||这些连接，具体用法说明可以自行搜索了解



## Websocket使用

为了节省开销，复用对象，这个使用jsch中的channelShell实现这个功能

url：ws://8.218.160.194/ssh/websocket/1.1.1.1?name=root&password=123456&port=22&keepAlive=1800

返回“success”

相关参数自行替换

连接上之后直接发送相关命令就行了，后台会将执行结果以二进制的数据返回给你

需要注意格式，我上传了两次命令“pwd”和“ls ./webssh"，返回的结果如下所示，可以看到这个是像在一般ssh窗口中显示的那样，会将”[root@iZj6caera24wuzshs9bkx6Z ~]#“这种前缀和命令本身返回的，所以需要自行处理相关数据才行

![image-20230301082054603](https://user-images.githubusercontent.com/52553032/222013843-bef2208f-4093-4f9d-b74d-2d6b20bca408.png)


最后臭不要脸一下，如果想觉得有帮助，有余力的话，可以支持一下，谢谢
![1677626592069](https://user-images.githubusercontent.com/52553032/222013955-1701a7e6-76f4-433d-ac6e-d051f24be2c6.jpg)
