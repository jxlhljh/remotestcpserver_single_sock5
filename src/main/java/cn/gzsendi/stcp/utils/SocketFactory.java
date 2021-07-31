package cn.gzsendi.stcp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Base64;

//https://oomake.com/question/1868013
public class SocketFactory {
	
	private static int soTimeOut = 120000; // 5分钟

	public static Socket createHttpProxySocket(String host, Integer port, String proxyHost, Integer proxyPort, String proxyUsername,String proxyPassword) throws IOException {

		Socket sock = new Socket(proxyHost, proxyPort);
		sock.setSoTimeout(soTimeOut);
		String proxyConnect = "CONNECT " + host + ":" + port + " HTTP/1.0";
		proxyConnect = proxyConnect.concat("\r\nProxy-Authorization: Basic "+ Base64.getEncoder().encodeToString(new String(proxyUsername + ":" + proxyPassword).getBytes()));
		proxyConnect = proxyConnect.concat("\r\nHost " + host + ":" + port + "");
		proxyConnect = proxyConnect.concat("\r\n\r\n");

		sock.getOutputStream().write(proxyConnect.getBytes());
		sock.getOutputStream().flush();

		byte[] tmpBuffer = new byte[512];
		InputStream socketInput = sock.getInputStream();
		int len = socketInput.read(tmpBuffer, 0, tmpBuffer.length);
		if (len == 0) {
			throw new SocketException("Invalid response from proxy");
		}
		String proxyResponse = new String(tmpBuffer, 0, len, "UTF-8");
		// Expecting HTTP/1.x 200 OK
		if (proxyResponse.indexOf("200") != -1) {
			// Flush any outstanding message in buffer
			if (socketInput.available() > 0){
				socketInput.skip(socketInput.available());
			}
			// Proxy Connect Successful, return the socket for IO
			return sock;
			
		}else {
			throw new SocketException("Fail to create Socket " + proxyResponse);
		}

	}
	
	public static Socket createSock5ProxySocket(String host, Integer port, String proxyHost, Integer proxyPort, String proxyUsername,String proxyPassword) throws IOException {
		
		
		
		return null;
		
	}


}
