package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
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
	private Database dbModify;
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
		dbModify = myEnv.openDatabase(null, "ModifiedTime", dbConfig);
		dbXPath = myEnv.openDatabase(null, "XPath", dbConfig);
	}
	
	public boolean putPasswordInUser(String username, String password)
	{
		DatabaseEntry key = new DatabaseEntry(username.getBytes());
		DatabaseEntry data = new DatabaseEntry();
		
		if(dbUser.get(null, key, data, null) == OperationStatus.SUCCESS)	// EXIST
			return false;
		data.setData(password.getBytes());
		if(dbUser.put(null, key, data) == OperationStatus.SUCCESS)
			return true;
		else
			return false;	// should never reach;

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
		String pwd = new String(data.getData());
		if (pwd.equals(password))
			return true;
		else 
			return false;
	}
	public boolean deleteChannel(String username, String channelname)
	{
		DatabaseEntry key = new DatabaseEntry((channelname+"@"+username).getBytes());
		OperationStatus op = dbXPath.delete(null, key);
		if (op == OperationStatus.SUCCESS)
			return true;
		else
			return false;
	}
	
	public boolean addChannel(MyChannel mychannel, boolean update) throws IOException
	{
		DatabaseEntry key = new DatabaseEntry((mychannel.channelName+"@"+mychannel.userName).getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus op = dbXPath.get(null, key, data, null);
		if (!update)
		{
			if(op == OperationStatus.SUCCESS)
				return false;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(mychannel);
		data.setData(baos.toByteArray());
		op = dbXPath.put(null, key, data);
		if (op == OperationStatus.SUCCESS)
			return true;
		else
			return false;	// should never reach
	}
	
	
	public boolean addChannel(String username, String channelname, String xslurl, String [] xpaths, String [] urls, boolean append) throws IOException, ClassNotFoundException
	{
		DatabaseEntry key = new DatabaseEntry((channelname+"@"+username).getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus op = dbXPath.get(null, key, data, null);
		if(op == OperationStatus.SUCCESS)
			return false;
		if (!append)
		{
			MyChannel mychannel = new MyChannel(username, channelname, xslurl, xpaths, urls);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(mychannel);
			data.setData(baos.toByteArray());
			op = dbXPath.put(null, key, data);
			if (op == OperationStatus.SUCCESS)
				return true;
			else
				return false;
		}
		else
		{
			op = dbXPath.get(null, key, data, null);
			MyChannel mychannel = null;
			if (op == OperationStatus.SUCCESS)
			{
				ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());
				ObjectInputStream ois = new ObjectInputStream(bais);
				mychannel = ((MyChannel)ois.readObject());
				mychannel.appendXPaths(Arrays.asList(xpaths));
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(mychannel);
			data.setData(baos.toByteArray());
			op = dbXPath.put(null, key, data);
			if (op == OperationStatus.SUCCESS)
				return true;
			else
				return false;
		}
	}
	public MyChannel getChannel(String username, String channelname) throws IOException, ClassNotFoundException
	{
		DatabaseEntry key = new DatabaseEntry((channelname+"@"+username).getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus op = dbXPath.get(null, key, data, null);
		MyChannel mychannel = null;
		if (op == OperationStatus.SUCCESS)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
			mychannel = ((MyChannel)ois.readObject());
			return mychannel;
		}
		else
			return null;
	}
	
	public List<MyChannel> getAllChannels() throws IOException, ClassNotFoundException
	{
		List<MyChannel> channels = new ArrayList<MyChannel>();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		CursorConfig cc = new CursorConfig();
		Cursor cursor = dbXPath.openCursor(null, cc);
		OperationStatus os = cursor.getFirst(key, data, null);
		while(os == OperationStatus.SUCCESS)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
			channels.add(((MyChannel)ois.readObject()));
			os = cursor.getNext(key, data, null);
		}
		cursor.close();
		return channels;
	}
	
	public void putDocument(String type, String url, String raw)
	{
		DatabaseEntry key = new DatabaseEntry(url.getBytes());
		DatabaseEntry data = new DatabaseEntry(raw.getBytes());
		Long now = (new Date()).getTime();
		DatabaseEntry datatime = new DatabaseEntry((type+now.toString()).getBytes());
		// write to both document and modifiedtime
		dbDoc.put(null, key, data);
		dbModify.put(null, key, datatime);
		
	}
	
	public long getModified(String url)
	{
		DatabaseEntry key = new DatabaseEntry(url.getBytes());
		DatabaseEntry data = new DatabaseEntry();
		// write to both document and modifiedtime
		OperationStatus op = dbModify.get(null, key, data, null);
		if (op != OperationStatus.SUCCESS)
			return 0;
		else
		{
			String comb = new String(data.getData());
			comb = comb.substring(4);
			long time = Long.valueOf(comb);
			return time;
		}
	}
	public String getType (String url)
	{
		DatabaseEntry key = new DatabaseEntry(url.getBytes());
		DatabaseEntry data = new DatabaseEntry();
		// write to both document and modifiedtime
		OperationStatus op = dbModify.get(null, key, data, null);
		if (op != OperationStatus.SUCCESS)
			return null;
		else
		{
			String comb = new String(data.getData());
			comb = comb.substring(0, 4);
			return comb;
		}
	}
	
	public String getDocument(String url)
	{
		DatabaseEntry key = new DatabaseEntry(url.getBytes());
		DatabaseEntry data = new DatabaseEntry();
		// write to both document and modifiedtime
		OperationStatus op = dbDoc.get(null, key, data, null);
		if (op != OperationStatus.SUCCESS)
			return null;
		else
		{
			return new String(data.getData());
		}
	}
	
	public Cursor getDocCursor()	// Has to close after use
	{
		CursorConfig cc = new CursorConfig();
		return dbDoc.openCursor(null, cc);
	}
	
	public void closeDatabase()
	{
		dbUser.close();
		dbDoc.close();
		dbXPath.close();
		dbModify.close();
	}
	
	public void removeAllDatabase()	//Should be very careful when calling this function
	{
		myEnv.removeDatabase(null, "User");
		myEnv.removeDatabase(null, "Document");
		myEnv.removeDatabase(null, "XPath");
		myEnv.removeDatabase(null, "ModifiedTime");
	}
	
	public void closeEnvironment()
	{
		closeDatabase();
		myEnv.close();
	}
	
	
	
}
