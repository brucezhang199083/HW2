package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		
		pw.println("<html><head><h2>");
		pw.println("XPath Servlet 1.0 -- CIS 555 Homework 2 Milestone 1");
		pw.println("</h2></head><body>");
		pw.println("<form action='"+req.getContextPath()+"' method='POST'>");
		pw.println("Enter URL of an HTML or XML: <br/>");
		pw.println("<input size=20 name=urlofxml type=text><br/>");
		pw.println("");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

  /* TODO: Implement user interface for XPath engine here */
	
}









