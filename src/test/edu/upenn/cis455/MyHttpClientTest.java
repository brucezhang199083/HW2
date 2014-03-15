package test.edu.upenn.cis455;

import java.io.IOException;

import edu.upenn.cis455.servlet.MyHttpClient;
import junit.framework.TestCase;

public class MyHttpClientTest extends TestCase {

	MyHttpClient mhc;
	public MyHttpClientTest()
	{
		mhc = new MyHttpClient();
	}
	public void testReceive() {
		try {
			mhc.connectTo("http://www.cis.upenn.edu/~cis455/");
			mhc.send("GET");
			Thread.sleep(100);
			mhc.receive();
			mhc.closeConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(true);
	}
}
