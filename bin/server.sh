#!/bin/sh

pid=`ps -ef | grep StcpServerStart | grep java | awk '{print $2}'`
kill -9 $pid >/dev/null 2>&1

java -cp "./lib/*:./remotestcpserver_single_sock5.jar" cn.gzsendi.stcp.server.StcpServerStart -ssl false -serverPort 7000 -token 123456 &
