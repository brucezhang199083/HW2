package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class BDBStorage {
	public Environment myEnv;
	
	public BDBStorage(String path)
	{
		EnvironmentConfig ec = new EnvironmentConfig();
		File storepath = new File(path);
		if(!storepath.isDirectory())
		{
			storepath.mkdir();
		}
		myEnv  = new Environment(storepath, ec);
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		
	}
}
