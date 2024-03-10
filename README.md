<!-- common contents -->

<div style="text-align: center">
    <img width="160" src="logo.svg" alt="logo"><br/>
    projectGDT - for a more connected Minecraft world!<br/>
    QQ Group:
    <a href="https://qm.qq.com/cgi-bin/qm/qr?k=jNFTovEpc0WDFtbSbUMrbQ0NyUgDpnCu&jump_from=webapi&authKey=6oBQQeoeB6gA7+AljJK7AV1IUEjkk/HpkvxrBNgAQtpxPtw230h4GQrp56nTw81I">
        162779544
    </a>
</div>


---

# gdt-connector

projectGDT 的子项目之一，作为服务器 (Spigot) 插件，承担与后端通讯的任务。

## 设计说明

### 需要实现的目标 & 完成情况

- [x] **连接**：开启服务器时，自动根据插件配置文件的内容与网站后端建立 socket 连接；提供服务端指令 `/setconfig` 用于设置插件配置文件并尝试连接到网站后端。

- [x] **报告玩家在线状态**：在服务器有玩家登入/登出时，向网站后端报告玩家的基本信息（profile 信息、事件时间戳）。

- [x] **踢出玩家**：当网站后端要求踢出某玩家时，踢出该玩家并向网站后端发送响应。

## 使用说明

### 开启 Minecraft 服务器并安装插件

该插件用于 Minecraft 服务器，需要安装在 Minecraft 服务器上使用。如果您已经会开服和安装插件，请跳过本节内容。

要开启用于测试本插件的 Minecraft 服务器，可以参考视频 https://www.bilibili.com/video/BV1rq4y117uA/ 的 01:00 到 03:40 之间的内容。开服成功后，可以将 `server.properties` 文件中的 `server-ip` 改为 `127.0.0.1` 或其他你能访问到的 IP 地址，便于之后使用 Minecraft 加入该服务器。

要安装插件，只需要将插件文件 `gdt-connector-0.1.0-all.jar` 放在服务端的 `\plugins` 目录下即可。

### 连接网站后端

安装好插件后，再次启动 Minecraft 服务器。服务器安装插件后首次启动时，插件会在 `\plugins\gdt-connector` 下创建配置文件 `config.yml` 并提示用法，如图：

![](https://pic.imgdb.cn/item/65edb1289f345e8d03c781cf.png)

在服务端内输入 `setconfig [serverId] [token]` 以设置配置文件内容，并自动连接到网站后端。一旦配置文件设置成功，每次启动服务器时就都会自动连接到网站后端，不需要再重复设置该文件。

### 配置文件说明

插件的配置文件位于 `\plugins\gdt-connector` 下。一个典型的配置文件内容如下：

```
backendAddress: localhost:14590
serverId: 1
token: token
```

`backendAddress` 为网站后端的地址，如果您的网站后端不是运行在 `localhost:14590` ，请将其手动修改成您的网站后端地址。

`serverId` 为您的服务器在网站后端中的 ID 。

`token` 为您的服务器的 token。

### 功能测试

注：由于网站前端完成度问题，目前只能测试插件报告玩家登入登出的功能。但踢出玩家的功能已经实现。

在开启 Minecraft 服务器并且插件成功连接到网站后端后，使用 Minecraft 登入或登出该服务器时，服务器后端就会以如下格式收到插件报告的信息：

```json
{
  "profile": {
    "uniqueIdProvider": 0,
    "uniqueId": "string",
    "cachedPlayerName": "string"
  },
  "timestamp": 0
}
```

由于提交的网站后端不会将信息 log 出来，所以您使用提交的网站后端可能无法观测到这条信息。建议您观看演示视频以了解该功能。

## 构建说明

开发者使用 Windows 11 系统，其他系统下能否使用同样的方式构建本插件是未经测试的。

使用终端在项目根目录下运行如下命令：

```
./gradlew shadowJar
```

执行完成后，可以在 `\build\libs` 目录下找到构建完成的 `.jar` 文件，文件的默认名称为 `gdt-connector-0.1.0-all.jar` 。