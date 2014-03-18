package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.tidy.PPrint;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	int numberOfXPath = 1;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		
		pw.println("<html><head><h2>");
		pw.println("XPath Servlet 1.0 -- CIS 555 Homework 2 Milestone 1");
		pw.println("</h2></head><body>");
		pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
		pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
		pw.println("<input size=80 name=urlofxml type=text maxlength=2000><br/>");
		pw.println("<h3>Enter XPaths you want to evaluate: </h3>");
		for (int i = 0; i < numberOfXPath; i++)
		{
			pw.println("<br/><input size=50 name='xpathnum"+String.valueOf(i)+"' type=text >");
		}
		pw.println("<input type=submit name='another' value='Add Another'><br/>");
		pw.println("<input type=submit name='done' value='Submit'></form>");
		pw.println("</body></html>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String another = req.getParameter("another");
		String done = req.getParameter("done");
		
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		//Switch between add another xpath and submit the form
		if(another != null && another.equals("Add Another"))
		{
			pw.println("<html><head><h2>");
			pw.println("XPath Servlet 1.0 -- CIS 555 Homework 2 Milestone 1");
			pw.println("</h2></head><body>");
			pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
			pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
			pw.println("<input size=80 name=urlofxml type=text maxlength=2000 value=" +
					   req.getParameter("urlofxml")+"><br/>");
			pw.println("<h3>Enter XPaths you want to evaluate: </h3><br/>");
			for (int i = 0; i < numberOfXPath; i++)
			{
				pw.println("<input size=50 name='xpathnum"+String.valueOf(i)+"' type=text value=" +
						   req.getParameter("xpathnum"+i)+"><br/>");
			}
			pw.println("<input size=50 name='xpathnum"+String.valueOf(numberOfXPath)+"' type=text>");
			numberOfXPath++;
			pw.println("<input type=submit name='another' value='Add Another'><br/>");
			pw.println("<input type=submit name='done' value='Submit'></form>");
			pw.println("</body></html>");
		}
		else if(done != null && done.equals("Submit"))	//Call XPathEngine to evaluate the XPaths and output the results
		{
			pw.println("<html><head><h2>SUBMITTED!</h2></head>");
			pw.println("Totalxpath:"+String.valueOf(numberOfXPath)+"<br/>");
			
			// Retrieve the html/xml from url
			MyHttpClient client = new MyHttpClient();
			try{
				client.connectTo(req.getParameter("urlofxml"));
			} catch (MalformedURLException e)
			{
				pw.println("<p><font color=#FF0000> ERROR: </font>Invalid URL</p>");
				pw.println("<p><font color=#FF0000> DETAIL: </font>"+e.getMessage()+"</p>");
				client.closeConnection();
				return;
			}
			try {
				client.send("GET");
				String [] handb = client.receive();
				BufferedReader brheader = new BufferedReader(new StringReader(handb[0]));
				StringReader sr = new StringReader(handb[1]);
				StringWriter sw = new StringWriter();
				String header = null;
				String type = null;
				Document doc = null;
				while((header = brheader.readLine()) != null)
				{
					
					if (header.trim().matches("(?i)Content-Type.*html.*;*.*"))
					{
						Tidy tidy = new Tidy();
					    tidy.setTidyMark(false);
					    tidy.setXHTML(true);
						tidy.setXmlOut(true);
						tidy.setLowerLiterals(true);
						//tidy.getConfiguration().printConfigOptions(sw, true);
						tidy.parse(new ByteArrayInputStream(handb[1].getBytes()), sw);
						String tidied = sw.toString();
						DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbfactory.newDocumentBuilder();
						doc = db.parse(new ByteArrayInputStream(tidied.getBytes()));
						type = "html";
						break;
					}
					else if(header.trim().matches("(?i)Content-Type.*/xml.*;*.*"))
					{
						type = "xml";
						DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbfactory.newDocumentBuilder();
						doc = db.parse(new ByteArrayInputStream(handb[1].getBytes()));
						break;
					}
				}
				if(type == null)
				{
					pw.println("<p><font color=#FF0000> ERROR: </font>Neither html nor xml</p>");
					client.closeConnection();
				}
				else	// dom Ready to be evaluated
				{
					ArrayList<String> xpathlist = new ArrayList<String>();
					Enumeration<String> pname = req.getParameterNames();
					// Get the list of xpaths
					while (pname.hasMoreElements())
					{
						String name = pname.nextElement();
						if(name.matches("xpathnum[0-9]+"))
						{
							xpathlist.add(req.getParameter(name));
						}
					}
					// Create an instance of XPathEngine
					XPathEngineImpl xpe = new XPathEngineImpl();
					xpe.setXPaths(xpathlist.toArray(new String[0]));
					for(int i = 0; i < numberOfXPath; i++)
					{
						pw.println("XPath: \""+xpathlist.get(i)+"\" is "+(xpe.isValid(i) ? "valid." : "invalid."));
						pw.println("<br/>");
					}
					pw.println("<p>"+doc.getDocumentElement().getTextContent()+"</p>");
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				pw.println("<p><font color=#FF0000> ERROR: </font>sending and receiving</p>");
				StringWriter exceptionsw = new StringWriter();
				e.printStackTrace(new PrintWriter(exceptionsw));
				pw.println(exceptionsw.toString().replace("\r\n", "<br/>"));
				client.closeConnection();
			}
					
			client.closeConnection();
			numberOfXPath = 1;		
			pw.println("<form action='"+req.getContextPath()+"/xpath' method='GET'>");
			pw.println("<input type=submit value='Start Over'></form></html>");
		}	
		
	}

  /* TODO: Implement user interface for XPath engine here */
	
}









