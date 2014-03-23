package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import edu.upenn.cis455.Client.MyClientException;
import edu.upenn.cis455.Client.MyHttpClient;
import edu.upenn.cis455.storage.*;

public class XPathCrawler {
	// Static arguments
	static String StoragePath;
	static String StartingPage;
	static int MaximumSize;
	static int MaximumDocNumber;

	HashMap<String, RobotRules> robotsMap;

	Queue<String> crawlingQueue;
	
	
	XPathCrawler()
	{
		robotsMap = new HashMap<String, RobotRules>();
		crawlingQueue = new LinkedList<String>();
	}

	// Parse robots.txt for a specific host
	void parseRobotsTxt(String host) throws IOException
	{
		MyHttpClient mhc = new MyHttpClient();

		mhc.connectTo(host+"/robots.txt");

		try {
			mhc.send("GET");
			String [] handb = mhc.receive();

			RobotRules robot = new RobotRules();
			// If not visited, create the robots map for this site
			// Else, we return
			if (!robotsMap.containsKey(host))
				this.robotsMap.put(host, robot);
			else
				return;


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

		} catch (MyClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void Usage()
	{
		System.out.println("Name: Hao Zhang");
		System.out.println("SEAS Login: zhanghao");
		System.out.println("Usage: java XPathCrawler StartingPage StorageDir MaxSize [MaxDocNum]");
	}


	// Main
	public static void main(String args[])
	{
		/* TODO: Implement crawler */
		if (args.length != 3 || args.length != 4)
		{
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

	}


}
