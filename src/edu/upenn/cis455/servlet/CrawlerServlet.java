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

	BufferedReader pipe = null;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getParameter("getinfo") == null)
		{
			PrintWriter pw = resp.getWriter();
			resp.setContentType("text/html");
			MyServletHelper.WriteHTMLHead(pw);
			MyServletHelper.WriteAJAXScript(pw);
			if (pipe == null)
			{
				pw.println("<form action=\"/xpathcrawler\" method=\"POST\">");
				pw.println("<label>Alternative Storage Path (if not assigned, will use store path in web.xml)</label>");
				pw.println("<div class=\"input-control text\"><input type=\"text\" name=\"spath\"/>");
				pw.println("<button class=\"btn-clear\"></button></div>");
				pw.println("<label>Starting Page</label>");
				pw.println("<div class=\"input-control text\">");
				pw.println("<input type=\"text\" name=\"spage\">");
				pw.println("<button class=\"btn-clear\"></button></div>");
				pw.println("<label>Maximum Size to Download (in MB)</label>");
				pw.println("<div class=\"input-control text\"><input type=\"text\" name=\"maxs\"/></div>");
				pw.println("<label>Maximum Pages to Crawl</label>");
				pw.println("<div class=\"input-control text\">");
				pw.println("<input type=\"text\" name=\"maxn\"/></div>");
				pw.println("<div class=\"form-actions\">");
				pw.println("<button class=\"primary large\" type=\"submit\" name=\"buttonclicked\" value=\"START\">Start</button>");
				pw.println("<button class=\"primary large\" type=\"submit\" name=\"buttonclicked\" value=\"RETURN\">Return</button>");
				pw.println("</div>");
				pw.println("</form> ");
				pw.println("<div id=\"outputdiv\" class=\"container bg-yellow\" style=\"width:100%; height:250px; over-flow:scroll;\"></div>");
				pw.println("<div id=\"infodiv\" class=\"container bg-lightBlue\"></div>");
			}
			else
			{
				pw.println("<p class=\"bg-yellow\"><strong>Logs from Crawler</strong>(Sorry, currently 'stop crawling' is unsupported...you can set maximum number of document to a reasonable value):</p>");
				pw.println("<div id=\"outputdiv\" class=\"container\" style=\"width:100%; over-flow:scroll;\"></div>");
				pw.println("<div id=\"infodiv\" class=\"tile quadro\"></div>");
			}
			
			MyServletHelper.WriteHTMLTail(pw);
		}
		else if (req.getParameter("getinfo").equals("a"))
		{
			if (pipe == null)
			{
				PrintWriter pw = resp.getWriter();
				resp.setContentType("text/plain");
				pw.print("blah");
				pw.flush();
				return;
			}
			else
			{
				//System.out.println("Hitted here");
				PrintWriter pw = resp.getWriter();
				resp.setContentType("text/plain");
				String brline = pipe.readLine();
				if (brline != null) pw.println(brline);
			}
		}
		else if (req.getParameter("getinfo").equals("d"))
		{
			pipe.close();
			pipe = null;
		}
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println(req.getParameterMap());
		PrintWriter pw = resp.getWriter();
		MyServletHelper.WriteHTMLHead(pw);
		String opt = req.getParameter("buttonclicked");
		if (opt.equals("START"))
		{
			String path = req.getParameter("spath");
			String page = req.getParameter("spage");
			String maxs = req.getParameter("maxs");
			String maxn = req.getParameter("maxn");
			
			if (path.equals("") || page.equals("") || maxs.equals("") || maxn.equals(""))
			{
				pw.println("<div class=\"panel\">");
				pw.println("<div class=\"panel-header bg-red fg-white\">Failed!</div>");
				pw.println("<div class=\"panel-content text-center\" style=\"display: block;\">");
				pw.println("<p style=\"font-size: large;\">"+"Please fill in all the parameters!"+"</p>");
				pw.println("<a href=\"/xpathcrawler\"><button class=\"button danger\" style=\"width: 28%;\">" +
						"<p style=\"font-size: x-large;margin-top: 7px;\">Back to Home</p></button></a>");
				pw.println("</div></div>");
				MyServletHelper.WriteHTMLTail(pw);
				return;
			}
			else
			{
				XPathCrawler xpc = new XPathCrawler(path, page, Integer.valueOf(maxs), Integer.valueOf(maxn), true);
				pipe = new BufferedReader(new PipedReader(xpc.getPipedWriter()));
				Thread t = new Thread(xpc);
				t.start();
				
				pw.println("<div class=\"panel\">");
				pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Suceeded!</div>");
				pw.println("<div class=\"panel-content text-center\" style=\"display: block;\">");
				pw.println("<p style=\"font-size: large;\">"+"Successfully started."+"</p>");
				pw.println("<a href=\"/xpathcrawler\"><button class=\"button info\" style=\"width: 28%;\">" +
						"<p style=\"font-size: x-large;margin-top: 7px;\">Back to Admin</p></button></a>");
				pw.println("</div></div>");
				MyServletHelper.WriteHTMLTail(pw);
				return;
			}
		}
		else if (opt.equals("RETURN"))
		{
			MyServletHelper.WriteLoginSuccessPanel(pw, "Logged out from Admin");
			MyServletHelper.WriteHTMLTail(pw);
		}
	}

}
