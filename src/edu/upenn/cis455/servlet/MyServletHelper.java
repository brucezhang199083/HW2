package edu.upenn.cis455.servlet;

import java.io.PrintWriter;

import edu.upenn.cis455.storage.MyChannel;

public class MyServletHelper {

	public enum ListState
	{
		ALL,
		ALLLOGIN,
		CREATED,
		SUBSCRIBED,
	}
	public static void WriteHTMLHead(PrintWriter pw)
	{
		pw.println("<!DOCTYPE html>");
		pw.println("<html><head>");
		pw.println("<link rel=\"stylesheet\" href=\"/metro/css/metro-bootstrap.css\">");
		pw.println("<script src=\"/metro/js/jquery/jquery.min.js\"></script>");
		pw.println("<script src=\"/metro/js/jquery/jquery.widget.min.js\"></script>");
		pw.println("<script src=\"/metro/js/metro.min.js\"></script></head>");
		pw.println("<script src=\"/metro/js/metro/metro-accordion.js\"></script></head>");
		pw.println("<script src=\"/metro/js/metro/metro-listview.js\"></script></head>");
		pw.println("<body class=\"metro\" >");
		pw.println("<div class=\"container\" style=\"width:80%;\">");
		pw.println("<div class=\"panel\">");
		pw.println("<div class=\"panel-header bg-blue fg-white\"><h1 class=\"text-center\">Channel Subscription Servlet</h1></div>");
		pw.println("<div class=\"panel-content\" style=\"display: block;\">");
		pw.println("<h3 class=\"text-center\">CIS 455/555 Homework 2 Milestone 2 by Hao Zhang (zhanghao)</h3></div></div><br/>");
	}
	public static void WriteHTMLTail(PrintWriter pw)
	{
		pw.println("</div></body></html>");
	}
	
	public static void WriteLoginFailPanel(PrintWriter pw, String errormsg)
	{
		pw.println("<div class=\"panel\">");
		pw.println("<div class=\"panel-header bg-red fg-white\">Failed!</div>");
		pw.println("<div class=\"panel-content text-center\" style=\"display: block;\">");
		pw.println("<p style=\"font-size: large;\">"+errormsg+"</p>");
		pw.println("<a href=\"/xpath\"><button class=\"button danger\" style=\"width: 28%;\">" +
				"<p style=\"font-size: x-large;margin-top: 7px;\">Back to Home</p></button>");
		pw.println("</div></div>");
	}
	public static void WriteLoginSuccessPanel(PrintWriter pw, String msg)
	{
		pw.println("<div class=\"panel\">");
		pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Suceeded!</div>");
		pw.println("<div class=\"panel-content text-center\" style=\"display: block;\">");
		pw.println("<p style=\"font-size: large;\">"+msg+"</p>");
		pw.println("<a href=\"/xpath\"><button class=\"button info\" style=\"width: 28%;\">" +
				"<p style=\"font-size: x-large;margin-top: 7px;\">Back to Home</p></button>");
		pw.println("</div></div>");
	}
	
