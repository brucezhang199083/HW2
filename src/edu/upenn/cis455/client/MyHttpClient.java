package edu.upenn.cis455.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



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
		connectTo(m_url);
	}
	public void connectTo(URL url) throws IOException
	{
		m_url = url;
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
		if (line == null)
			throw new MyClientException("NOTHING TO READ!!!");
		if(!line.matches("(?i)HTTP/1\\..\\s*[23]0[0-9].*"))
		{
			throw new MyClientException("CONTENT NOT ACCESSABLE! "+m_url+"Detail:"+line);
		}
		while((line = br.readLine()) != null)
		{
			header.append(line+"\r\n");
			if(line.equals(""))
			{
				break;
			}
		}
		HashMap<String , List<String> > hm = parseHeader(header.toString());
		if (hm.containsKey("transfer-encoding"))
		{
			if (hm.get("transfer-encoding").get(0).equals("chunked"))
			{
				try
				{
					while((line = br.readLine()) != null)
					{
						//System.out.println(line);
						int numChar = Integer.parseInt(line, 16);
						System.out.println("chunksize:"+numChar);
						if(numChar == 0)
							break;
						int data;
						for(int i = 0; i < numChar; i++)
						{
							data = br.read();
							if (data == -1)
								throw new MyClientException("chunk end unexpectedly");
							else
								body.append((char) data);
						}
						
					}
				}
				catch (NumberFormatException | IOException e)
				{
					e.printStackTrace();
					throw new MyClientException("Error parsing chunked data");
				}
			}
			else
			{
				while((line = br.readLine()) != null)
				{
					body.append(line+"\r\n");
				}
			}
		}
		else
		{
			while((line = br.readLine()) != null)
			{
				body.append(line+"\r\n");
			}
		}
		String [] result = {header.toString(), body.toString()};
		return result;
	}
	public InputStream getInputStream()
	{
		return socketInputStream;
	}
	
	public HashMap <String, List<String> > parseHeader(String str) throws IOException
	{
		BufferedReader bsr = new BufferedReader(new StringReader(str));
		HashMap <String, List<String> > m_header = new HashMap<String, List<String> >();
		String lastHeader = null;
		boolean headerend = false;
		while(true)	//Create the header map
	    {
	    	String s1 = bsr.readLine();
	    	////System.out.println(s1);
	    	if(s1 != null)
	    	{
	    		if(s1.isEmpty())
	    		{
	    			headerend = true;
	    			continue;
	    		}
	    		if(!headerend)
	    		{
	    			if(Character.isWhitespace(s1.charAt(0)))
	    			{
	    				if(lastHeader == null)
	    				{
	    					//TODO: Invalid request format, 
	    					
	    				}
	    				List<String> a = m_header.get(lastHeader);
	    				a.set(a.size()-1,a.get(a.size()-1).concat(s1.trim()));
	    				continue;
	    			}
		    		String headerpair[] = s1.split(":", 2);
		    		if(headerpair.length != 2)
		    		{
		    			//out.println("Wrong Header Format");
		    			
		    		}
		    		else
		    		{
		    			List<String> a = m_header.get(headerpair[0].toLowerCase());
		    			if(a != null)
		    				a.add(headerpair[1].trim());
		    			else
		    			{
		    				List<String> n = new ArrayList<String>();
		    				n.add(headerpair[1].trim());
			    			m_header.put(headerpair[0].toLowerCase(), n);
			    			lastHeader = headerpair[0].toLowerCase();
		    			}
		    		}
	    		}
	    	}
	    	else
	    		break;
	    }
		return m_header;
	}
}
