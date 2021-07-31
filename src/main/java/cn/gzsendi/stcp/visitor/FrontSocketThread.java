package cn.gzsendi.stcp.visitor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gzsendi.stcp.server.handler.ClientSocketThread;
import cn.gzsendi.stcp.server.handler.ControlCliHandler;
import cn.gzsendi.stcp.utils.FlowCounter;
import cn.gzsendi.stcp.utils.MessageUtils;
import cn.gzsendi.system.exception.GzsendiException;
import cn.gzsendi.system.utils.SnowflakeIdWorker;

public class FrontSocketThread extends Thread{
	
	private final static Logger logger = LoggerFactory.getLogger(FrontSocketThread.class);
	private final static String characterSet = "UTF-8";
	
	private int bufferSize = 8092;
	private String globalTraceId = SnowflakeIdWorker.generateId().toString(); //
	private FrontServer frontServer;
	private ControlCliHandler controlCliHandler;//绑定的controlCli
	private Socket frontSocket;
	private String groupName;//分组名称
	private InputStream in;
	private OutputStream out;
	private DataInputStream din;
	private DataOutputStream dout;
	
	public FrontSocketThread(FrontServer frontServer,Socket frontSocket, String groupName){
		this.frontServer = frontServer;
		this.frontSocket = frontSocket;
		this.groupName = groupName;
	}
	
	public void run() {
		
		try {
			
			frontServer.getStcpServer().getFrontSocketThreads().put(globalTraceId, this);
			
			logger.info(groupName + " -> frontSocket>>>> " + frontSocket + "  connected" );
			
			in = frontSocket.getInputStream();
			out = frontSocket.getOutputStream();
			din = new DataInputStream(in);
			dout = new DataOutputStream(out);
			
			//尝试进行后端绑定
			ClientSocketThread control = this.frontServer.getStcpServer().getGroupNames().get(groupName);
			if(control == null ){
				throw new GzsendiException(groupName + " -> no controlClient ready....");
			}
			
			synchronized (control) {
				//下发连接绑定请求给control角色
				String bindRequestStr = "{\"msgType\":\"dataBindReq\",\"groupName\":\""+groupName+"\",\"globalTraceId\":\""+globalTraceId+"\"}";
				control.getDout().write(MessageUtils.bytessTart);
				control.getDout().writeInt(bindRequestStr.getBytes(characterSet).length);
				control.getDout().write(bindRequestStr.getBytes(characterSet));
				
				//记录从StcpServer发送出去的流量
				FlowCounter.addSendSize(2 + 4 + bindRequestStr.getBytes(characterSet).length);
				
			}
			
			
			
			//等10秒未绑定，则直接关闭
			long start = System.currentTimeMillis();
			while(getControlCliHandler() == null && getFrontSocket() != null) {
				long currentTime = System.currentTimeMillis();
				if((currentTime - start ) > 30000l) { //如果超过10秒未绑定成功
					throw new GzsendiException("binded visitorCli failed over 30s...");
				}
				Thread.sleep(10l);//
			}
			
			//成功后进行数据转发
			byte[] data = new byte[bufferSize];
			int len = 0;
			while((len = din.read(data)) > 0){
				if(len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
					
					//记录StcpServer收到的流量
					FlowCounter.addReceivedSize(data.length);
					
					this.controlCliHandler.getClientSocketThread().getDout().write(data);
					
				}else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
					byte[] dest = new byte[len];
					System.arraycopy(data, 0, dest, 0, len);
					
					//记录StcpServer收到的流量
					FlowCounter.addReceivedSize(dest.length);
					
					this.controlCliHandler.getClientSocketThread().getDout().write(dest);
					
				}
			}
			
			
		}catch (Exception e) {
			logger.error("",e);
		}finally {
			
			frontServer.getStcpServer().getFrontSocketThreads().remove(globalTraceId);
			
			close();
			
			if(controlCliHandler !=null){
				
				try {
					controlCliHandler.getClientSocketThread().close();
					logger.info(groupName + " -> controlCliHandler  >>>> " + controlCliHandler +" socket closed ");
				} catch (Exception e1) {
					logger.error("",e1);
				} finally {
					controlCliHandler = null;
				}
			}
			
		}
		
	}
	
	public void close(){
		
		if(frontSocket !=null){
			
			try {
				frontSocket.close();
				logger.info(groupName + " -> frontSocket  >>>> " + frontSocket +" socket closed ");
			} catch (Exception e1) {
				logger.error("",e1);
			} finally {
				frontSocket = null;
			}
		}

	}
	
	public ControlCliHandler getControlCliHandler() {
		return controlCliHandler;
	}

	public void setControlCliHandler(ControlCliHandler controlCliHandler) {
		this.controlCliHandler = controlCliHandler;
	}
	
	public DataOutputStream getDout() {
		return dout;
	}
	
	public Socket getFrontSocket() {
		return frontSocket;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getGlobalTraceId() {
		return globalTraceId;
	}
}
