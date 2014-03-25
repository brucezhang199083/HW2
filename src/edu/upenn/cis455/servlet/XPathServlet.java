package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.client.MyHttpClient;
import edu.upenn.cis455.storage.BDBStorage;
import edu.upenn.cis455.storage.MyChannel;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	int numberOfXPath = 1;
	ArrayList<MyChannel> currentChannels;
	BDBStorage storage;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
//		String bdbpath = config.getInitParameter("BDBstore");
//		if (bdbpath == null)
//			bdbpath = config.getServletContext().getInitParameter("BDBstore");
//		storage = new BDBStorage(bdbpath);
//		
	}
		
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
//		storage.closeDatabase();
//		storage.closeEnvironment();
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		MyServletHelper.WriteHTMLHead(pw);
		pw.println("<body class=\"metro\" >");
		pw.println("<h1 align=\"center\">Channel Subscription Servlet -- CIS 555 Homework 2 Milestone 2</h1><br/>");
		pw.println("<div class=\"container\" align=\"center\" style=\"width: 80%;\">");
		// Login or Welcome
		// TODO: CHECK SESSION
		// Login:
		pw.println("<div class=\"panel place-left\" style=\"width: 35%;\">");
		pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Login</div>");
		pw.println("<div class=\"panel-content\" style=\"display: block;\">");
		// Login message and button:
		pw.println("<p style=\"font-size: large;\">Login or Sign up now to view, create and edit your own channels!</p></br>");
		pw.println("<button id=\"loginButton\" class=\"button primary\" >" +
				"<p style=\"font-size: x-large;margin-top: 7px;margin-left: 7px;margin-right:7px;\">Login / SignUp</p></button></div></div>");
		// Channel list panel
		pw.println("<div class=\"panel place-right\" style=\"width: 61%;\">");
		pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Channel List</div>");
		pw.println("<div class=\"panel-content\">");
		// channel list accordion
		// No channel for now. Try to be the first one to create some channels!
		pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
		pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading\">HEADING</a>");
		pw.println("<div class=\"content\" ><h3>contents</h3></div></div>");
		pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading\">HEADING2</a>");
		pw.println("<div class=\"content\" ><h3>contents2</h3></div></div>");
		pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading\">HEADING3</a>");
		pw.println("<div class=\"content\" ><h3>contents3</h3></div></div></div></div>");

		MyServletHelper.WriteLoginButtonScript(pw);
		pw.println(	"</body></html>");
		
