package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
		RobotRules robot = new RobotRules();
		if (!robotsMap.containsKey(host))
			this.robotsMap.put(host, robot);
		else
			return this.robotsMap.get(host);
		mhc.connectTo(host+"/robots.txt");
		try {
			mhc.send("GET");
			String [] handb = mhc.receive();
			mhc.closeConnection();
			

			// If not visited, create the robots map for this site
			// Else, we return
			

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
			while(instance.crawlNext() != null)
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

	Boolean crawlNext() throws IOException, InterruptedException
	{
		URL nextURL = crawlingQueue.poll();
		//Thread.sleep(1000);
		if(nextURL == null)
		{
			return null;
		}
		else
		{
			System.out.println(nextURL);
			if (crawledSet.contains(nextURL.toString()))
				return false;
			else
				crawledSet.add(nextURL.toString());
			RobotRules rule = this.parseRobotsTxt(nextURL);
			if (!rule.isCrawlable())
			{
				crawlingQueue.add(nextURL);
				crawledSet.remove(nextURL.toString());
				//Thread.sleep(5000);
				return false;
			}
			else
			{
				MyHttpClient mhc = new MyHttpClient();
				mhc.connectTo(nextURL);
				// Send head to get info
				try {
					mhc.send("HEAD");
					String [] headerarray = mhc.receive();
					mhc.closeConnection();
					HashMap<String, List<String> > headerMap = mhc.parseHeader(headerarray[0]);
					//System.out.println(headerMap);
					if (headerMap.containsKey("content-length"))
					{
						int contentLength = Integer.parseInt(headerMap.get("content-length").get(0));
						if (contentLength > MaximumSize)
							return false;
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
							String [] handb = mhc.receive();
							mhc.closeConnection();
							if (handb[1].length() > MaximumSize)
							{
								System.out.println("Content Length exceeds the limit");
								return false;
							}
							Tidy tidy = new Tidy();
							tidy.setTidyMark(false);
							tidy.setXmlOut(true);
							tidy.setShowWarnings(false);
							tidy.setQuiet(true);
							tidy.setForceOutput(true);

							
							ByteArrayInputStream bin = new ByteArrayInputStream(handb[1].getBytes());
							ByteArrayOutputStream bout = new ByteArrayOutputStream();
							tidy.parse(bin, bout);
							String xml = bout.toString();
							DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							Document doc = null;
							try
							{
								doc = dbuilder.parse(new ByteArrayInputStream(xml.getBytes()));
							}
							catch (SAXParseException e)
							{
								System.out.println("Parse Error!");
								return false;
							}
							// go on crawling
							
							List<URL> newURLs = new LinkedList<URL>();
							NodeList nodelist = doc.getElementsByTagName("a");
							NodeList nodelist2 = doc.getElementsByTagName("A");
							List<Element> anchors = new LinkedList<Element>();
							for(int i = 0 ; i < nodelist.getLength(); i++)
							{
								Node n = nodelist.item(i);
								if(n.getNodeType() == Node.ELEMENT_NODE)
								{
									anchors.add((Element)n);
								}
							}
							for(int i = 0 ; i < nodelist2.getLength(); i++)
							{
								Node n = nodelist2.item(i);
								if(n.getNodeType() == Node.ELEMENT_NODE)
								{
									anchors.add((Element)n);
								}
							}

							for(Element e : anchors)
							{
								String href = e.getAttribute("href");
								if (href.equals(""))	// Try Upper case
								{
									href = e.getAttribute("HREF");
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
							String [] handb = mhc.receive();
							if (handb[1].length() > MaximumSize)
								return false;
							// store and no more crawling for XML
						}

						
					}
				} catch (MyClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return false;
	}
}
