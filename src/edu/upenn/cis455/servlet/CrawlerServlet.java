package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.crawler.XPathCrawler;

@SuppressWarnings("serial")
public class CrawlerServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = resp.getWriter();
		String storagePath = getInitParameter("BDBstore");
		if (storagePath == null)
			storagePath = getServletContext().getInitParameter("BDBstore");
		
		String testPath = "/tmp/def";
		String initpage = "http://crawltest.cis.upenn.edu";
		int maxs = 1;
		int maxn = 100;
		
		XPathCrawler crawler = new XPathCrawler(testPath, initpage, maxs, maxn, true);
		PipedWriter iwriter = crawler.getPipedWriter();
		Thread crawlerThread = new Thread(crawler);
		PipedReader ireader = new PipedReader(iwriter);
		BufferedReader br = new BufferedReader(ireader);
		
		crawlerThread.start();
		String brline = null;
		while ((brline = br.readLine()) != null)
			pw.println(brline);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = resp.getWriter();
		String storagePath = getInitParameter("BDBstore");
		if (storagePath == null)
			storagePath = getServletContext().getInitParameter("BDBstore");
		
		String testPath = "/tmp/def";
		String initpage = "http://crawltest.cis.upenn.edu";
		int maxs = 1;
		int maxn = 100;
		
		XPathCrawler crawler = new XPathCrawler(testPath, initpage, maxs, maxn, true);
		PipedWriter iwriter = crawler.getPipedWriter();
		Thread crawlerThread = new Thread(crawler);
		PipedReader ireader = new PipedReader(iwriter);
		BufferedReader br = new BufferedReader(ireader);
		
		crawlerThread.start();
		String brline = null;
		while ((brline = br.readLine()) != null)
			pw.println(brline);
	}

}
