@echo off

title _stcpserver_single.bat

java -cp "./lib/*;./remotestcpserver_single_sock5.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456

