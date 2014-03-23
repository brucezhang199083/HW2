package test.edu.upenn.cis455;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import edu.upenn.cis455.Client.MyHttpClient;

import junit.framework.TestCase;

public class MyHttpClientTest extends TestCase {

	MyHttpClient mhc;
	public MyHttpClientTest()
	{
		mhc = new MyHttpClient();
	}
	public void testReceive() {
		try {
			mhc.connectTo("http://www.cis.upenn.edu/~cis455/demo/example1.xml");
			mhc.send("GET");
			Thread.sleep(100);
			String [] a = mhc.receive();
			assertTrue(a.length > 0);
			mhc.closeConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
