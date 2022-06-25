@echo off

title _control.bat

java -server -Xmx128m -Xms128m -Xmn64m -cp "./lib/*;./remotestcpserver_single_sock5.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token 123456 -trunnelHost 127.0.0.1 -trunnelPort 7000 -groups stcp1 -types tcp -serverFrontPorts 13306 -remoteHosts 127.0.0.1 -remotePorts 3306

