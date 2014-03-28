package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathCrawler implements Runnable{
	// Static arguments
	String StoragePath;
	String StartingPage;
	int MaximumSize;
	int MaximumDocNumber;
	PipedWriter interWriter;

	HashMap<String, RobotRules> robotsMap;
	Queue<URL> crawlingQueue;
	Set<String> crawledSet;

	int totalByte = 0;
	int htmlnum = 0;
	int xmlnum = 0;
	int channelnum = 0;
	
	public XPathCrawler(String storep, String startp, int maxs, int maxn, boolean piped)
	{
		if (piped)
			interWriter = new PipedWriter();
		else
			interWriter = null;
		StoragePath = storep;
		StartingPage = startp;
		MaximumSize = maxs;
		MaximumDocNumber = maxn;
		robotsMap = new HashMap<String, RobotRules>();
		crawlingQueue = new LinkedList<URL>();
		crawledSet = new HashSet<String>();
	}
	
	public PipedWriter getPipedWriter()
	{
		return interWriter;
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
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		// Inter process pipe
		PrintWriter pw = null;
		if (interWriter == null)
			pw = new PrintWriter(System.out);
		else
			pw = new PrintWriter(interWriter);

		BDBStorage bdb = new BDBStorage(StoragePath);
		try {
			URL seeds = null;
			if (!StartingPage.startsWith("http://"))
				seeds = new URL("http://"+StartingPage);
			else
				seeds = new URL(StartingPage);
			crawlingQueue.add(seeds);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			pw.println("URL FORMAT ERROR : "+StartingPage);
			pw.println("Detail:");
			e.printStackTrace(pw);
			return;
		}

			int count = 0;
			Boolean res = null;
			do
			{
				try {
					res = crawlNext(bdb, pw);
				} catch (Exception e)
				{
					e.printStackTrace(pw);
					continue;
				}
				//System.out.println("Crawingqueue:"+instance.crawlingQueue);
				if (res)
					count++;
				if (MaximumDocNumber > 0 && count >= MaximumDocNumber)
					break;
				else
					continue;
			}
			while(res != null);
		
			pw.println("I have to be outputed");
			pw.println(crawledSet.size()+" Document Visited");
			pw.println("Domain visited : "+robotsMap.keySet());
			pw.println("Total byte downloaded : "+totalByte);
			pw.println("HTML Scanned : "+htmlnum);
			pw.println("XML Retrieved : "+xmlnum);
			pw.println("Total channel matching processed : "+channelnum);
			pw.println("See the log for other details.");
			pw.println("crawling finished");
			bdb.closeDatabase();
			bdb.closeEnvironment();
			pw.close();
		
	}
	// Main
	public static void main(String args[]) throws InterruptedException, ClassNotFoundException
	{
		String storagePath = null;
		String startingPage = null;
		int maximumSize;
		int maximumDocNumber;
		/* TODO: Implement crawler */
		if (args.length != 3 && args.length != 4)
		{
			System.out.println("Error arg num");
			Usage();
			return;
		}
		startingPage = args[0];
		storagePath = args[1];

		try {
			maximumSize = Integer.parseInt(args[2]);
			if (args.length == 3)	//No MaxDocNum specified
			{
				maximumDocNumber = -1;
			}
			else
			{
				maximumDocNumber = Integer.parseInt(args[3]);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Usage();
			return;
		}
		XPathCrawler instance = new XPathCrawler(storagePath, startingPage, maximumSize, maximumDocNumber, false);
		instance.run();		
	}

	Boolean crawlNext(BDBStorage storage, PrintWriter pw) throws IOException, InterruptedException, ClassNotFoundException, MyClientException
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
					mhc.send("HEAD");
					if (interWriter == null) { 
						//System.out.println("Head sended");
					}
					else
					{
						//pw.println("Head sended");
					}
					String [] headerarray = null;
					try
					{
						headerarray = mhc.receive();
					}
					catch (MyClientException e)
					{
						e.printStackTrace(pw);
						return false;
					}
					//System.out.println(headerarray[0]);
					mhc.closeConnection();
					HashMap<String, List<String> > headerMap = mhc.parseHeader(headerarray[0]);
					
					//System.out.println(headerMap);
					if (headerMap.containsKey("content-length"))
					{
						int contentLength = Integer.parseInt(headerMap.get("content-length").get(0));
						if (contentLength > MaximumSize*1048576)
						{
							if (interWriter == null) { 
								System.out.println(nextURL+" : File size exceeds limit, discarding...");
							}
							else
							{
								pw.println(nextURL+" : File size exceeds limit, discarding...");
							}
							return false;
						}
					}
					if (headerMap.containsKey("content-type"))
					{
						String type = headerMap.get("content-type").get(0).split(";")[0].trim();					
						if (type.matches("text/html"))
						{
							String [] handb = null;
							boolean notmodified = false;
							if (headerMap.containsKey("last-modified"))
							{
								String date = headerMap.get("last-modified").get(0);
								SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
								try {
									long mdf = sdf.parse(date).getTime();
									long crawled = storage.getModified(nextURL.toString());
									if(mdf < crawled)
									{
										if (interWriter == null) 
										{ 
											System.out.println(nextURL+" : HTML Not Modified, retrieving from DB...");
										}
										else
										{
											pw.println(nextURL+" : HTML Not Modified, retrieving from DB...");
										}
										notmodified = true;
										String fromdb = storage.getDocument(nextURL.toString());
										handb = new String[2];
										handb[1] = fromdb;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace(pw);
									return false;
								}
							}
							if (!notmodified)
							{
							// retrieve the body and push links\
								mhc.connectTo(nextURL);
								rule.access();
								mhc.send("GET");
								if (interWriter == null) { 
									System.out.println(nextURL+" : HTML Downloading...");
								}
								else
								{
									pw.println(nextURL+" : HTML Downloading...");
								}
								try
								{
									handb = mhc.receive();
								}
								catch (MyClientException e)
								{
									if (interWriter == null) { 
										System.out.println("Error when receiving, discarding...");
									}
									else
									{
										pw.println("Error when receiving, discarding...");
									}
									return false;
								}
								totalByte += handb[1].length();
								mhc.closeConnection();
								if (handb[1].length() > MaximumSize*1048576)
								{
									if (interWriter == null) 
									{ 
										System.out.println(nextURL+" : File size exceeds limit, discarding...");
									}
									else
									{
										pw.println(nextURL+" : File size exceeds limit, discarding...");
									}
									return false;
								}
								else
								{
									// store raw html
									storage.putDocument("html", nextURL.toString(), handb[1]);
								}
							}
							// parse Document using Jsoup
							htmlnum++;
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
									if (href.startsWith("#"))
										continue;
									//pw.println(href);
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
										{
											int pos = start.lastIndexOf('/');
											String last = start.substring(pos+1);
											if (last.contains("."))
											{
												fullurl = start.substring(0, pos+1)+href;
											}
											else
											{
												fullurl = start+"/"+href;
											}
										}
									}
									newURLs.add(new URL(fullurl));
								}
							}
						//	System.out.println(newURLs);
							crawlingQueue.addAll(newURLs);
						}
						else if(type.matches(".*/xml"))
						{
							String [] handb = null;
							boolean notmodified = false;
							if (headerMap.containsKey("last-modified"))
							{
								String date = headerMap.get("last-modified").get(0);
								SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
								try {
									long mdf = sdf.parse(date).getTime();
									long crawled = storage.getModified(nextURL.toString());
									if(mdf < crawled)
									{
										if (interWriter == null)
										{ 
											System.out.println(nextURL+" : XML Not Modified, retrieving from DB...");
										}
										else
										{
											pw.println(nextURL+" : XML Not Modified, retrieving from DB...");
										}
										notmodified = true;
										String fromdb = storage.getDocument(nextURL.toString());
										handb = new String[2];
										handb[1] = fromdb;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace(pw);
									return false;
								}
							}
							if (!notmodified)
							{
								mhc.connectTo(nextURL);
								mhc.send("GET");
								rule.access();
								if (interWriter == null)
								{ 
									System.out.println(nextURL+" : XML Downloading...");
								}
								else
								{
									pw.println(nextURL+" : XML Downloading...");
								}
								handb = mhc.receive();
								if (handb[1].length() > MaximumSize*1048576)
								{
									if (interWriter == null) { 
										System.out.println(nextURL+" : File size exceeds limit, discarding...");
									}
									else
									{
										pw.println(nextURL+" : File size exceeds limit, discarding...");
									}
									return false;
								}
								totalByte += handb[1].length();
								storage.putDocument("xmls", nextURL.toString(), handb[1]);
							}
							xmlnum++;
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
								if (interWriter == null) { 
									System.out.println("Malformed XML");
								}
								else
								{
									pw.println("Malformed XML");
								}
								e.printStackTrace(pw);
								return false;
							}
							List<MyChannel> cl = storage.getAllChannels();
							XPathEngineImpl xpe = new XPathEngineImpl();
							for(MyChannel mc : cl)
							{
								xpe.setXPaths(mc.getXPaths().toArray(new String[0]));
								boolean [] res = xpe.evaluate(doc);
								for (boolean b : res)
								{
									if(b)
									{
										channelnum++;
										pw.println("Matched Channel : "+mc.getChannelName());
										mc.addURL(nextURL.toString());
										storage.addChannel(mc, true);
										break;
									}
								}
							}
						}
						else
						{
							if (interWriter == null) { 
								System.out.println(nextURL+" : Content type mismatch, discarding...");
							}
							else
							{
								pw.println(nextURL+" : Content type mismatch, discarding...");
							}
							return false;
						}
						storage.sync();
					}	// content-type
					else
					{
						if (interWriter == null) { 
							System.out.println(nextURL+" : Content type not found, discarding...");
						}
						else
						{
							pw.println(nextURL+" : Content type not found, discarding...");
						}
						return false;
					}
				}
		}
		return true;
	}
	
	class NullWriter extends Writer
	{
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			// DO NOTHING!
		}

		@Override
		public void flush() throws IOException {
			// DO NOTHING!			
		}

		@Override
		public void close() throws IOException {
			// DO NOTHING!
		}
	}
}
