package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

public class XPathCrawlerTest extends TestCase {

	public void testParseRobotsTxt() throws IOException
	{
		XPathCrawler xc = new XPathCrawler();
		String host = "crawltest.cis.upenn.edu";
		xc.parseRobotsTxt(host);
		assertEquals(xc.robotsMap.toString(), "{"+host+"=CrawlDelay: 5, Disallows: [/marie/private/, /foo/]}");
	}
}
