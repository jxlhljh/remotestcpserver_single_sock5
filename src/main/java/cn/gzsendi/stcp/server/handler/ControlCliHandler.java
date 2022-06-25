package cn.gzsendi.stcp.server.handler;

import java.io.DataInputStream;
import java.util.Map;

import cn.gzsendi.stcp.utils.FlowCounter;
import cn.gzsendi.stcp.visitor.FrontSocketThread;
import cn.gzsendi.system.exception.GzsendiException;
import cn.gzsendi.system.utils.JsonUtil;

public class ControlCliHandler {
	
	private int bufferSize = 8092;
	private FrontSocketThread frontSocketThread;
	private ClientSocketThread clientSocketThread;
	private Map<String,Object> headStr;
	
	public ControlCliHandler(ClientSocketThread clientSocketThread,Map<String,Object> headStr){
		this.clientSocketThread = clientSocketThread;
		this.headStr = headStr;
	}
	
	public void handler() throws Exception{

		try {
			
			Map<String,FrontSocketThread> frontSocketThreads = clientSocketThread.getStcpServer().getFrontSocketThreads();
			String globalTraceId = JsonUtil.getString(headStr, "globalTraceId"); 
			FrontSocketThread frontSocketThread = frontSocketThreads.get(globalTraceId);
			
			//frontSocketThread没有连接
			if(frontSocketThread == null) {
				throw new GzsendiException("no frontSocketThread connected, globalTraceId: " + globalTraceId);
			}
			
			//绑定成功
			this.frontSocketThread = frontSocketThread;
			this.frontSocketThread.setControlCliHandler(this);
			
			//绑定成功后进行数据转发
			byte[] data = new byte[bufferSize];
			int len = 0;
			DataInputStream din = this.getClientSocketThread().getDin();
			while((len = din.read(data)) > 0){
				if(len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
					
					//记录从StcpServer发送出去的流量
					FlowCounter.addSendSize(data.length);
					
					frontSocketThread.getDout().write(data);
					
				}else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
					byte[] dest = new byte[len];
					System.arraycopy(data, 0, dest, 0, len);
					
					//记录从StcpServer发送出去的流量
					FlowCounter.addSendSize(dest.length);
					
					frontSocketThread.getDout().write(dest);
					
				}
			}
			
			
		} catch (Exception e) {
			
			throw e;
			
		}finally {
			
			if(frontSocketThread != null) {
				
				frontSocketThread.close();
				
			}
			frontSocketThread = null;
			
		}
		
	}
	
	public ClientSocketThread getClientSocketThread() {
		return clientSocketThread;
	}

	public void setClientSocketThread(ClientSocketThread clientSocketThread) {
		this.clientSocketThread = clientSocketThread;
	}

}
