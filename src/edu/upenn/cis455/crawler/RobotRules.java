package edu.upenn.cis455.crawler;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

public class RobotRules implements Serializable
{
	private static final long serialVersionUID = -4927450621832951076L;
	int CrawlDelay;
	long LastAccess;
	HashSet<String> Disallows;
	
	public RobotRules()
	{
		// default Crawdelay is 1 sec
		LastAccess = 0;
		CrawlDelay = 1;
		Disallows = new HashSet<String>();
	}

	public void clear() {
		// TODO Auto-generated method stub
		Disallows.clear();
	}

	public void addDisallow(String rule) {
		// TODO Auto-generated method stub
		Disallows.add(rule);
	}

	public void setDelay(int delay) {
		// TODO Auto-generated method stub
		CrawlDelay = delay;
	}
	
	public HashSet<String> getDisallows()
	{
		return Disallows;
	}
	public long getLastAccess()
	{
		return LastAccess;
	}
	public int getDelay()
	{
		return CrawlDelay;
	}
	public boolean isCrawlable()
	{
		long now = (new Date()).getTime();
		return (now - LastAccess > CrawlDelay*1000 );
	}
	public void access()
	{
		LastAccess = (new Date()).getTime();
	}
	
	public String toString()
	{
		return "CrawlDelay: "+String.valueOf(CrawlDelay)+", "+"Disallows: "+Disallows.toString();
	}

}
