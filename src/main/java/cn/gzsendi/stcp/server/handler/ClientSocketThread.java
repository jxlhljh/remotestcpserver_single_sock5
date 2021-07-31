package cn.gzsendi.stcp.server.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gzsendi.stcp.server.StcpServer;
import cn.gzsendi.stcp.server.StcpServerStart;
import cn.gzsendi.stcp.utils.FlowCounter;
import cn.gzsendi.system.exception.GzsendiException;
import cn.gzsendi.system.utils.JsonUtil;

import com.alibaba.fastjson.JSONObject;

public class ClientSocketThread extends Thread{
	
	private final static Logger logger = LoggerFactory.getLogger(ClientSocketThread.class);
	private final static String characterSet = "UTF-8";
	private static String token = StcpServerStart.token;
	private StcpServer stcpServer;
	private Socket clientSocket;
	private InputStream in;
	private OutputStream out;
	private DataInputStream din;
	private DataOutputStream dout;

	private String groupName;
	private String role;
	
	public ClientSocketThread(StcpServer stcpServer,Socket cliSocket){
		this.stcpServer = stcpServer;
		this.clientSocket = cliSocket;
	}
	
	public DataInputStream getDin() {
		return din;
	}
	
	public DataOutputStream getDout() {
		return dout;
	}
	
	public void run() {
		
		try {
			
			in = clientSocket.getInputStream();
			out = clientSocket.getOutputStream();
			din = new DataInputStream(in);
			dout = new DataOutputStream(out);
			
			//解析头部信息
			JSONObject headStr = readDataStr(din);
			String msgType = headStr.getString("msgType");//消息类型，controlConnect
			String tokenStr = headStr.getString("token");//gzsendi
			String groupName = headStr.getString("groupName");//stcp
			String role = headStr.getString("role"); //control,visitor,controlCli,visitorCli
			if(StringUtils.isEmpty(msgType)) {
				throw new GzsendiException("msgType is null.");
			}
			if(StringUtils.isEmpty(tokenStr)) {
				throw new GzsendiException("token is null.");
			}
			if(StringUtils.isEmpty(groupName)) {
				throw new GzsendiException("groupName is null.");
			}
			if(StringUtils.isEmpty(role)) {
				throw new GzsendiException("role is null.");
			}
			if(!tokenStr.equals(token)) {
				throw new GzsendiException("token is error..");
			}
			this.groupName = groupName;
			this.role = role;
			
			if("control".equals(role)){
				
				new ControlHandler(this,headStr).handler();
				
			}else if("controlCli".equals(role)){
				
				new ControlCliHandler(this,headStr).handler();
				
			}else {
				
				throw new GzsendiException("unkown role..");
				
			}
			
			
		}catch (Exception e) {
			logger.error("",e);
		}finally {
			close();
		}
		
	}
	
	public void close(){
		
		try {
			
			if(clientSocket !=null && !clientSocket.isClosed()) {
				clientSocket.close();
				logger.info(groupName + " -> clientSocket  >>>> " + clientSocket +" socket closed ");
			}
			
		} catch (Exception e1) {
			logger.error("",e1);
		} finally {
			clientSocket = null;
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
		
		//记录进入StcpServer的流量
		FlowCounter.addReceivedSize(2 + resultlength);
		
		return JsonUtil.fromJson(headStr);
	}
	
	public StcpServer getStcpServer() {
		return stcpServer;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public String getRole() {
		return role;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}

}
