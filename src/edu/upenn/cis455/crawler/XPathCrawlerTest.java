package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class XPathCrawlerTest extends TestCase {

	public void testParseRobotsTxt() throws IOException
	{
		XPathCrawler xc = new XPathCrawler();
		URL host = new URL("http://crawltest.cis.upenn.edu");
		String hstring = xc.getHostAndPort(host);
		xc.parseRobotsTxt(host);
		assertEquals(xc.robotsMap.toString(), "{"+hstring+"=CrawlDelay: 5, Disallows: [/marie/private/, /foo/]}");
	}
	
}
