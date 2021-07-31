package cn.gzsendi.stcp.server;


public class StcpServerStart {
	
	public static int serverPort = 7000;
	public static String token = "123456";
	public static boolean ssl = true;
	
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
