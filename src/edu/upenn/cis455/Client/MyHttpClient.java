package edu.upenn.cis455.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	
	private InputStream socketInputStream;
	public MyHttpClient()
	{
		m_socket = new Socket();
	}
	public void connectTo(String url) throws IOException
	{
		if (url.startsWith("http://") || url.startsWith("HTTP://"))
			m_url = new URL(url);
		else
			m_url = new URL("http://"+url);
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
		socketInputStream = null;
		return true;
	}
	public void send(String method) throws MyClientException, IOException
	{
		if (m_connected == false)
		{
			throw new MyClientException("NOT CONNECTED YET");
		}
		if (!method.equals("GET") && !method.equals("HEAD"))
		{
			throw new MyClientException("METHOD NOT SUPPORTED");
		}
		String path = m_url.getPath();
		if(path.equals(""))
			path = "/";
		PrintWriter pw;
		pw = new PrintWriter(m_socket.getOutputStream(), true);
		pw.println(method+" "+path+" HTTP/1.1");
		pw.println("Host: "+m_url.getHost());
		pw.println("Connection: close");
		pw.println("User-Agent: cis455crawler");	//the header required
		pw.println();

		
	}
	public String [] receive() throws IOException, MyClientException
	{
		socketInputStream = m_socket.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(socketInputStream));
		StringBuffer header = new StringBuffer();
		StringBuffer body = new StringBuffer();
		String line = br.readLine();
		if(!line.matches("(?i)HTTP/1\\..\\s*[23]0[0-9].*"))
		{
			throw new MyClientException("CONTENT NOT ACCESSABLE! Detail:"+line);
		}
		while((line = br.readLine()) != null)
		{
			header.append(line+"\r\n");
			if(line.equals(""))
			{
				break;
			}
		}
		while((line = br.readLine()) != null)
		{
			body.append(line+"\r\n");
		}
		String [] result = {header.toString(), body.toString()};
		return result;
	}
	public InputStream getInputStream()
	{
		return socketInputStream;
	}
	
	
}
