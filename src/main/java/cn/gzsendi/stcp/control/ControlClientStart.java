package cn.gzsendi.stcp.control;

import java.util.Arrays;
import java.util.List;

import cn.gzsendi.stcp.config.ControlClientConfig;

public class ControlClientStart {

	public static String token = ControlClientConfig.token;
	public static String trunnelHost = ControlClientConfig.trunnelHost;
	public static int trunnelPort = ControlClientConfig.trunnelPort;
	
	public static boolean ssl = ControlClientConfig.ssl;

	public static List<String> groups = ControlClientConfig.groups;
	public static List<String> types = ControlClientConfig.types;
	public static List<String> remoteHosts = ControlClientConfig.remoteHosts;
	public static List<String> remotePorts = ControlClientConfig.remotePorts;
	public static List<String> serverFrontPorts = ControlClientConfig.serverFrontPorts;

	public static boolean needProxy = ControlClientConfig.needProxy;
	public static String proxyType = ControlClientConfig.proxyType;
	public static String proxyHost = ControlClientConfig.proxyHost;
	public static int proxyPort = ControlClientConfig.proxyPort;
	public static String proxyUsername = ControlClientConfig.proxyUsername;
	public static String proxyPassword = ControlClientConfig.proxyPassword;

	public static void main(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if ("-trunnelHost".equals(args[i])) {
				trunnelHost = args[i + 1];
				i++;
			} else if ("-trunnelPort".equals(args[i])) {
				trunnelPort = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-token".equals(args[i])) {
				token = args[i + 1];
				i++;
			} else if ("-groups".equals(args[i])) {
				groups = Arrays.asList(args[i + 1].split(","));
				i++;
			} else if ("-types".equals(args[i])) {
				types = Arrays.asList(args[i + 1].split(","));
				i++;
			} else if ("-remoteHosts".equals(args[i])) {
				remoteHosts = Arrays.asList(args[i + 1].split(","));
				i++;
			} else if ("-remotePorts".equals(args[i])) {
				remotePorts = Arrays.asList(args[i + 1].split(","));
				i++;
			} else if ("-serverFrontPorts".equals(args[i])) {
				serverFrontPorts = Arrays.asList(args[i + 1].split(","));
				i++;
			} else if ("-proxyType".equals(args[i])) {
				proxyType = args[i + 1];
				i++;
			} else if ("-needProxy".equals(args[i])) {
				needProxy = Boolean.parseBoolean(args[i + 1]);
				i++;
			} else if ("-proxyHost".equals(args[i])) {
				proxyHost = args[i + 1];
				i++;
			} else if ("-proxyPort".equals(args[i])) {
				proxyPort = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-proxyUsername".equals(args[i])) {
				proxyUsername = args[i + 1];
				i++;
			} else if ("-proxyPassword".equals(args[i])) {
				proxyPassword = args[i + 1];
				i++;
			} else if ("-ssl".equals(args[i])) {
		    	ssl = Boolean.parseBoolean(args[i+1]);
		    	i++;
		     }

		}

		for (int i = 0; i < groups.size(); i++) {

			String groupName = groups.get(i);
			String typeName = types.get(i);
			String remoteHost = remoteHosts.get(i);
			int remotePort = Integer.parseInt(remotePorts.get(i));
			int serverFrontPort = Integer.parseInt(serverFrontPorts.get(i));
			new ControlClient(groupName,typeName, remoteHost, remotePort, serverFrontPort).start();

		}

	}

}
