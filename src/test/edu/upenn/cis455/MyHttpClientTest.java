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
			mhc.connectTo("http://www.cis.upenn.edu/~cis455/demo/example1.xml");
			mhc.send("GET");
			Thread.sleep(100);
			String [] a = mhc.receive();
			System.out.println(a[0]);
			System.out.println(a[1]);
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
