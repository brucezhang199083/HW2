package test.edu.upenn.cis455;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.sleepycat.je.DatabaseEntry;

import edu.upenn.cis455.storage.BDBStorage;
import edu.upenn.cis455.storage.MyChannel;
import junit.framework.TestCase;

public class BDBStorageTest extends TestCase {

	
	public void testPutGetPassword() throws IOException, ClassNotFoundException
	{
		BDBStorage bdbStorage = new BDBStorage("/tmp/test");
		bdbStorage.putPasswordInUser("bruce", "12345678");
		bdbStorage.putPasswordInUser("bruce", "87654321");	//exist will fail
		bdbStorage.sync();

		assertFalse(bdbStorage.checkPasswordOfUser("bruce", "4422442244"));
		assertFalse(bdbStorage.checkPasswordOfUser("steanna", "4422442244"));
		assertFalse(bdbStorage.checkPasswordOfUser("bruce", "87654321"));
		assertTrue(bdbStorage.checkPasswordOfUser("bruce", "12345678"));
		bdbStorage.closeDatabase();
		bdbStorage.removeAllDatabase();
		bdbStorage.sync();
		bdbStorage.closeEnvironment();
	}
	
	public void testAddPutChannel() throws IOException, ClassNotFoundException
	{
		BDBStorage bdbStorage = new BDBStorage("/tmp/test");
		List<String> xp = new ArrayList<String>();
		List<String> urls = new ArrayList<String>();
		xp.add("/abc/def");
		xp.add("/org/org");
		urls.add("foo.bar");
		bdbStorage.addChannel("zhanghao", "channel1", "http://foo.bar.com", xp.toArray(new String[0]), urls.toArray(new String[0]), false);
		bdbStorage.addChannel("bruce", "channel2", "http://foo.bar.com", xp.toArray(new String[0]), urls.toArray(new String[0]), false);
		bdbStorage.sync();
		MyChannel mc = bdbStorage.getChannel("zhanghao", "channel1");
		assertEquals(mc.getChannelName(), "channel1");
		assertEquals(mc.getUserName(), "zhanghao");
		assertEquals(mc.getXPaths().get(0), "/abc/def");
		assertEquals(mc.getURLs().get(0), "foo.bar");
		
		List<MyChannel> chs = bdbStorage.getAllChannels();
		
		assertEquals(chs.get(0).getChannelName(), "channel1");
		assertEquals(chs.get(0).getUserName(), "zhanghao");
		
		assertEquals(chs.get(1).getChannelName(), "channel2");
		assertEquals(chs.get(1).getUserName(), "bruce");
		
		bdbStorage.closeDatabase();
		bdbStorage.removeAllDatabase();
		bdbStorage.sync();
		bdbStorage.closeEnvironment();
	}
	
	public void testGetPutDocumentAndModifiedTime() throws IOException, ClassNotFoundException, InterruptedException
	{
		BDBStorage bdbStorage = new BDBStorage("/tmp/test");
		long time = (new Date()).getTime();
		Thread.sleep(1000);
		bdbStorage.putDocument("html", "www.abc.com", "<html></html>");
		
		long modified = bdbStorage.getModified("www.abc.com");
		assertTrue(modified - 1000 >= time);
		
		String rawDoc = bdbStorage.getDocument("www.abc.com");
		assertEquals(rawDoc, "<html></html>");
		bdbStorage.closeDatabase();
		bdbStorage.removeAllDatabase();
		bdbStorage.sync();
		bdbStorage.closeEnvironment();
	}
		
}
