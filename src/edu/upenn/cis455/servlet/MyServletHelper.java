package edu.upenn.cis455.servlet;

import java.io.PrintWriter;

public class MyServletHelper {

	public static void WriteHTMLHead(PrintWriter pw)
	{
		pw.println("<!DOCTYPE html>");
		pw.println("<html><head>");
		pw.println("<link rel=\"stylesheet\" href=\"/metro/css/metro-bootstrap.css\">");
		pw.println("<script src=\"/metro/js/jquery/jquery.min.js\"></script>");
		pw.println("<script src=\"/metro/js/jquery/jquery.widget.min.js\"></script>");
		pw.println("<script src=\"/metro/js/metro.min.js\"></script></head>");
		pw.println("<script src=\"/metro/js/metro-accordion.js\"></script></head>");
	}
	public static void WriteLoginButtonScript(PrintWriter pw)
	{
		pw.println("<script>");
		pw.println(" $(\"#loginButton\").on('click', function(){");
		pw.println(" $.Dialog({");
		pw.println(" overlay: true,");
		pw.println(" shadow: true,");
		pw.println(" flat: true,");
		pw.println(" title: 'Flat window',");
		pw.println(" content: '',");
		pw.println(" padding: 10,");
		pw.println(" onShow: function(_dialog){");
		pw.println(" var content = '<form action=\"/xpath\" method=\"GET\">' +");
		pw.println(" '<label>User Login</label>' +");
		pw.println(" '<div class=\"input-control text\"><input type=\"text\" name=\"login\">' +");
		pw.println(" '<button class=\"btn-clear\"></button></div> ' +");
		pw.println(" '<label>Password</label>' +");
		pw.println(" '<div class=\"input-control password\">' +");
		pw.println(" '<input type=\"password\" name=\"password\">' +");
		pw.println(" '<button class=\"btn-reveal\"></button></div> ' +");
		pw.println(" '<div class=\"form-actions\">' +");
		pw.println(" '<button class=\"button primary\" name=\"buttonclicked\" value=\"LOGIN\">Login</button> '+");
		pw.println(" '<button class=\"button\" type=\"button\" onclick=\"$.Dialog.close()\">Cancel</button> '+");
		pw.println(" '</div>'+");
		pw.println(" '</form>';");
		pw.println(" $.Dialog.title(\"User login\");");
		pw.println(" $.Dialog.content(content);");
		pw.println(" $.Metro.initInputs();");
		pw.println(" }");
		pw.println(" });");
		pw.println(" });</script>");
	}
}
