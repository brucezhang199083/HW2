package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;

public class BDBStorage {
	private Environment myEnv;
	
	private Database dbUser;
	private Database dbDoc;
	private Database dbDocMod;
	private Database dbXPath;
	public BDBStorage(String path)
	{
		EnvironmentConfig ec = new EnvironmentConfig();
		ec.setAllowCreate(true);
		File storepath = new File(path);
		if(!storepath.isDirectory())
		{
			storepath.mkdir();
		}
		myEnv  = new Environment(storepath, ec);
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbUser = myEnv.openDatabase(null, "User", dbConfig);
		dbDoc = myEnv.openDatabase(null, "Document", dbConfig);
		dbDocMod = myEnv.openDatabase(null, "DocumentModified", dbConfig);
		dbXPath = myEnv.openDatabase(null, "XPath", dbConfig);
	}
	
	public boolean putPasswordInUser(String username, String password) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(password);
		
		DatabaseEntry key = new DatabaseEntry(username.getBytes());
		DatabaseEntry data = new DatabaseEntry(baos.toByteArray());
		
		if(dbUser.put(null, key, data) == OperationStatus.SUCCESS)
			return true;
		return false;
	}
	
	public void sync()
	{
		myEnv.sync();
	}
	
	public boolean checkPasswordOfUser(String username, String password) throws IOException, ClassNotFoundException
	{
		DatabaseEntry key = new DatabaseEntry(username.getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus op = dbUser.get(null, key, data, null);
		if(op == OperationStatus.NOTFOUND)
			return false;
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.getData()));
		String pwd = (String)ois.readObject();
		if (pwd.equals(password))
			return true;
		else 
			return false;
	}
	public void closeDatabase()
	{
		dbUser.close();
		dbDoc.close();
		dbDocMod.close();
		dbXPath.close();
	}
	
	public void removeAllDatabase()	//Should be very careful when calling this function
	{
		myEnv.removeDatabase(null, "User");
		myEnv.removeDatabase(null, "Document");
		myEnv.removeDatabase(null, "DocumentModified");
		myEnv.removeDatabase(null, "XPath");
	}
	
}
