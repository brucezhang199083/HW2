package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

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
		
		String action = req.getParameter("another");
		
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		//Switch between add another xpath and submit the form
		if(action != null && action.equals("Add Another"))
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
			pw.println("<input type=submit name='another' value='Submit'></form>");
			pw.println("</body></html>");
		}
		else if(action != null && action.equals("Submit"))	//Call XPathEngine to evaluate the XPaths and output the results
		{
			pw.println("<html><head><h2>SUBMITTED!</h2></head>");
			pw.println("Totalxpath:"+String.valueOf(numberOfXPath)+"<br/>");
			Enumeration<String> pname = req.getParameterNames();
			while (pname.hasMoreElements())
			{
				String name = pname.nextElement();
				pw.println(name+":"+req.getParameter(name)+"<br/>");
			}
			numberOfXPath = 1;
			pw.println("<form action='"+req.getContextPath()+"/xpath' method='GET'>");
			pw.println("<input type=submit value='Start Over'></form></html>");
		}	
		
	}

  /* TODO: Implement user interface for XPath engine here */
	
}









