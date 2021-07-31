package cn.gzsendi.stcp.control;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gzsendi.stcp.server.StcpServer;
import cn.gzsendi.stcp.utils.MessageUtils;
import cn.gzsendi.stcp.utils.SocketFactory;
import cn.gzsendi.system.exception.GzsendiException;
import cn.gzsendi.system.utils.JsonUtil;

import com.alibaba.fastjson.JSONObject;

public class ControlClient extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(ControlClient.class);
	private int soTimeOut = 120000;//2分钟超时
	private final static String characterSet = "UTF-8";

	String token = ControlClientStart.token;
	String role = "control";
	String groupName;
	String typeName;
	String trunnelHost = ControlClientStart.trunnelHost;
	int trunnelPort = ControlClientStart.trunnelPort;
	boolean loginFailExit = false;//失败了直接退出
	private String remoteHost;
	private int remotePort;
	private int serverFrontPort;

	private boolean needProxy = ControlClientStart.needProxy;
	private String proxyType = ControlClientStart.proxyType;
	private String proxyHost = ControlClientStart.proxyHost;
	private int proxyPort = ControlClientStart.proxyPort;
	private String proxyUsername = ControlClientStart.proxyUsername;
	private String proxyPassword = ControlClientStart.proxyPassword;
	
	DataOutputStream dout = null;
	
	public ControlClient(String groupName,String typeName,String remoteHost,int remotePort, int serverFrontPort){
		this.groupName = groupName;
		this.typeName = typeName;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.serverFrontPort = serverFrontPort;
	}

	public void run() {
		
		ControlClientCheck heartThread = new ControlClientCheck();
		heartThread.setDaemon(true);
		heartThread.start();

		while (true) {
			
			connectServer();//连上服务器并进行服务

			if (loginFailExit == true){
				break;
			}

			try {
				
				Thread.sleep(10000l);
				logger.info(groupName + " -> controlClient >>>> Unconnected, Try reconnect to "	+ trunnelHost + ":" + trunnelPort);

			} catch (Exception e) {
				logger.error(groupName + " -> ",e);
			}

		}

	}
	
    private Proxy createProxy(String proxyAddr, int proxyPort) {
    	
    	// 设置认证
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
            }
        });
        
        // 设置Socks代理
        Proxy proxy = new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(proxyAddr, proxyPort));
        return proxy;
        
    }
	
	public void connectServer() {
		
		Socket controlSocket = null;
		InputStream in = null;
		OutputStream out = null;
		DataInputStream din = null;
		
		try {
			
			//目前做不到ssl的同时使用proxy
			if(ControlClientStart.ssl == false) {
				
				if(needProxy) {
					
					if("socks".equals(proxyType)){
			    		
						Proxy proxy = createProxy(proxyHost, proxyPort);
						controlSocket = new Socket(proxy);
						controlSocket.setSoTimeout(soTimeOut);//5分钟未收到数据，自动关闭资源
						controlSocket.connect(new InetSocketAddress(trunnelHost, trunnelPort));
			    		
			    	}else {
			    		//"http".equals(proxyType))
			    		controlSocket = SocketFactory.createHttpProxySocket(trunnelHost, trunnelPort, proxyHost, proxyPort,proxyUsername,proxyPassword);
			    	}
					
				}else {
					
					//无代理
					controlSocket = new Socket();
					controlSocket.setSoTimeout(soTimeOut);//5分钟未收到数据，自动关闭资源
					controlSocket.connect(new InetSocketAddress(trunnelHost, trunnelPort));
					
				}
				
			}else {
				
				SSLContext ctx = SSLContext.getInstance("SSL");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		        KeyStore ks = KeyStore.getInstance("JKS");
		        KeyStore tks = KeyStore.getInstance("JKS");
		        
		        InputStream kclientIn = StcpServer.class.getClassLoader().getResourceAsStream("cert/kclient.ks");
		        InputStream tclientIn = StcpServer.class.getClassLoader().getResourceAsStream("cert/tclient.ks");
		        ks.load(kclientIn, "sendiclientpass".toCharArray());
		        tks.load(tclientIn, "sendiclientpublicpass".toCharArray());
		        kclientIn.close();
		        tclientIn.close();
		        
		        kmf.init(ks, "sendiclientpass".toCharArray());
		        tmf.init(tks);
		        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		        controlSocket = (SSLSocket) ctx.getSocketFactory().createSocket(trunnelHost, trunnelPort);
		        controlSocket.setSoTimeout(soTimeOut);//5分钟未收到数据，自动关闭资源
				
			}
			
			logger.info(groupName + " controlSocket>>>> " + controlSocket + "  connected" );
			
			in = controlSocket.getInputStream();
			out = controlSocket.getOutputStream();
			din = new DataInputStream(in);
			dout = new DataOutputStream(out);
			
			//进行第一个数据包下发，告诉服务器当前是什么角色
			dout.write(MessageUtils.bytessTart);
			String heartStr = "{\"msgType\":\"cotrolConnect\",\"token\":\""+token+"\",\"role\":\""+role+"\",\"groupName\":\""+groupName+"\",\"serverFrontPort\":\""+serverFrontPort+"\"} ";
			dout.writeInt(heartStr.getBytes(characterSet).length);
			dout.write(heartStr.getBytes(characterSet));
			
			logger.info(groupName + " -> controlSocket client connected to remote server success. ---->>>>" +controlSocket.getRemoteSocketAddress() );
			logger.info(groupName + " -> Client is proxyed to visit---->>>> "+trunnelHost+":"+serverFrontPort+"->" +remoteHost + ":" + remotePort );

			//其他包解析并处理
			while(true){
				
				JSONObject dataStr = readDataStr(din);
				String msgType = dataStr.getString("msgType");//消息类型，controlConnect,controlHeart,visitorConnect,visitorHeart,dataBindReq
				
				//心跳回应
				if("controlHeart".equals(msgType)) {
					
					logger.info(groupName + " -> controlClient>>>> " + controlSocket.getInetAddress().getHostAddress()+":"+ controlSocket.getPort() + "  receive heart pkg response");

					
				}else if("dataBindReq".equals(msgType)) {
					
					logger.info(groupName + " " + dataStr.getString("globalTraceId"));
					new ControlCli(typeName,remoteHost,remotePort,dataStr).start();

					
				}else {
					
					throw new GzsendiException("unknown msgType.");
					
				}
				
			}
			
			
		} catch (Exception e) {
			logger.error(groupName + " -> ",e);
		} finally {
			
			close(controlSocket);
			
		}
		
	}
	
	//发送心跳包
	public void sendHeartMessage() throws IOException{
		if(dout != null) {
			
			String heartStr = "{\"msgType\":\"controlHeart\"}";
			dout.write(MessageUtils.bytessTart);
			dout.writeInt(heartStr.getBytes(characterSet).length);
			dout.write(heartStr.getBytes(characterSet));
			
		}
	}
	
	//关闭资源
	public void close(Socket controlSocket){
		try {
			if(controlSocket !=null && !controlSocket.isClosed()) {
				controlSocket.close();
				logger.info(groupName + " -> controlSocket>>>> " + controlSocket.getRemoteSocketAddress() +" isClosed:"+ controlSocket.isClosed());
				controlSocket = null;
			}
			
		} catch (Exception e1) {
			logger.error(groupName + " -> ",e1);
		}
		
	}
	
	//读取socket消息头 , 0x070x07+字符串长度+Json字符串
	private JSONObject readDataStr(DataInputStream dis) throws IOException{
		
		byte firstByte = dis.readByte();
		byte secondByte = dis.readByte();
		if( firstByte != 0x07 && secondByte != 0x07){
			throw new GzsendiException("unkown clientSocket.");
		}
		
		int resultlength = dis.readInt();
		byte[] datas = new byte[resultlength];
		int totalReadedSize = 0;
		while(totalReadedSize < resultlength) {
			int readedSize = dis.read(datas,totalReadedSize,resultlength-totalReadedSize);
			totalReadedSize += readedSize;
		}
		String headStr = new String(datas,characterSet);
		return JsonUtil.fromJson(headStr);
	}

	//心跳包发送
	private class ControlClientCheck extends Thread {
		
		public void run() {
			
			while(true) {
				
				try {
					Thread.sleep(30000l);
				} catch (Exception e) {
					logger.error(groupName + " -> ",e);
				}
				
				try {
					sendHeartMessage();
				} catch (Exception e) {
					logger.error(groupName + " -> ",e);
				}
				
			}
			
		}
		
	}


}