	public static void WriteChannelAccordionFrame(PrintWriter pw, MyChannel mc, ListState ls)
	{
		if (ls == ListState.ALL)
		{
			pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading\">" +
					   mc.getChannelName()+" @User: "+mc.getUserName()+"</a>");
		}
		else if (ls == ListState.ALLLOGIN)
		{
			pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading bg-lightBlue fg-wight\">" +
					   mc.getChannelName()+" @User: "+mc.getUserName()+"</a>");
		}
		else if (ls == ListState.CREATED)
		{
			pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading bg-green fg-white\">" +
				   mc.getChannelName()+"</a>");
		}
		else if (ls == ListState.SUBSCRIBED)
		{
			pw.println("<div class=\"accordion-frame\" ><a href=\"\" class=\"heading bg-orange fg-white\">" +
					   mc.getChannelName()+" @User: "+mc.getUserName()+"</a>");
		}
		pw.println("<div class=\"content\">");
		// xpath list
		if (ls == ListState.ALL)
		{
			pw.println("<form id=\"Display\" action=\"/xpath\" method=\"POST\" target\"_blank\">");
			pw.println("<input type=\"hidden\" name=\"targetchannel\" value=\""+mc.getChannelName()+"\"/>");
			pw.println("<input type=\"hidden\" name=\"createuser\" value=\""+mc.getUserName()+"\"/>");
			pw.println("<div class=\"form-actions\">");
			pw.println("<button class=\"button info\" name=\"buttonclicked\" value=\"DISPLAY\">Display</button>");
			pw.println("</div></form>");
		}
		else if (ls == ListState.ALLLOGIN)
		{
			pw.println("<form id=\"Display\" action=\"/xpath\" method=\"POST\">");
			pw.println("<input type=\"hidden\" name=\"targetchannel\" value=\""+mc.getChannelName()+"\"/>");
			pw.println("<input type=\"hidden\" name=\"createuser\" value=\""+mc.getUserName()+"\"/>");
			pw.println("<div class=\"form-actions\">");
			pw.println("<button class=\"button info\" name=\"buttonclicked\" value=\"DISPLAY\">Display</button>");
			pw.println("<button class=\"button success\" name=\"buttonclicked\" value=\"SUBSCRIBE\">Subscribe</button>");
			pw.println("</div></form>");
		}
		else if (ls == ListState.CREATED)
		{
			pw.println("<form id=\"DisplayDelete\" action=\"/xpath\" method=\"POST\">");
			pw.println("<input type=\"hidden\" name=\"targetchannel\" value=\""+mc.getChannelName()+"\"/>");
			pw.println("<input type=\"hidden\" name=\"createuser\" value=\""+mc.getUserName()+"\"/>");
			pw.println("<div class=\"form-actions\">");
			pw.println("<button class=\"button info\" name=\"buttonclicked\" value=\"DISPLAY\" >Display</button>");
			pw.println("<button class=\"button danger\" name=\"buttonclicked\" value=\"DELETE\">Delete</button>");
			pw.println("</div></form>");
		}
		else if (ls == ListState.SUBSCRIBED)
		{
			pw.println("<form id=\"Sub\" action=\"/xpath\" method=\"POST\">");
			pw.println("<input type=\"hidden\" name=\"targetchannel\" value=\""+mc.getChannelName()+"\"/>");
			pw.println("<input type=\"hidden\" name=\"createuser\" value=\""+mc.getUserName()+"\"/>");
			pw.println("<div class=\"form-actions\">");
			pw.println("<button class=\"button info\" name=\"buttonclicked\" value=\"DISPLAY\" >Display</button>");
			pw.println("<button class=\"button warning\" name=\"buttonclicked\" value=\"UNSUBSCRIBE\" >Unsubscribe</button>");
			pw.println("</div></form>");
		}
		pw.println("<table class=\"bordered text-left\">");
		pw.println("<thead><tr class=\"bg-lightTeal\"><th>Rules (XPaths):</th></tr></thead>");
		pw.println("<tbody>");
		for(String s : mc.getXPaths())
		{
			pw.println("<tr><td>"+s+"</td></tr>");
		}
		pw.println("</tbody>");
		pw.println("<thead><tr class=\"bg-lightTeal\"><th>Matching URLs:</th></tr></thead>");
		pw.println("<tbody>");
		for(String s : mc.getURLs())
		{
			pw.println("<tr><td>"+"<a href=\""+s+"\">"+s+"</a>"+"</td></tr>");
		}
		pw.println("</tbody>");
		pw.println("<thead><tr class=\"bg-lightTeal\"><th>XSLT Stylesheet:</th></tr></thead>");
		pw.println("<tbody>");
		pw.println("<tr><td>"+"<a href=\""+mc.getXslURL()+"\">"+mc.getXslURL()+"</a>"+"</td></tr>");
		pw.println("</tbody>");
		pw.println("</table>");

		pw.println("</div></div>");
		// content is not end
	}
	public static void WriteLoginButtonScript(PrintWriter pw)
	{
		pw.println("<script>");
		pw.println(" $(\"#loginButton\").on('click', function(){");
		pw.println(" $.Dialog({");
		pw.println(" overlay: true,");
		pw.println(" shadow: true,");
		pw.println(" flat: true,");
		pw.println(" title: 'User Login',");
		pw.println(" content: '',");
		pw.println(" padding: 10,");
		pw.println(" onShow: function(_dialog){");
		pw.println(" var content = '<form action=\"/xpath/loginresp\" method=\"GET\">' +");
		pw.println(" '<label>Username</label>' +");
		pw.println(" '<div class=\"input-control text\"><input type=\"text\" name=\"username\">' +");
		pw.println(" '<button class=\"btn-clear\"></button></div> ' +");
		pw.println(" '<label>Password</label>' +");
		pw.println(" '<div class=\"input-control password\">' +");
		pw.println(" '<input type=\"password\" name=\"password\">' +");
		pw.println(" '<button class=\"btn-reveal\"></button></div> ' +");
		pw.println(" '<div class=\"form-actions\">' +");
		pw.println(" '<button class=\"button primary\" name=\"buttonclicked\" value=\"LOGIN\">Login</button> '+");
		pw.println(" '<button class=\"button success\" name=\"buttonclicked\" value=\"SIGNUP\">SignUp</button> '+");
		pw.println(" '<button class=\"button\" type=\"button\" onclick=\"$.Dialog.close()\">Cancel</button> '+");
		pw.println(" '</div>'+");
		pw.println(" '</form>';");
		pw.println(" $.Dialog.title(\"User Login\");");
		pw.println(" $.Dialog.content(content);");
		pw.println(" $.Metro.initInputs();");
		pw.println(" }");
		pw.println(" });");
		pw.println(" });</script>");
	}
	
	public static void WriteCreateButtonScript(PrintWriter pw)
	{
		pw.println("<script>");
		pw.println(" $(\"#createNewChannel\").on('click', function(){");
		pw.println(" $.Dialog({");
		pw.println(" overlay: true,");
		pw.println(" shadow: true,");
		pw.println(" flat: true,");
		pw.println(" title: 'User Login',");
		pw.println(" content: '',");
		pw.println(" padding: 10,");
		pw.println(" onShow: function(_dialog){");
		pw.println(" var content = '<form action=\"/xpath\" method=\"POST\">' +");
		pw.println(" '<label>Channel Name</label>' +");
		pw.println(" '<div class=\"input-control text size5\"><input type=\"text\" name=\"channelname\">' +");
		pw.println(" '<button class=\"btn-clear\"></button></div> ' +");
		pw.println(" '<label>XSLT Stylesheet URL</label>' +");
		pw.println(" '<div class=\"input-control text size5\">' +");
		pw.println(" '<input type=\"text\" name=\"xsltname\">' +");
		pw.println(" '<button class=\"btn-clear\"></button></div> ' +");
		pw.println(" '<label>XPaths (one on each line)</label>' +");
		pw.println(" '<div class=\"input-control textarea size5\">' +");
		pw.println(" '<textarea name=\"linesxpath\"></textarea></div>' +");
		pw.println(" '<div class=\"form-actions\">' +");
		pw.println(" '<button class=\"button primary\" name=\"buttonclicked\" value=\"CREATE\">Create</button> '+");
		pw.println(" '<button class=\"button\" type=\"button\" onclick=\"$.Dialog.close()\">Cancel</button> '+");
		pw.println(" '</div>'+");
		pw.println(" '</form>';");
		pw.println(" $.Dialog.title(\"New Channel\");");
		pw.println(" $.Dialog.content(content);");
		pw.println(" $.Metro.initInputs();");
		pw.println(" }");
		pw.println(" });");
		pw.println(" });</script>");
	}
	public static void WriteSimpleSubmitScript(PrintWriter pw, String formname)
	{
		pw.println("<script>"+
	    "function toTarget(target){"+
	    "document.getElementById('"+formname+"').target = target;"+
	    "document.getElementById('"+formname+"').submit();}</script>");
	}
}