//		pw.println("<html><head><h2>");
//		pw.println("</h2></head><body>");
//		pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
//		pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
//		pw.println("<input size=80 name=urlofxml type=text maxlength=2000><br/>");
//		pw.println("<h3>Enter XPaths you want to evaluate: </h3>");
//		for (int i = 0; i < numberOfXPath; i++)
//		{
//			pw.println("<br/><input size=50 name='xpathnum"+String.valueOf(i)+"' type=text >");
//		}
//		
//		pw.println("<input style=\"overflow: visible; height: 0; width: 0;" +
//				" margin: 0; border: 0; padding: 0; display: block;\" " +
//				"type=\"submit\" name=\"done\" value=\"Submit\"/>");
//		pw.println("<input type=submit name='another' value='Add Another'>");
//		pw.println("<input type=submit name='done' value='Submit'></form>");
//		pw.println("</body></html>");
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
			pw.println("<html><head>");
			pw.println("<h2>XPath Servlet 1.0 -- CIS 555 Homework 2 Milestone 1");
			pw.println("</h2></head><body>");
			pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
			pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
			pw.println("<input size=80 name=urlofxml type=text maxlength=2000 value='" +
					   req.getParameter("urlofxml")+"'><br/>");
			pw.println("<h3>Enter XPaths you want to evaluate: </h3><br/>");
			for (int i = 0; i < numberOfXPath; i++)
			{
				pw.println("<input size=50 name='xpathnum"+String.valueOf(i)+"' type='text' value='" +
						   req.getParameter("xpathnum"+i)+"'><br/>");
			}
			pw.println("<input size=50 name='xpathnum"+String.valueOf(numberOfXPath)+"' type=text>");
			numberOfXPath++;
			// An invisible button that take action when pressing enter
			pw.println("<input style=\"overflow: visible; height: 0; width: 0;" +
						" margin: 0; border: 0; padding: 0; display: block;\" " +
						"type=\"submit\" name=\"done\" value=\"Submit\"/>");
			pw.println("<input type=submit name='another' value='Add Another'>");
			pw.println("<input type=submit name='done' value='Submit'></form>");
			pw.println("</body></html>");
		}
		else if(done != null && done.equals("Submit"))	//Call XPathEngine to evaluate the XPaths and output the results
		{
			pw.println("<html><head>");
			pw.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"fancytable.css\"/>");
			pw.println("<h2>Successfully Submitted!</h2>" +
					   "<h3>Below is the evaluation result.</h3></head>");
			
			// Retrieve the html/xml from url
			MyHttpClient client = new MyHttpClient();
			try{
				client.connectTo(req.getParameter("urlofxml"));
			} 
			catch (MalformedURLException e)
			{
				pw.println("<p><font color=#FF0000> ERROR: </font>Invalid URL</p>");
				pw.println("<p><font color=#FF0000> DETAIL: </font>"+e.getMessage()+"</p>");
				client.closeConnection();
			}
			catch (IOException e)
			{
				pw.println("<p><font color=#FF0000> ERROR: </font>Error when connect</p>");
				StringWriter exceptionsw = new StringWriter();
				e.printStackTrace(new PrintWriter(exceptionsw));
				pw.println(exceptionsw.toString().replace("\r\n", "<br/>"));
				client.closeConnection();
			}
			try {
				client.send("GET");
				String [] headerbody = client.receive();
				BufferedReader brheader = new BufferedReader(new StringReader(headerbody[0]));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbfactory.newDocumentBuilder();
				String header = null;
				String type = null;
				Document doc = null;
				Tidy tidy = new Tidy();
			    tidy.setTidyMark(false);
			    tidy.setXHTML(true);
				tidy.setXmlOut(true);
				//tidy.setUpperCaseTags(true);
				tidy.setForceOutput(true);
				tidy.setShowWarnings(false);
				tidy.setQuiet(true);
				while((header = brheader.readLine()) != null)
				{
					if (header.trim().matches("(?i)Content-Type.*html.*;*.*"))
					{
						tidy.parse(new ByteArrayInputStream(headerbody[1].getBytes()), baos);
						doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
						type = "html";
						break;
					}
					else if(header.trim().matches("(?i)Content-Type.*/xml.*;*.*"))
					{
						type = "xml";
						doc = db.parse(new ByteArrayInputStream(headerbody[1].getBytes()));
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
					// Create an instance of XPathEngine
					XPathEngineImpl xpe = new XPathEngineImpl();
					// Debug printing
					//System.out.println(xpe.transformDoc(doc));
					
					
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

					// Set the xpaths
					xpe.setXPaths(xpathlist.toArray(new String[0]));
					// Evaluate the xpaths (which will call isValid)
					boolean [] resEvaluate = xpe.evaluate(doc);
					
					// Print a table to show the results
					pw.println("<table>");
					pw.println("<tr><th>XPath</th><th>IsValid Result</th><th>Evaluate Result</th></tr>");
					for(int i = 0; i < numberOfXPath; i++)
					{
						pw.print("<tr><td>"+resp.encodeURL(xpathlist.get(i))+"</td>");
						if (xpe.isValid(i))
							pw.print("<td>True</td>");
						else
							pw.print("<td class=\"Falsetd\">False</td>");
						if (resEvaluate[i])
							pw.print("<td>True</td>");
						else
							pw.print("<td class=\"Falsetd\">False</td>");
					}
					pw.println("</table>");
					
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
			
			// the number of xpath should be 1 again
			numberOfXPath = 1;		
			pw.println("<form action='"+req.getContextPath()+"/xpath' method='GET'>");
			pw.println("<input type=submit value='Start Over'></form></html>");
		}	
		
	}

  /* TODO: Implement user interface for XPath engine here */
	
}









