package cn.gzsendi.stcp.utils;

public class FlowCounter {
	
	public static long totalSendSize; //流量总发送大小
	public static long totalReceivedSize; //流量总接收大小
	
	public static synchronized void addSendSize(long sendSize) {
		totalSendSize += sendSize;
	}
	
	public static synchronized void addReceivedSize(long receivedSize) {
		
		totalReceivedSize += receivedSize;
		
	}

}
