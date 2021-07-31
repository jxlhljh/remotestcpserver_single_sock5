package cn.gzsendi.stcp.control;

import java.util.Arrays;
import java.util.List;

public class ControlClientStart {

	public static String token = "gzsendi";
	public static String trunnelHost = "127.0.0.1";
	public static int trunnelPort = 7000;
	
	public static boolean ssl = true;

	public static List<String> groups = Arrays.asList("stcp1");
	public static List<String> types = Arrays.asList("tcp");
	public static List<String> remoteHosts = Arrays.asList("192.168.60.133");
	public static List<String> remotePorts = Arrays.asList("8899");
	public static List<String> serverFrontPorts = Arrays.asList("18899");

	public static boolean needProxy = false;
	public static String proxyType = "socks";
	public static String proxyHost = "172.168.201.131";
	public static int proxyPort = 19080;
	public static String proxyUsername = "sendi";
	public static String proxyPassword = "sendi123";

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
