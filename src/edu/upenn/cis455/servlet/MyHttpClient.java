package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

public class MyHttpClient {
	
	private Socket m_socket;
	private boolean m_connected;
	private URL m_url;
	
	public MyHttpClient()
	{
		m_socket = new Socket();
	}
	public void connectTo(String url) throws IOException
	{
		m_url = new URL(url);
		String host = m_url.getHost();
		int port = m_url.getPort();
		if (port == -1)
			port = 80;
		InetSocketAddress iaddr = new InetSocketAddress(host, port);
		m_socket.connect(iaddr);
		m_connected = true;
	}
	public boolean closeConnection() throws IOException
	{
		m_socket.close();
		m_socket = new Socket();
		m_connected = false;
		return true;
	}
	public void send(String method) throws Exception
	{
		if (m_connected == false)
		{
			throw new Exception("NOT CONNECTED YET");
		}
		if (!method.equals("GET") && !method.equals("HEAD"))
		{
			throw new Exception("METHOD NOT SUPPORTED");
		}
		String path = m_url.getPath();
		if(path.equals(""))
			path = "/";
		PrintWriter pw = new PrintWriter(m_socket.getOutputStream(), true);
		pw.println(method+" "+path+" HTTP/1.1");
		pw.println("Host: "+m_url.getHost());
		pw.println("Connection: close");
		pw.println();
	}
	public String receive() throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
		StringBuffer body = new StringBuffer();
		String line = null;
		while((line = br.readLine()) != null)
		{
			System.out.println(line);
		}
		return null;
	}
}
