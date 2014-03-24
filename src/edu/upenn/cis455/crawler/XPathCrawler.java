package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import edu.upenn.cis455.client.MyClientException;
import edu.upenn.cis455.client.MyHttpClient;
import edu.upenn.cis455.storage.*;

public class XPathCrawler {
	// Static arguments
	static String StoragePath;
	static String StartingPage;
	static int MaximumSize;
	static int MaximumDocNumber;

	HashMap<String, RobotRules> robotsMap;
	Queue<URL> crawlingQueue;
	Set<String> crawledSet;

	XPathCrawler()
	{
		robotsMap = new HashMap<String, RobotRules>();
		crawlingQueue = new LinkedList<URL>();
		crawledSet = new HashSet<String>();
	}
	
	// Parse robots.txt for a specific host
	String getHostAndPort(URL url)
	{
		return (url.getHost() + (url.getPort() == -1 ? "" : (":"+url.getPort())));
	}

	RobotRules parseRobotsTxt(URL url) throws IOException
	{
		MyHttpClient mhc = new MyHttpClient();
		String host = getHostAndPort(url);
		// If not visited, create the robots map for this site
		// Else, we return
		RobotRules robot = new RobotRules();
		if (!robotsMap.containsKey(host))
			this.robotsMap.put(host, robot);
		else
			return this.robotsMap.get(host);
		mhc.connectTo(host+"/robots.txt");
		try {
			mhc.send("GET");
			String [] handb = null;
			try
			{
				handb = mhc.receive();
			}
			catch (MyClientException e)
			{
				e.printStackTrace();
				return robot;
			}
			mhc.closeConnection();

			StringReader robotsReader = new StringReader(handb[1]);
			BufferedReader brRobot = new BufferedReader(robotsReader);
			// Parse robots.txt of this host
			String line = null;
			boolean ignore = true;
			boolean hasMatch = false;
			while ((line = brRobot.readLine()) != null)
			{

				String pair = line.replaceAll("\\s*", "");
				// Ignore empty lines and comments
				if (pair.equals("") || pair.startsWith("#"))
					ignore = true;
				else if (pair.matches("(?i)User-agent:.*"))
				{
					// only care about cis455crawler rules
					if (pair.equalsIgnoreCase("User-agent:cis455crawler"))
					{
						ignore = false;
						hasMatch = true;
						robot.clear();
					}
					// If no rules for cis455crawler, then follow the generic rules
					else if (pair.equalsIgnoreCase("User-agent:*"))
					{
						if (hasMatch)
							ignore = true;
						else
							ignore = false;
					}
				}
				if(ignore)	
					continue;
				else	// parse the rules
				{
					String [] rule = pair.split(":");
					if (rule.length != 2)
						continue;
					else
					{
						// Only cares about disallow rules
						if (rule[0].equalsIgnoreCase("Disallow"))
						{
							robot.addDisallow(rule[1]);
						}
						else if (rule[0].equalsIgnoreCase("Crawl-delay"))
						{
							robot.setDelay(Integer.parseInt(rule[1]));
						}
					}
				}
			}
			return robot;
		} catch (MyClientException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
	}

	private static void Usage()
	{
		System.out.println("Name: Hao Zhang");
		System.out.println("SEAS Login: zhanghao");
		System.out.println("Usage: java XPathCrawler StartingPage StorageDir MaxSize [MaxDocNum]");
	}
	
	// Main
	public static void main(String args[]) throws InterruptedException
	{
		/* TODO: Implement crawler */
		if (args.length != 3 && args.length != 4)
		{
			System.out.println("Error arg num");
			Usage();
			return;
		}
		StartingPage = args[0];
		StoragePath = args[1];

		try {
			MaximumSize = Integer.parseInt(args[2]);
			if (args.length == 3)	//No MaxDocNum specified
			{
				MaximumDocNumber = -1;
			}
			else
			{
				MaximumDocNumber = Integer.parseInt(args[3]);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Usage();
			return;
		}
		XPathCrawler instance = new XPathCrawler();
		BDBStorage bdb = new BDBStorage(StoragePath);
		try {
			URL seeds = null;
			if (!StartingPage.startsWith("http://"))
				seeds = new URL("http://"+StartingPage);
			else
				seeds = new URL(StartingPage);
			instance.crawlingQueue.add(seeds);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("URL FORMAT ERROR!");
			System.out.println(StartingPage);
			Usage();
			return;
		}
		try {
			int count = 0;
			while(instance.crawlNext(bdb) != null)
			{
				count++;
				if (MaximumDocNumber > 0 && count >= MaximumDocNumber)
					break;
				else
					continue;
			}
			
			System.out.println(instance.crawledSet.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	Boolean crawlNext(BDBStorage storage) throws IOException, InterruptedException
	{
		URL nextURL = crawlingQueue.poll();
		//Thread.sleep(1000);
		if(nextURL == null)
		{
			return null;
		}
		else
		{			
			if (crawledSet.contains(nextURL.toString()))
				return false;
			RobotRules rule = null;
			try
			{
				rule = this.parseRobotsTxt(nextURL);
			}
			catch (UnknownHostException e)
			{
				System.out.println("Host not found!");
				return false;
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}
			if (!rule.isCrawlable())
			{
				crawlingQueue.add(nextURL);
				//Thread.sleep(5000);
				return false;
			}
			else if(rule.isDisallowed(nextURL.getPath()+"?"+nextURL.getQuery()))
			{
				// the content is disallowed
				return false;
			}
			else
			{
				// We actually crawled this URL
				crawledSet.add(nextURL.toString());
				// Make connection
				MyHttpClient mhc = new MyHttpClient();
				mhc.connectTo(nextURL);
				// Send head to get info
				try {
					mhc.send("HEAD");
					System.out.println("Head sended");
					String [] headerarray = null;
					try
					{
						headerarray = mhc.receive();
					}
					catch (MyClientException e)
					{
						e.printStackTrace();
						return false;
					}
					//System.out.println(headerarray[0]);
					mhc.closeConnection();
					HashMap<String, List<String> > headerMap = mhc.parseHeader(headerarray[0]);
					//System.out.println(headerMap);
					if (headerMap.containsKey("content-length"))
					{
						int contentLength = Integer.parseInt(headerMap.get("content-length").get(0));
						if (contentLength > MaximumSize)
						{
							System.out.println(nextURL+" : File size exceeds limit, discarding...");
							return false;
						}
					}
					if (headerMap.containsKey("content-type"))
					{
						String type = headerMap.get("content-type").get(0).split(";")[0].trim();					
						if (type.matches("text/html"))
						{
							// retrieve the body and push links\
							mhc.connectTo(nextURL);
							rule.access();
							mhc.send("GET");
							System.out.println(nextURL+" : HTML Downloading...");
							System.out.println("Get sended");
							String [] handb = null;
							try
							{
								handb = mhc.receive();
							}
							catch (MyClientException e)
							{
								System.out.println("Error when receiving, discarding...");
								return false;
							}
							mhc.closeConnection();
							if (handb[1].length() > MaximumSize)
							{
								System.out.println(nextURL+" : File size exceeds limit, discarding...");
								return false;
							}
							//Store raw html first
							storage.putDocument(nextURL.toString(), handb[1]);
							
							// parse Document using Jsoup
							Document d = Jsoup.parse(handb[1]);
							Elements linklist = d.select("a[href]");
							// go on crawling
							List<URL> newURLs = new LinkedList<URL>();
							for(Element e : linklist)
							{
								String href = e.attr("href");
								if (href.equals(""))	// Try Upper case
								{
									href = e.attr("HREF");
								}
								if (href.equals(""))	// There is no href...
								{
									continue;
								}
								else
								{
									//System.out.println(e);
									String fullurl = null;
									if(href.matches("(?i)^http://.*"))
									{
										fullurl = href;
									}
									else if(href.matches("^/.*"))
									{
										fullurl = "http://"+getHostAndPort(nextURL)+href;
									}
									else
									{
										if(href.matches("[^:/]*:.*"))
										{
											String [] protocolpart = href.split(":");
											if (!protocolpart[0].equalsIgnoreCase("http"))
												continue;
										}
										String [] urlpart = nextURL.toString().split("\\?");
										String start = urlpart[0];
										if(start.endsWith("/"))
											fullurl = start+href;
										else
											fullurl = start+"/"+href;
									}
									newURLs.add(new URL(fullurl));
								}
							}
							crawlingQueue.addAll(newURLs);
						}
						else if(type.matches(".*/xml"))
						{
							mhc.connectTo(nextURL);
							mhc.send("GET");
							rule.access();
							System.out.println(nextURL+" : XML Downloading...");
							String [] handb = mhc.receive();
							
							if (handb[1].length() > MaximumSize)
								return false;
							DocumentBuilderFactory factory  =  DocumentBuilderFactory.newInstance(); 
							DocumentBuilder documentBuilder;
							org.w3c.dom.Document doc = null;
							try {
								documentBuilder = factory.newDocumentBuilder();
								doc = documentBuilder.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(handb[1].getBytes()),"UTF-8")));
								// store and no more crawling for XML
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SAXException e) {
								// TODO Auto-generated catch block
								System.out.println("Malformed XML");
								e.printStackTrace();
							}
						}
						else
						{
							System.out.println(nextURL+" : Content type mismatch, discarding...");
							return false;
						}
					}	// content-type
					else
					{
						System.out.println(nextURL+" : Content type not found, discarding...");
						return false;
					}
				} catch (MyClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		return false;
	}
}
