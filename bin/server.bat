@echo off

title _stcpserver_single_sock5.bat

java -server -Xmx128m -Xms128m -Xmn64m -cp "./lib/*;./remotestcpserver_single_sock5.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456
