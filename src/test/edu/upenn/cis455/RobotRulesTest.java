package test.edu.upenn.cis455;

import java.net.MalformedURLException;
import java.net.URL;

import edu.upenn.cis455.crawler.RobotRules;
import junit.framework.TestCase;

public class RobotRulesTest extends TestCase {

	public void testIsCrawlable() throws InterruptedException {
		RobotRules rr = new RobotRules();
		rr.setDelay(2);
		rr.access();
		assertFalse(rr.isCrawlable());
		Thread.sleep(2014);
		assertTrue(rr.isCrawlable());
	}

	public void testIsDisallowed() throws MalformedURLException {
		RobotRules rr = new RobotRules();
		rr.addDisallow("/abc");
		rr.addDisallow("/foo/*/bar");
		rr.addDisallow("/myserver?");

		assertFalse(rr.isDisallowed("/def"));
		assertFalse(rr.isDisallowed("/foo"));
		assertFalse(rr.isDisallowed("/foo/b/b/b/bar"));
		
		assertTrue(rr.isDisallowed("/abcdef"));
		assertTrue(rr.isDisallowed("/foo/blah/bar"));
		assertTrue(rr.isDisallowed("/myserver?access=1"));
		assertTrue(rr.isDisallowed("/foo/blah/bar/barblah"));
	}

}
