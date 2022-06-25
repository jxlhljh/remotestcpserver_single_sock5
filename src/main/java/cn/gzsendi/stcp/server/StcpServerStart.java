package cn.gzsendi.stcp.server;

import cn.gzsendi.stcp.config.ServerConfig;

public class StcpServerStart {
	
	public static int serverPort = ServerConfig.serverPort;
	public static String token = ServerConfig.token;
	public static boolean ssl = ServerConfig.ssl;
	
	public static void main(String[] args) {
		
		for(int i=0;i<args.length;i++) {
	      if ("-serverPort".equals(args[i])) {
	    	  serverPort = Integer.parseInt(args[i+1]);
	    	  i++;
	      }else if ("-token".equals(args[i])) {
	    	  token = args[i+1];
	    	  i++;
	      }else if ("-ssl".equals(args[i])) {
	    	  ssl = Boolean.parseBoolean(args[i+1]);
	    	  i++;
	      }
	    }
		
		StcpServer stcpServer = new StcpServer(serverPort);
        stcpServer.startServer();
	}

}
