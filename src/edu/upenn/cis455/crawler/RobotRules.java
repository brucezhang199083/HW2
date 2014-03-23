package edu.upenn.cis455.crawler;

import java.util.HashSet;

public class RobotRules {
	int CrawlDelay;
	HashSet<String> Disallows;
	
	public RobotRules()
	{
		// default Crawdelay is 1 sec
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
	
	public String toString()
	{
		return "CrawlDelay: "+String.valueOf(CrawlDelay)+", "+"Disallows: "+Disallows.toString();
	}
}
