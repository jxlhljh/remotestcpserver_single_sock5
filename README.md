1. ##拉取代码
```c
git clone https://github.com/jxlhljh/remotestcpserver_single_sock5.git

git clone https://gitee.com/jxlhljh/remotestcpserver_single_sock5.git

```
2. ##编绎
```c
maven clean package
```
`编绎成功后在maven项目的target下面会生成lib目录和remotestcpserver_single_sock5.jar，如图`
![在这里插入图片描述](https://img-blog.csdnimg.cn/de0f4956c0f242cfae9fb14bcf8d1492.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2p4bGhsamg=,size_16,color_FFFFFF,t_70)
3. ##启动示例
将lib文件夹和remotestcpserver_single_sock5.jar放在remotestcpserver_single_sock5目录下


3.1 ####场景1：如家里电脑（192.168.0.101）需要访问公司的Git代码服务器（172.168.201.88的8899端口）
`注意：以下的均为linux下的部署，命令中采用的是冒号，如果是windows下，改成分号即可`
![在这里插入图片描述](https://img-blog.csdnimg.cn/dfb7fe49453e4cdcbdebdf013ee8d29b.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2p4bGhsamg=,size_16,color_FFFFFF,t_70)
```c
##服务端，在阿里云服务器103.212.12.74上部署，开启端7000监听
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456

##控制端，在公司服务器（172.168.201.148上部署）
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token gzsendi -trunnelHost 103.212.12.74 -trunnelPort 7000 -groups stcp1 -types tcp -serverFrontPorts 18899 -remoteHosts 172.168.201.88 -remotePorts 8899
```
>##服务端
>ssl：是否使用ssl加密码，true是，false否
>serverPort: 7000,阿里云服务器的服务监听端口
>token：简单的socket通信密码，socket连接上来需要发送这个密码，校验不通过将断开连接

>##控制端
>ssl：是否使用ssl加密码，true是，false否，需要与服务端的配置保持一致
>token：简单的socket通信密码，需要与server的配置一致
>trunnelHost：阿里云服务器的Ip，隧道Ip
>trunnelPort：阿里云服务器的开放的隧道端口
>types：穿透的类型，目前支持取持tcp，sock5两种
>serverFrontPorts：这个是阿里云开放的端口，提供给外部网络连接进来使用的，比如18899，这样通过家里电话连接阿里云服务器的Ip和18899端口，就相当于访问内网的git服务器的ip和端口，达到穿透的效果
>remoteHosts：远程真实的IP，这里指的就是git服务器的ip
>remotePorts：远程真实的port，这里指的就是git代码服务器的端口
>groups: 群组名称，进行配置的区分，多个端口转发时，设置多个，用逗号分隔

程序启动后，从家里的电脑通过103.212.12.74的18899端口进行访问，能内网穿透到公司Git代码服务器172.168.201.88的8899。

3.2 ####场景2：sock5透传代理。
```c
##服务端，在阿里云服务器103.212.12.74上部署，开启端7000监听
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456

##控制端，在公司服务器（172.168.201.148上部署）
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token gzsendi -trunnelHost 103.212.12.74 -trunnelPort 7000 -groups stcp1 -types sock5 -serverFrontPorts 1080
```
>控制端：
>types：sock5 ，表示使用sock5代理，此时不需要配置remoteHosts和remotePorts

`程序启动后，将在103.212.12.74的1080端口开启sock5代理，通过sock5客户端配置后，能访问公司内部的所有机器，相当于在172.168.201.148上访问一样。`

3.3 ####场景3：同时开放多端口映射透传
```c
##服务端，在阿里云服务器103.212.12.74上部署，开启端7000监听
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456

##控制端，在公司服务器（172.168.201.148上部署）
cd remotestcpserver_single_sock5
java -cp "./lib/*:./remotestcpserver_single.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token gzsendi -trunnelHost 103.212.12.74 -trunnelPort 7000 -groups stcp1,stcp2 -types tcp -serverFrontPorts 18899,18080 -remoteHosts 172.168.201.88,172.168.201.10 -remotePorts 8899,8080
```
<font color=#ff0000>groups:这里因为配了2个端口透传因此配置了stcp1,stcp2</font>

`程序启动后，将在103.212.12.74上开放2个端口透传`

```c
103.212.12.74:18899 ----> 172.168.201.88:8899
103.212.12.74:18080 ----> 172.168.201.88:8080
```