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
	List<String> XPaths = new ArrayList<String>();
	List<String> URLs = new ArrayList<String>();
	public MyChannel(String userName, String channelName, String xslURL,
			String[] xpaths, String [] urls) {
		this.userName = userName;
		this.channelName = channelName;
		this.xslURL = xslURL;
		XPaths = Arrays.asList(xpaths);
		URLs = Arrays.asList(urls);
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
		XPaths = Arrays.asList(xPaths);
	}
	
	public List<String> getURLs()
	{
		return URLs;
	}
	
	public void setURLs(String [] urls) 
	{
		URLs = Arrays.asList(urls);
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
