@echo off

title _control.bat

java -cp "./lib/*;./remotestcpserver_single.jar" cn.gzsendi.stcp.control.ControlClientStart -ssl false -token 123456 -trunnelHost 127.0.0.1 -trunnelPort 7000 -groups stcp1 -types sock5 -serverFrontPorts 10808

