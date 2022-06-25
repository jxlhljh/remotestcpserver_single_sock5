package cn.gzsendi.stcp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.gzsendi.system.utils.JsonUtil;
import cn.gzsendi.system.utils.YmlUtils;

public class ControlClientConfig {
	
	private static final String fileName = "controlClient.yml";

	public static final String token = YmlUtils.getValue(fileName,"token", "123456").toString();
	public static final boolean ssl = (boolean)YmlUtils.getValue(fileName,"ssl", false);
	
	public static final String trunnelHost = YmlUtils.getValue(fileName,"trunnelHost", "127.0.0.1").toString();
	public static final int trunnelPort = Integer.parseInt(YmlUtils.getValue(fileName,"trunnelPort", 7000).toString());

	public static final boolean needProxy = (boolean)YmlUtils.getValue(fileName,"needProxy", false);
	public static final String proxyType = YmlUtils.getValue(fileName,"proxyType", "http").toString();//http, sock5
	public static final String proxyHost = YmlUtils.getValue(fileName,"proxyHost", "127.0.0.1").toString();//
	public static final int proxyPort = Integer.parseInt(YmlUtils.getValue(fileName,"proxyPort", 1080).toString());
	public static final String proxyUsername = YmlUtils.getValue(fileName,"proxyUsername", "sendi").toString();//
	public static final String proxyPassword = YmlUtils.getValue(fileName,"proxyPassword", "sendi123").toString();//
	
	public static List<String> groups = new ArrayList<String>();
	public static List<String> types = new ArrayList<String>();
	public static List<String> serverFrontPorts = new ArrayList<String>();
	public static List<String> remoteHosts = new ArrayList<String>();
	public static List<String> remotePorts = new ArrayList<String>();
	
	static {
		
		Object configs = YmlUtils.getValue(fileName,"configs");
		if(configs != null){
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<Map<String,Object>> list = (List)configs;
			for(Map<String,Object> aConfig : list) {
				
				String groupStr = JsonUtil.getString(aConfig, "group");
				String typeStr = JsonUtil.getString(aConfig, "type");
				String serverFrontPort = JsonUtil.getInteger(aConfig, "serverFrontPort").toString();
				
				groups.add(groupStr);
				types.add(typeStr);
				serverFrontPorts.add(serverFrontPort);
				
				if(!"sock5".equals(typeStr)){
					
					String remoteHostStr = JsonUtil.getString(aConfig, "remoteHost");
					String remotePortStr = JsonUtil.getInteger(aConfig, "remotePort").toString();
					remoteHosts.add(remoteHostStr);
					remotePorts.add(remotePortStr);
					
				}else {
					
					//虚拟出数据进行填充,程序中不会用到，但没有的话序号会乱。
					remoteHosts.add("0.0.0.0");
					remotePorts.add("9999");
					
				}
				
			}
		}
		
	}
}
