package test.edu.upenn.cis455;

import com.sleepycat.je.DatabaseEntry;

import edu.upenn.cis455.storage.BDBStorage;
import junit.framework.TestCase;

public class BDBStorageTest extends TestCase {

	public void testBDBStorage() {
		BDBStorage bdbStorage = new BDBStorage();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		
		assertTrue(true);
		
	}

}
