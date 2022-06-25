package cn.gzsendi.stcp.config;

import cn.gzsendi.system.utils.YmlUtils;

public class ServerConfig {
	
	private static final String fileName = "server.yml";
	public static final int serverPort = (int)YmlUtils.getValue(fileName, "serverPort", 7000);
	public static final String token = YmlUtils.getValue(fileName,"token", "123456").toString();
	public static final boolean ssl = (boolean)YmlUtils.getValue(fileName,"ssl", false);
	
}
