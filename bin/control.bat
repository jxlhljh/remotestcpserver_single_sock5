@echo off

title _control.bat

java -cp "./lib/*;./remotestcpserver_single_sock5.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token gzsendi -trunnelHost 127.0.0.1 -trunnelPort 7000 -groups stcp1 -types tcp -serverFrontPorts 13306 -remoteHosts 127.0.0.1 -remotePorts 3306