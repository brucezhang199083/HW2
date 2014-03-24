package test.edu.upenn.cis455;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import com.sleepycat.je.DatabaseEntry;

import edu.upenn.cis455.storage.BDBStorage;
import junit.framework.TestCase;

public class BDBStorageTest extends TestCase {

	public void testPutGetPassword() throws IOException, ClassNotFoundException
	{
		BDBStorage bdbStorage = new BDBStorage("/tmp/abc");
		bdbStorage.putPasswordInUser("bruce", "12345678");
		bdbStorage.sync();

		assertFalse(bdbStorage.checkPasswordOfUser("bruce", "4422442244"));
		assertFalse(bdbStorage.checkPasswordOfUser("steanna", "4422442244"));
		assertTrue(bdbStorage.checkPasswordOfUser("bruce", "12345678"));
		
		bdbStorage.close();

	}
	

}
