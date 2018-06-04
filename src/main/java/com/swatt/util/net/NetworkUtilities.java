package com.swatt.util.net;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.swatt.util.io.IoUtilities;

public class NetworkUtilities {

	public static void close(Socket socket) { close(null, null, socket); }
	
	public static void close(InputStream in, OutputStream out, Socket socket) {
		if (out != null)
			IoUtilities.close(out);

		if (in != null)
			IoUtilities.close(in);

		try { 
			if (socket != null)
				socket.close();
		} catch(IOException e) { }
	}
	
	public static void close(ServerSocket serverSocket) {
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) { }
	}
		
	public static ServerSocket createServerSocket() throws IOException {
		ServerSocket serverSocket = new ServerSocket(0);
		return serverSocket;
	}
	
	public static boolean isPortAvailable(int port) {
		ServerSocket ss = createServerSocketIfAvailable(port);
		
		if (ss != null) {
			close(ss);
			return true;
		} else
			return false;
	}
	
	public static ServerSocket createServerSocketIfAvailable(int port) {
		ServerSocket ss = null;
		
		try {
			ss = new ServerSocket(port);
			return ss;
		} catch (Throwable t) {
			return null;
		} 
	}
	
	public static boolean isPublicAddress(String host) throws UnknownHostException {
		try {
			return isPublicAddress(InetAddress.getByName(host));
		} catch (Exception e) {
			return true;
		}
	}
		
	public static boolean isPublicAddress(InetAddress inetAddress) {
		int addr[] = toIntArray(inetAddress.getAddress());

		if (addr[0] == 10)
			return false;

		if ( (addr[0] == 192) && (addr[1] == 168) )
			return false;

		if ( (addr[0] == 127) && (addr[1] == 0) && (addr[2] == 0) && (addr[3] == 1)) // Loopback address
			return false;
		
		// Fix-Me: We still need to test for the Microsoft "local" addresses  (we have to research what they are)

		return true;
	}

	public static boolean isLoopbackAddress(String address) {
		return (address.equalsIgnoreCase("localhost") || address.trim().equals("127.0.0.1"));
	}

	public static boolean isLoopbackAddress(InetAddress inetAddress) {
		int addr[] = toIntArray(inetAddress.getAddress());
		return (  (addr[0] == 127) && (addr[1] == 0) && (addr[2] == 0) && (addr[3] == 1) );
	}
	
	public static String[] getPublicIpAddresses() throws IOException {
		LinkedList<String> publicIps = new LinkedList<String>();
		String[] allIps = getIpAddresses();
		for (int i = 0; i < allIps.length; i++) {
			String ip = allIps[i];
			if (isPublicAddress(ip))
				publicIps.add(ip);
		}
		return (String[])publicIps.toArray(new String[0]);
	}

	public static String[] getPrivateIpAddresses() throws IOException {
		LinkedList<String> privateIps = new LinkedList<String>();
		String[] allIps = getIpAddresses();
		for (int i = 0; i < allIps.length; i++) {
			String ip = allIps[i];
			if (!isPublicAddress(ip))
				privateIps.add(ip);
		}
		return (String[])privateIps.toArray(new String[0]);
	}

	public static String getBestIpAddress() throws IOException {
		String[] hosts = getPublicIpAddresses();
		if (hosts.length > 0)
			return hosts[0];
		else {
			hosts = getPrivateIpAddresses();
			if (hosts.length > 0)
				return hosts[0];
		}
		return "127.0.0.1";
	}
		
	private static boolean isWindowsFamily() {
		String osName = System.getProperty("os.name");
		if (osName.toUpperCase().startsWith("WIN")) return true;
		return false;
	}
	
	public static String[] getIpAddresses() throws IOException {
		if (isWindowsFamily())
			return getWindowsIpAddresses();
		else {
			
			String[] retVal = new String[] {};
			try { retVal = getUnixIpAddresses("/sbin/ifconfig -a"); }
			catch(IOException e) {
				retVal = getUnixIpAddresses("/usr/sbin/ifconfig -a");
			}
			return retVal;
		}
	}

	private static String[] getWindowsIpAddresses() throws IOException {
		ArrayList<String> addresses = new ArrayList<String>();
		
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("ipConfig");
		InputStream in = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		for (;;) {
			String line = reader.readLine();
			if (line == null) 
				break;

			if(line.indexOf("IP Address") == -1) 
				continue;

			int pos = line.indexOf(":");
			String address = line.substring(pos + 1).trim();
			if (address.indexOf(":") == -1) // ignore IPv6 addresses. 
				addresses.add(address);
				
		}

		return (String[]) addresses.toArray(new String[0]);
	}

	private static String[] getUnixIpAddresses(String command) throws IOException {
		ArrayList<String> addresses = new ArrayList<String>();
		
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);
		InputStream in = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		for (;;) {
			String line = reader.readLine();		
			if (line == null) 
				break;
			int index = line.indexOf("inet") ;
			if(index == -1)
				continue;


			int dotPos = line.indexOf(".");
			if (dotPos == -1)
				continue;
				
			line = line.substring(dotPos-3);

			int semiPos = line.indexOf(":");
			if (semiPos != -1 && semiPos<3)
				line = line.substring(semiPos+1);

			int spacePos = line.indexOf(" ");
			if (spacePos>0 && spacePos<3)
				line = line.substring(spacePos);

			spacePos = -1;
			spacePos = line.indexOf(" ");
			if (spacePos != -1)
				line = line.substring(0,spacePos);
			
			String address = line.trim();
			if (address.indexOf(";") == -1) //ignore IPv6 addresses
				addresses.add(address);
		}
		

		return (String[]) addresses.toArray(new String[0]);
	}

	private static int[] toIntArray(byte addr[]) {
		int result[] = new int[addr.length];
		
		for (int i=0; i < addr.length; i++) 
			result[i] =  addr[i] & 0xff;

		return result;
	}

	public static boolean isNumericHostName(String host)  {

		if (host == null || host.length() == 0)
			return false;
			
		if (!Character.isDigit(host.charAt(0))) {
		    return false;
		} else {	// see if it is of the format d.d.d.d 	where d is a digit between 0-255
		    int numDots = 0;
		
		    for(int i = 0; i < host.length(); i++) {
				char c = host.charAt(i);
				
				if (c < '0' || c > '9') 
				    return false;

				// we are at the first digit of a block
				
				int b = c - '0';
				i++;

				for (; i < host.length(); i++) {
					c = host.charAt(i);
					
					if (c < '0' || c > '9')	{	
						if (c == '.') {		// end of block
							numDots++;
							break;
						} else
							return false;	//  only digits or '.' are valid
					}

					b = b*10 + c - '0';
				}
				
				if(b > 255) 				// only values 0 - 255 are valid
				    return false;

		    }
		
		    if(numDots != 3)
				return false;
		}

		return true;
	}
	
	public static final String getBestHostName() {
		try {
			String ip = getBestIpAddress();
			if (ip != null)
				return getHostName(ip);
			else
				return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static final String getHostName(String host) throws UnknownHostException {
		return InetAddress.getByName(host).getCanonicalHostName();
	}
	
/////////////////////////
	

	public static String getLocalAddressText() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostName();
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
	private static void addAddresses(ArrayList<String> addresses, InetAddress inetAddress) {
		String hostName = inetAddress.getHostName().toUpperCase();
		String hostAddress = inetAddress.getHostAddress().toUpperCase();

		if (!addresses.contains(hostName)) 
			addresses.add(hostName);
		
		if (!addresses.contains(hostAddress))
			addresses.add(hostAddress);
	}
	
	public static ArrayList<String> getAllLocalAddressTexts() {
		ArrayList<String> localHostTexts = new ArrayList<String>();
		String canonicalHostName = null;
		
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			
			addAddresses(localHostTexts, inetAddress);
			
			canonicalHostName = inetAddress.getCanonicalHostName().toUpperCase();
			
			if (!localHostTexts.contains(canonicalHostName))
				localHostTexts.add(canonicalHostName);
			
		} catch (UnknownHostException e) {}
		
		try {
			InetAddress[] inetAddresss = InetAddress.getAllByName(canonicalHostName);
		
			for (int i = 0; i < inetAddresss.length; i++) {
				InetAddress inetAddress = inetAddresss[i];
				
				addAddresses(localHostTexts, inetAddress);			
			}
			
		} catch (UnknownHostException e) { 	}
		
		return localHostTexts;
	}
	
	
	public static boolean isLocalAddress(String hostName) {
		if (hostName.equalsIgnoreCase("localhost"))
			return true;
		else if (hostName.indexOf("127.0") == 0) 
			return true;	// OK, this is lazy, we should cehck for well formedness
		else {
			hostName = hostName.toUpperCase();
			return getAllLocalAddressTexts().contains(hostName);
		}
	}
	
	public static InputStream openUrlStream(String url) throws IOException {
		return openStream(new URL(url));
	}
	
	public static InputStream openStream(URL url) throws IOException {
		return url.openStream();
	}

	
	public static String getContentAsString(String url) throws IOException {
		return getContentAsString(new URL(url));
	}
	
	public static byte[] getContentFromUrl(String url) throws IOException {
		return getContent(new URL(url));
	}
	
	public static String getContentAsString(URL url) throws IOException {
		InputStream in = openStream(url);
		return IoUtilities.streamToString(in);
	}
	
	public static byte[] getContent(URL url) throws IOException {
		InputStream in = openStream(url);
		return IoUtilities.streamToBytes(in);
	}	
	
	public static void downloadContent(String url, String fileName) throws IOException {
		downloadContent(new URL(url), new File(fileName));
	}
	
	public static void downloadContentToDirectory(String url, String dirName) throws IOException {
		downloadContent(new URL(url), new File(dirName));
	}
	
	public static void downloadContentToDirectory(String url, File dir) throws IOException {
		downloadContentToDirectory(new URL(url), dir);
	}

	public static void downloadContentToDirectory(URL url, File dir) throws IOException {
		String fileName = url.getFile();
		int pos1 = fileName.lastIndexOf('/');
		int pos2 = fileName.lastIndexOf('\\');
		int pos = Math.max(pos1, pos2);
		
		if ((fileName.length() == 0) || (pos == (fileName.length()-1)))
			fileName = "index.html";
		else if (pos != -1)
			fileName = fileName.substring(pos+1);
		
		downloadContent(url, new File(dir, fileName));
	}

	
	public static void downloadContent(String url, File file) throws IOException {
		downloadContent(new URL(url), file);
	}
	
	
	public static void downloadContent(URL url, File file) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		
		try {
			URLConnection urlConnection = url.openConnection();
			
			if (urlConnection instanceof HttpURLConnection) {
				HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

				int responseCode = httpURLConnection.getResponseCode();
				
				if (responseCode != HttpURLConnection.HTTP_OK)
					throw new IOException("Got Response Code: " + responseCode);
			}
			
			in = urlConnection.getInputStream();
			out = new FileOutputStream(file);
			IoUtilities.copyStream(in, out);
		} finally {
			IoUtilities.close(out);
			IoUtilities.close(in);
		}
		
	}
}
