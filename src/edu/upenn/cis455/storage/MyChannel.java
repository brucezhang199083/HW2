package edu.upenn.cis455.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MyChannel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4237843817299518215L;
	
	String userName;
	String channelName;
	String xslURL;
	ArrayList<String> XPaths = new ArrayList<String>();
	ArrayList<String> URLs = new ArrayList<String>();
	public MyChannel(String userName, String channelName, String xslURL,
			String[] xpaths, String [] urls) {
		this.userName = userName;
		this.channelName = channelName;
		this.xslURL = xslURL;
		XPaths = new ArrayList<String>();
		URLs =  new ArrayList<String>();
		for(String s : xpaths)
			XPaths.add(s);
		for(String s : urls)
			URLs.add(s);
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getXslURL() {
		return xslURL;
	}
	public void setXslURL(String xslURL) {
		this.xslURL = xslURL;
	}
	public List<String> getXPaths() {
		return XPaths;
	}
	public void setXPaths(String [] xPaths) {
		XPaths =  new ArrayList<String>();
		for(String s : xPaths)
			XPaths.add(s);
	}
	
	public List<String> getURLs()
	{
		return URLs;
	}
	
	public void setURLs(String [] urls) 
	{
		URLs =  new ArrayList<String>();
		for(String s : urls)
			URLs.add(s);
	}
	
	public void addURL(String url)
	{
		URLs.add(url);
	}
	
	public void appendXPaths(Collection<String> xPaths)
	{
		XPaths.addAll(xPaths);
	}
	
	
}
