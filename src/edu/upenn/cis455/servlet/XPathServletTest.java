package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mockito.Mockito;

import edu.upenn.cis455.servlet.XPathServlet;

public class XPathServletTest extends TestCase {


	public void testDoGetHttpServletRequestHttpServletResponse() {
		
		try
		{
			HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
			HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			Mockito.when(mockResponse.getWriter()).thenReturn(pw);
			XPathServlet xps = new XPathServlet();
			xps.doGet(mockRequest,mockResponse);
			//System.out.println(sw.toString());
			assertTrue(sw.toString().contains("Enter URL of an HTML or XML"));
		}
		catch (Exception e)
		{
			
		}
	}

	public void testDoPostHttpServletRequestHttpServletResponse() {

		try
		{
			HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
			HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			Mockito.when(mockResponse.getWriter()).thenReturn(pw);
			Mockito.when(mockRequest.getParameter("urlofxml")).thenReturn("http://localhost:8080/examples");
			Mockito.when(mockRequest.getParameter("xpathnum0")).thenReturn("/html");
			Mockito.when(mockRequest.getParameter("another")).thenReturn(null);
			Mockito.when(mockRequest.getParameter("done")).thenReturn("Submit");
			
			XPathServlet xps = new XPathServlet();
			xps.doPost(mockRequest,mockResponse);
			//System.out.println(sw.toString());
			assertTrue(sw.toString().contains("Successfully Submitted!"));
		}
		catch (Exception e)
		{
			
		}
	}

}
