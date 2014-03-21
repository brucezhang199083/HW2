package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class BDBStorage {
	public Environment myEnv;
	public Database myDB;
	
	public BDBStorage()
	{
		EnvironmentConfig ec = new EnvironmentConfig();
		ec.setAllowCreate(true);
		myEnv  = new Environment(new File("/tmp/bdb"), ec);
		
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		
	}
}
