package cn.gzsendi.stcp.server.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gzsendi.stcp.utils.FlowCounter;
import cn.gzsendi.stcp.utils.MessageUtils;
import cn.gzsendi.stcp.visitor.FrontServer;
import cn.gzsendi.system.exception.GzsendiException;
import cn.gzsendi.system.utils.JsonUtil;


public class ControlHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(ControlHandler.class);
	
	private ClientSocketThread clientSocketThread;
	private Map<String,Object> headStr;
	private final static String characterSet = "UTF-8";
	
	public ControlHandler(ClientSocketThread clientSocketThread, Map<String,Object> headStr) {
		this.clientSocketThread = clientSocketThread;
		this.headStr = headStr;
	}
	
	public void handler() throws IOException{
		
		Map<String, ClientSocketThread> groupNames = clientSocketThread.getStcpServer().getGroupNames();
		Map<String, FrontServer> frontServers = clientSocketThread.getStcpServer().getFrontServers();
		String groupName = clientSocketThread.getGroupName();
		
		//控制器只有一个
		if(groupNames.containsKey(groupName)) {
			throw new GzsendiException("duplicate control, groupName: " + groupName);
		}
		
		//
		//启动FrontServer
		if(frontServers.containsKey(groupName)) {
			frontServers.get(groupName).close();
		}
		int frontPort = Integer.parseInt(JsonUtil.getString(headStr, "serverFrontPort"));//本地启动端口
		FrontServer frontServer = new FrontServer(clientSocketThread.getStcpServer(),frontPort, clientSocketThread.getGroupName());
		frontServer.start();
		
		
		try {
			
			//放入内存
			groupNames.put(groupName, clientSocketThread);
			frontServers.put(groupName, frontServer);
			
			while(true){
				
				Map<String,Object> dataStr = readDataStr(clientSocketThread.getDin());
				String msgType = JsonUtil.getString(dataStr, "msgType");//消息类型，controlConnect,controlHeart,visitorConnect,visitorHeart,dataBindReq
				
				//心跳回应
				if("controlHeart".equals(msgType)) {
					
					logger.info(groupName + " -> "  + clientSocketThread + " receive heart pkg request.");
					
					DataOutputStream dout = clientSocketThread.getDout();
					dout.write(MessageUtils.bytessTart);
					String heartStr = "{\"msgType\":\"controlHeart\"}";
					dout.writeInt(heartStr.getBytes(characterSet).length);
					dout.write(heartStr.getBytes(characterSet));
					
				}else {
					
					throw new GzsendiException("unknown msgType.");
					
				}
				
			}
			
		} catch (Exception e) {
			
			try {
				throw e;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
		}finally {
			
			groupNames.remove(groupName);
			
			if(frontServers.containsKey(groupName)) {
				frontServers.get(groupName).close();
			}
			frontServers.remove(groupName);
			
		}
		
	}
	
	//读取socket消息头 , 0x070x07+字符串长度+Json字符串
	private Map<String,Object> readDataStr(DataInputStream dis) throws IOException{
		
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
		
		//记录流经StcpServer的流量
		FlowCounter.addReceivedSize(2 + resultlength);
		
		return JsonUtil.castToObject(headStr);
	}

}
