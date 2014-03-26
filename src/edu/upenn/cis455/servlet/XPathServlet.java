package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.client.MyHttpClient;
import edu.upenn.cis455.storage.BDBStorage;
import edu.upenn.cis455.storage.MyChannel;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	int numberOfXPath = 1;
	HashMap<String, List<MyChannel>> currentChannelMap;
	BDBStorage storage;

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		String bdbpath = config.getInitParameter("BDBstore");
		if (bdbpath == null)
			bdbpath = config.getServletContext().getInitParameter("BDBstore");
		storage = new BDBStorage(bdbpath);
		// build current user channel map;
		currentChannelMap = new HashMap<String, List<MyChannel>>();
		try {
			List<MyChannel> currentChannels = storage.getAllChannels();
			for (MyChannel mc : currentChannels) {
				List<MyChannel> userChannel = null;
				if (currentChannelMap.containsKey(mc.getUserName())) {
					userChannel = currentChannelMap.get(mc.getUserName());
					userChannel.add(mc);
				} else {
					userChannel = new ArrayList<MyChannel>();
					userChannel.add(mc);
					currentChannelMap.put(mc.getUserName(), userChannel);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		storage.sync();
		storage.closeDatabase();
		storage.closeEnvironment();
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		resp.setContentType("text/html");
		MyServletHelper.WriteHTMLHead(pw);
		HttpSession session = req.getSession();
		// TODO: CHECK Parameter
		String path = req.getPathInfo();
		if (path != null) {
			if (path.equals("/loginresp")) // response for login
			{
				String un = req.getParameter("username");
				String pswd = req.getParameter("password");
				String opt = req.getParameter("buttonclicked");
				if (opt.equals("LOGOUT")) {
					// delete session
					String un2 = (String) session.getAttribute("currentuser");
					session.removeAttribute("currentuser");
					MyServletHelper.WriteLoginSuccessPanel(pw,
							"User Logged out! See you next time, " + un2 + "!");
					MyServletHelper.WriteHTMLTail(pw);
					return;
				}
				if ((un == null || pswd == null)
						|| (un.equals("") || pswd.equals(""))) {
					MyServletHelper.WriteLoginFailPanel(pw,
							"Username or Password can not be empty!");
					MyServletHelper.WriteHTMLTail(pw);
					return;
				} else {
					if (opt.equals("LOGIN")) // login clicked
					{
						try {
							if (storage.checkPasswordOfUser(un, pswd)) {
								session.setAttribute("currentuser", un);
								MyServletHelper.WriteLoginSuccessPanel(pw,
										"Successfully Logged in! Now will access as user :"
												+ un);
								MyServletHelper.WriteHTMLTail(pw);
								return;
							} else {
								MyServletHelper.WriteLoginFailPanel(pw,
										"Username or Password is incorrect!");
								MyServletHelper.WriteHTMLTail(pw);
								return;
							}

						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (opt.equals("SIGNUP")) // signup clicked
					{
						if (un.matches("[_A-Za-z0-9]+")) {
							if (storage.putPasswordInUser(un, pswd)) {
								session.setAttribute("currentuser", un);
								MyServletHelper.WriteLoginSuccessPanel(pw,
										"Successfully Signed up! Now will access as user :"
												+ un);
							} else {
								MyServletHelper.WriteLoginFailPanel(pw,
										"Username Already Exists!");
							}
							MyServletHelper.WriteHTMLTail(pw);
							return;
						} else {
							MyServletHelper
									.WriteLoginFailPanel(pw,
											"Username contains invalid character, should be in [_a-zA-Z0-9]");
							MyServletHelper.WriteHTMLTail(pw);
							return;
						}
					}
				}
			}
		}

		// and Session
		String currentuser = (String) session.getAttribute("currentuser");
		if (currentuser != null) {
			// Welcome
			pw.println("<div class=\"panel place-left\" style=\"width: 35%;\">");
			pw.println("<div class=\"panel-header bg-orange fg-white\">Welcome</div>");
			pw.println("<div class=\"panel-content\" style=\"display: block;\">");
			// Login message and button:
			pw.println("<p style=\"font-size: large;\">Welcome back, "
					+ currentuser + "!</p></br>");
			// create new channel button
			pw.println("<button id=\"createNewChannel\" class=\"button success\" style=\"width: 80%;\">");
			pw.println("<p style=\"font-size: x-large;margin-top: 7px;\">New Channel</p></button><br/><br/>");

			// Log out button
			pw.println("<a href=\"/xpath/loginresp?buttonclicked=LOGOUT\">");
			pw.println("<button class=\"button danger\" style=\"width: 80%;\" >"
					+ "<p style=\"font-size: x-large;margin-top: 7px;\">Log me out</p></button></a></div></div>");

			pw.println("<div class=\"panel place-right\" style=\"width: 61%;\">");
			pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Channel For "
					+ currentuser + "</div>");
			pw.println("<div class=\"panel-content\">");
			// if no channel
			List<MyChannel> userChannel = currentChannelMap.get(currentuser);
			if (userChannel == null) {
				pw.println("<p style=\"font-size:large;\">You don't have any channel for now, try create one!</p></div>");
			} else {
				// channel list accordion
				pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
				for (MyChannel mc : userChannel) {
					MyServletHelper.WriteChannelAccordionFrame(pw, mc);
					MyServletHelper.WriteSimpleSubmitScript(pw, "DisplayDelete");
					pw.println("<form id=\"DisplayDelete\" action=\"/xpath\" method=\"POST\">");
					pw.println("<input type=\"hidden\" name=\"targetchannel\" value=\""+mc.getChannelName()+"\"/>");
					pw.println("<div class=\"form-actions\">");
					pw.println("<button class=\"button info\" name=\"buttonclicked\" value=\"DISPLAY\" onclick=\"toTarget(_blank)\">Display</button>");
					pw.println("<button class=\"button danger\" name=\"buttonclicked\" value=\"DELETE\">Delete</button>");
					pw.println("</div></form>");
					pw.println("</div></div>");
				}
				pw.println("</div></div>");
			}
			MyServletHelper.WriteCreateButtonScript(pw);
			MyServletHelper.WriteHTMLTail(pw);
		} else {
			// Login:
			pw.println("<div class=\"panel place-left\" style=\"width: 35%;\">");
			pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Login</div>");
			pw.println("<div class=\"panel-content\" style=\"display: block;\">");
			// Login message and button:
			pw.println("<p style=\"font-size: large;\">Login or Sign up now to view, create and edit your own channels!</p></br>");
			pw.println("<button id=\"loginButton\" class=\"button primary\" style=\"width: 80%;\">"
					+ "<p style=\"font-size: x-large;margin-top: 7px;\">Login / SignUp</p></button><br/><br/>");
			// admin Login
			pw.println("<button id=\"adminButton\" class=\"button bg-cyan\" style=\"width: 80%;\">"
					+ "<p style=\"font-size: x-large;margin-top: 7px;\">Login as Admin</p></button></div></div>");
			// Channel list panel
			pw.println("<div class=\"panel place-right\" style=\"width: 61%;\">");
			pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Channel List</div>");
			pw.println("<div class=\"panel-content\">");
			List<MyChannel> clist = null;
			// channel list accordion
			try {
				clist = storage.getAllChannels();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (clist.size() == 0) {
				// No channel for now. Try to be the first one to create some
				// channels!
				pw.println("<p style=\"font-size: large;\">No channel available for now. Try to be the first one to create channels!</p>");
				pw.println("</div></div>");
			} else {
				// contents
				pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
				for (MyChannel mc : clist) {
					MyServletHelper.WriteChannelAccordionFrame(pw, mc);
					pw.println("</div></div>");
				}
				pw.println("</div></div>");
			}
			MyServletHelper.WriteLoginButtonScript(pw);
			MyServletHelper.WriteHTMLTail(pw);
		}
		// pw.println("<html><head><h2>");
		// pw.println("</h2></head><body>");
		// pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
		// pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
		// pw.println("<input size=80 name=urlofxml type=text maxlength=2000><br/>");
		// pw.println("<h3>Enter XPaths you want to evaluate: </h3>");
		// for (int i = 0; i < numberOfXPath; i++)
		// {
		// pw.println("<br/><input size=50 name='xpathnum"+String.valueOf(i)+"' type=text >");
		// }
		//
		// pw.println("<input style=\"overflow: visible; height: 0; width: 0;" +
		// " margin: 0; border: 0; padding: 0; display: block;\" " +
		// "type=\"submit\" name=\"done\" value=\"Submit\"/>");
		// pw.println("<input type=submit name='another' value='Add Another'>");
		// pw.println("<input type=submit name='done' value='Submit'></form>");
		// pw.println("</body></html>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// String another = req.getParameter("another");
		// String done = req.getParameter("done");

		PrintWriter pw = resp.getWriter();

		String opt = req.getParameter("buttonclicked");
		if (opt.equals("CREATE")) {
			resp.setContentType("text/html");
			MyServletHelper.WriteHTMLHead(pw);
			String channelName = req.getParameter("channelname");
			String XPaths = req.getParameter("linesxpath");
			String XsltURL = req.getParameter("xsltname");
			String currentuser = (String) req.getSession().getAttribute(
					"currentuser");
			if (channelName.equals("") || XPaths.equals("")
					|| XsltURL.equals("")) {
				MyServletHelper
						.WriteLoginFailPanel(pw,
								"Channel name, XSLT URL and XPaths must all not be empty!");
				MyServletHelper.WriteHTMLTail(pw);
				return;
			}
			String nl = System.getProperty("line.separator");
			String[] xpathArray = XPaths.split(nl);
			// iterate through all document
			try {
				List<String> matchingURLs = getMatchingList(xpathArray);
				MyChannel newc = new MyChannel(currentuser, channelName,
						XsltURL, xpathArray, matchingURLs.toArray(new String[0]));
				if (storage.addChannel(newc, false)) {
					// update local copies
					List<MyChannel> userChannel = null;
					if (currentChannelMap.containsKey(currentuser)) {
						userChannel = currentChannelMap.get(currentuser);
						userChannel.add(newc);
					} else {
						userChannel = new ArrayList<MyChannel>();
						userChannel.add(newc);
						currentChannelMap.put(currentuser, userChannel);
					}
					storage.sync();
					MyServletHelper.WriteLoginSuccessPanel(pw,
							"Channel successfully added!");
					MyServletHelper.WriteHTMLTail(pw);
					return;
				} else {
					MyServletHelper.WriteLoginFailPanel(pw,
							"Channel name already exists!");
					MyServletHelper.WriteHTMLTail(pw);
					return;
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyServletHelper.WriteHTMLTail(pw);

		}
		else if (opt.equals("DELETE"))
		{
			resp.setContentType("text/html");
			MyServletHelper.WriteHTMLHead(pw);
			
			String target=req.getParameter("targetchannel");
			if (target != null)
			{
				String cu = (String) req.getSession().getAttribute("currentuser");
				if (storage.deleteChannel(cu, target))
				{
					storage.sync();
					// sync with currentChannelMap
					List<MyChannel> listChannel = currentChannelMap.get(cu);
					MyChannel todelete = null;
					for(MyChannel mc : listChannel)
					{
						if (mc.getChannelName().equals(target))
						{
							todelete = mc;
							break;
						}
					}
					if (todelete != null)
						listChannel.remove(todelete);
					MyServletHelper.WriteLoginSuccessPanel(pw, "Successfully deleted channel : "+target);
					MyServletHelper.WriteHTMLTail(pw);
					return;
				}
				else
				{
					MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...storage fail");
					MyServletHelper.WriteHTMLTail(pw);
					return;
				}
			}
			else
			{
				MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...target null");
				MyServletHelper.WriteHTMLTail(pw);
				return;
			}
		}
		else if(opt.equals("DISPLAY"))
		{
			resp.setContentType("text/xml");
			// WRITE THE FORMATTED XML
			String target=req.getParameter("targetchannel");
			String cu = (String) req.getSession().getAttribute("currentuser");
			List<MyChannel> listChannel = currentChannelMap.get(cu);
			MyChannel todisplay = null;
			for(MyChannel mc : listChannel)
			{
				if (mc.getChannelName().equals(target))
				{
					todisplay = mc;
					break;
				}
			}
			List<String> urls = todisplay.getURLs();
			
			
		}
		// Switch between add another xpath and submit the form
		// if(another != null && another.equals("Add Another"))
		// {
		// pw.println("<html><head>");
		// pw.println("<h2>XPath Servlet 1.0 -- CIS 555 Homework 2 Milestone 1");
		// pw.println("</h2></head><body>");
		// pw.println("<form action='"+req.getContextPath()+"/xpath' method='POST'>");
		// pw.println("<h3>Enter URL of an HTML or XML (Maximum length is 2000 chars): </h3><br/>");
		// pw.println("<input size=80 name=urlofxml type=text maxlength=2000 value='"
		// +
		// req.getParameter("urlofxml")+"'><br/>");
		// pw.println("<h3>Enter XPaths you want to evaluate: </h3><br/>");
		// for (int i = 0; i < numberOfXPath; i++)
		// {
		// pw.println("<input size=50 name='xpathnum"+String.valueOf(i)+"' type='text' value='"
		// +
		// req.getParameter("xpathnum"+i)+"'><br/>");
		// }
		// pw.println("<input size=50 name='xpathnum"+String.valueOf(numberOfXPath)+"' type=text>");
		// numberOfXPath++;
		// // An invisible button that take action when pressing enter
		// pw.println("<input style=\"overflow: visible; height: 0; width: 0;" +
		// " margin: 0; border: 0; padding: 0; display: block;\" " +
		// "type=\"submit\" name=\"done\" value=\"Submit\"/>");
		// pw.println("<input type=submit name='another' value='Add Another'>");
		// pw.println("<input type=submit name='done' value='Submit'></form>");
		// pw.println("</body></html>");
		// }
		// else if(done != null && done.equals("Submit")) //Call XPathEngine to
		// evaluate the XPaths and output the results
		// {
		// pw.println("<html><head>");
		// pw.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"fancytable.css\"/>");
		// pw.println("<h2>Successfully Submitted!</h2>" +
		// "<h3>Below is the evaluation result.</h3></head>");
		//
		// // Retrieve the html/xml from url
		// MyHttpClient client = new MyHttpClient();
		// try{
		// client.connectTo(req.getParameter("urlofxml"));
		// }
		// catch (MalformedURLException e)
		// {
		// pw.println("<p><font color=#FF0000> ERROR: </font>Invalid URL</p>");
		// pw.println("<p><font color=#FF0000> DETAIL: </font>"+e.getMessage()+"</p>");
		// client.closeConnection();
		// }
		// catch (IOException e)
		// {
		// pw.println("<p><font color=#FF0000> ERROR: </font>Error when connect</p>");
		// StringWriter exceptionsw = new StringWriter();
		// e.printStackTrace(new PrintWriter(exceptionsw));
		// pw.println(exceptionsw.toString().replace("\r\n", "<br/>"));
		// client.closeConnection();
		// }
		// try {
		// client.send("GET");
		// String [] headerbody = client.receive();
		// BufferedReader brheader = new BufferedReader(new
		// StringReader(headerbody[0]));
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// DocumentBuilderFactory dbfactory =
		// DocumentBuilderFactory.newInstance();
		// DocumentBuilder db = dbfactory.newDocumentBuilder();
		// String header = null;
		// String type = null;
		// Document doc = null;
		// Tidy tidy = new Tidy();
		// tidy.setTidyMark(false);
		// tidy.setXHTML(true);
		// tidy.setXmlOut(true);
		// //tidy.setUpperCaseTags(true);
		// tidy.setForceOutput(true);
		// tidy.setShowWarnings(false);
		// tidy.setQuiet(true);
		// while((header = brheader.readLine()) != null)
		// {
		// if (header.trim().matches("(?i)Content-Type.*html.*;*.*"))
		// {
		// tidy.parse(new ByteArrayInputStream(headerbody[1].getBytes()), baos);
		// doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
		// type = "html";
		// break;
		// }
		// else if(header.trim().matches("(?i)Content-Type.*/xml.*;*.*"))
		// {
		// type = "xml";
		// doc = db.parse(new ByteArrayInputStream(headerbody[1].getBytes()));
		// break;
		// }
		// }
		// if(type == null)
		// {
		// pw.println("<p><font color=#FF0000> ERROR: </font>Neither html nor xml</p>");
		// client.closeConnection();
		// }
		// else // dom Ready to be evaluated
		// {
		// // Create an instance of XPathEngine
		// XPathEngineImpl xpe = new XPathEngineImpl();
		// // Debug printing
		// //System.out.println(xpe.transformDoc(doc));
		//
		//
		// ArrayList<String> xpathlist = new ArrayList<String>();
		// Enumeration<String> pname = req.getParameterNames();
		// // Get the list of xpaths
		// while (pname.hasMoreElements())
		// {
		// String name = pname.nextElement();
		// if(name.matches("xpathnum[0-9]+"))
		// {
		// xpathlist.add(req.getParameter(name));
		// }
		// }
		//
		// // Set the xpaths
		// xpe.setXPaths(xpathlist.toArray(new String[0]));
		// // Evaluate the xpaths (which will call isValid)
		// boolean [] resEvaluate = xpe.evaluate(doc);
		//
		// // Print a table to show the results
		// pw.println("<table>");
		// pw.println("<tr><th>XPath</th><th>IsValid Result</th><th>Evaluate Result</th></tr>");
		// for(int i = 0; i < numberOfXPath; i++)
		// {
		// pw.print("<tr><td>"+resp.encodeURL(xpathlist.get(i))+"</td>");
		// if (xpe.isValid(i))
		// pw.print("<td>True</td>");
		// else
		// pw.print("<td class=\"Falsetd\">False</td>");
		// if (resEvaluate[i])
		// pw.print("<td>True</td>");
		// else
		// pw.print("<td class=\"Falsetd\">False</td>");
		// }
		// pw.println("</table>");
		//
		// }
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// pw.println("<p><font color=#FF0000> ERROR: </font>sending and receiving</p>");
		// StringWriter exceptionsw = new StringWriter();
		// e.printStackTrace(new PrintWriter(exceptionsw));
		// pw.println(exceptionsw.toString().replace("\r\n", "<br/>"));
		// client.closeConnection();
		// }
		// client.closeConnection();
		//
		// // the number of xpath should be 1 again
		// numberOfXPath = 1;
		// pw.println("<form action='"+req.getContextPath()+"/xpath' method='GET'>");
		// pw.println("<input type=submit value='Start Over'></form></html>");
		// }
		//
		// }
	}

	// iterate through the docs to get matching
	private List<String> getMatchingList(String[] xpaths)
			throws ParserConfigurationException, UnsupportedEncodingException,
			SAXException, IOException {
		List<String> urls = new ArrayList<String>();
		XPathEngineImpl xpe = new XPathEngineImpl();
		xpe.setXPaths(xpaths);

		Cursor docCursor = storage.getDocCursor();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		OperationStatus op = docCursor.getFirst(key, data, null);
		while (op == OperationStatus.SUCCESS) {
			// Use dom parser
			String url = new String(key.getData());
			System.out.println("URL: "+url);
			if (url.endsWith(".xml"))
			{
				DocumentBuilder db = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document dom = db.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(data.getData()),"UTF-8")));
				boolean[] res = xpe.evaluate(dom);
				System.out.println(Arrays.toString(res));
				for (boolean b : res) {
					if (b) {
						urls.add(new String(key.getData()));
						break;
					}
				}
			}
			op = docCursor.getNext(key, data, null);
		}
		docCursor.close();
		return urls;
	}
	
	private void writeFormattedXML(List<String> urls, PrintWriter pw)
	{
		pw.println("<documentcollection>");
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'kk:mm:ss");
		for(String u : urls)
		{
			String raw = storage.getDocument(u);
			raw = raw.replaceAll("<\\?[^>]*\\?>", "");
			pw.println("<document ");
			pw.println("crawled=");
			//sdf.parse(new Date(Long.valueOf(new String())))
			pw.println(raw);
			pw.println("</document>");
		}
		pw.println("</documentcollection>");
	}
	/* TODO: Implement user interface for XPath engine here */

}
