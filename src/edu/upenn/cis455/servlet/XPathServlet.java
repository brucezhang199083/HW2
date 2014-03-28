package edu.upenn.cis455.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.servlet.MyServletHelper.ListState;
import edu.upenn.cis455.storage.BDBStorage;
import edu.upenn.cis455.storage.MyChannel;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	int numberOfXPath = 1;
	HashMap<String, Set<MyChannel> > currentChannelMap;
	BDBStorage storage;
	String storagePath;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		storagePath = config.getInitParameter("BDBstore");
		if (storagePath == null)
			storagePath = config.getServletContext().getInitParameter("BDBstore");
		storage = new BDBStorage(storagePath);
		// build current user channel map;
		currentChannelMap = new HashMap<String, Set<MyChannel>>();
		try {
			List<MyChannel> currentChannels = storage.getAllChannels();
			for (MyChannel mc : currentChannels) {
				Set<MyChannel> userChannel = null;
				if (currentChannelMap.containsKey(mc.getUserName())) {
					userChannel = currentChannelMap.get(mc.getUserName());
					userChannel.add(mc);
				} else {
					userChannel = new HashSet<MyChannel>();
					userChannel.add(mc);
					currentChannelMap.put(mc.getUserName(), userChannel);
				}
			}
			storage.closeEnvironment();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		storage = new BDBStorage(storagePath);
		
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

					storage.closeEnvironment();
					return;
				}
				if ((un == null || pswd == null)
						|| (un.equals("") || pswd.equals(""))) {
					MyServletHelper.WriteLoginFailPanel(pw,
							"Username or Password can not be empty!");
					MyServletHelper.WriteHTMLTail(pw);

					storage.closeEnvironment();
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

								storage.closeEnvironment();
								return;
							} else {
								MyServletHelper.WriteLoginFailPanel(pw,
										"Username or Password is incorrect!");
								MyServletHelper.WriteHTMLTail(pw);

								storage.closeEnvironment();
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

							storage.closeEnvironment();
							return;
						} else {
							MyServletHelper
									.WriteLoginFailPanel(pw,
											"Username contains invalid character, should be in [_a-zA-Z0-9]");
							MyServletHelper.WriteHTMLTail(pw);

							storage.closeEnvironment();
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

			// XXX: Channel Tabs
			pw.println("<div class=\"tab-control place-right\" style=\"width: 62%;\" data-role=\"tab-control\">");
			pw.println("<ul class=\"tabs\">");
			pw.println("<li class=\"active\"><a href=\"#pagea\">All</a></li>");
			pw.println("<li><a href=\"#pagec\">Created</a></li>");
			pw.println("<li><a href=\"#pages\">Subscribed</a></li>");
			pw.println("</ul>");
			
			// tab page for all
			pw.println("<div class=\"frames\">");
			pw.println("<div class=\"frame\" id=\"pagea\">");
			// XXX: Channel list when user is
			pw.println("<div class=\"panel\">");
			pw.println("<div class=\"panel-header bg-blue fg-white\">Channel Available</div>");
			pw.println("<div class=\"panel-content\">");
			// All channel first
			List<MyChannel> clist = null;
			try {
				clist = storage.getAllChannels();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (clist.size() == 0) {
				// No channel for now. Try to be the first one to create some
				// channels!
				pw.println("<p style=\"font-size: large;\">No channel available for now. Try to be the first one to create channels!</p>");
				pw.println("</div></div>");
			} 
			else 
			{
				// contents
				pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
				for (MyChannel mc : clist) {
					MyServletHelper.WriteChannelAccordionFrame(pw, mc, ListState.ALLLOGIN);
					
				}
				pw.println("</div></div></div>");
			}			
			pw.println("</div>");	// PAGE ALL OVER
			
			pw.println("<div class=\"frame\" id=\"pagec\">");	// PAGE CREATE START
			
			pw.println("<div class=\"panel\">");
			pw.println("<div class=\"panel-header bg-darkGreen fg-white\">Channel User Created</div>");
			pw.println("<div class=\"panel-content\">");
			// if no channel
			Set<MyChannel> userChannel = currentChannelMap.get(currentuser);
			if (userChannel == null) {
				pw.println("<p style=\"font-size:large;\">You don't have any channel for now, try create one!</p></div></div>");
			} 
			else 
			{
				// channel list accordion
				pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
				for (MyChannel mc : userChannel) {
					MyServletHelper.WriteChannelAccordionFrame(pw, mc, ListState.CREATED);
				}
				pw.println("</div></div></div>");
			}
			
			pw.println("</div>"); // PAGE CREATE OVER!
			
			// TODO: PAGE SUBSCRIBE!
			pw.println("<div class=\"frame\" id=\"pages\">");	// PAGE CREATE START
			
			pw.println("<div class=\"panel\">");
			pw.println("<div class=\"panel-header bg-darkOrange fg-white\">Channel Subscribed</div>");
			pw.println("<div class=\"panel-content\">");
			Set<MyChannel> subChannels = null;
			try {
				subChannels = storage.getSubscribedChannels(currentuser);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (subChannels.size() == 0) {
				pw.println("<p style=\"font-size:large;\">You didn't subscribe any channel, try subscribe one!</p></div></div>");
			} 
			else 
			{
				// channel list accordion
				pw.println("<div class=\"accordion with-marker\" data-role=\"accordion\">");
				for (MyChannel mc : subChannels) {
					MyServletHelper.WriteChannelAccordionFrame(pw, mc, ListState.SUBSCRIBED);
				}
				pw.println("</div></div></div>");
			}
			pw.println("</div>"); // PAGE SUBSCRIBE OVER!
			pw.println("</div></div>");
			MyServletHelper.WriteCreateButtonScript(pw);
			MyServletHelper.WriteHTMLTail(pw);

			storage.closeEnvironment();
			return;
		}
		else
		{	// not loggin in!!!
			// Login:
			pw.println("<div class=\"panel place-left\" style=\"width: 35%;\">");
			pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Login</div>");
			pw.println("<div class=\"panel-content\" style=\"display: block;\">");
			// Login message and button:
			pw.println("<p style=\"font-size: large;\">Login or Sign up now to view, create, delete your own channels, as well as subscribe other's channels!</p></br>");
			pw.println("<button id=\"loginButton\" class=\"button primary\" style=\"width: 80%;\">"
					+ "<p style=\"font-size: x-large;margin-top: 7px;\">Login / SignUp</p></button><br/><br/>");
			// admin Login
			pw.println("<p style=\"font-size: large;\">Or, enter Admin's password and login as Admin to run crawler! And I will NOT tell you that the default password is 'admin'.</p></br>");
			pw.println("<form action=\"/xpath\" method=\"POST\">");
			pw.println("<label>Admin Password:</label>");
			pw.println("<div class=\"input-control password\"><input type=\"password\" name=\"adminpass\"/>");
			pw.println("<button class=\"btn-reveal\"></button></div>");
			pw.println("<div class=\"form-actions\">");
			pw.println("<button id=\"adminButton\" class=\"button bg-cyan\" style=\"width: 80%;\" name=\"buttonclicked\" " +
					" value=\"ADMINLOGIN\">"
					+ "<p style=\"font-size: x-large;margin-top: 7px;\">Login as Admin</p></button></div></form></div></div>");
			
			// Channel list panel
			pw.println("<div class=\"panel place-right\" style=\"width: 62%;\" >");
			pw.println("<div class=\"panel-header bg-gray fg-white\">Channel Available</div>");
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
					MyServletHelper.WriteChannelAccordionFrame(pw, mc, ListState.ALL);
					
				}
				pw.println("</div></div>");
			}			
			MyServletHelper.WriteLoginButtonScript(pw);
			MyServletHelper.WriteHTMLTail(pw);

			storage.closeEnvironment();
			return;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// String another = req.getParameter("another");
		// String done = req.getParameter("done");
		storage = new BDBStorage(storagePath);
		
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
				storage.closeEnvironment();
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
					Set<MyChannel> userChannel = null;
					if (currentChannelMap.containsKey(currentuser)) {
						userChannel = currentChannelMap.get(currentuser);
						userChannel.add(newc);
					} else {
						userChannel = new HashSet<MyChannel>();
						userChannel.add(newc);
						currentChannelMap.put(currentuser, userChannel);
					}
					storage.sync();
					MyServletHelper.WriteLoginSuccessPanel(pw,
							"Channel successfully added!");
					MyServletHelper.WriteHTMLTail(pw);
					storage.closeEnvironment();
					return;
				} else {
					MyServletHelper.WriteLoginFailPanel(pw,
							"Channel name already exists!");
					MyServletHelper.WriteHTMLTail(pw);
					storage.closeEnvironment();
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
					Set<MyChannel> listChannel = currentChannelMap.get(cu);
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
					storage.closeEnvironment();
					return;
				}
				else
				{
					MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...storage fail");
					MyServletHelper.WriteHTMLTail(pw);
					storage.closeEnvironment();
					return;
				}
			}
			else
			{
				MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...target null");
				MyServletHelper.WriteHTMLTail(pw);
				storage.closeEnvironment();
				return;
			}
		}
		else if(opt.equals("SUBSCRIBE"))
		{
			resp.setContentType("text/html");
			MyServletHelper.WriteHTMLHead(pw);
			

			String cu = (String) req.getSession().getAttribute("currentuser");
			String createUser = req.getParameter("createuser");
			String target=req.getParameter("targetchannel");
			
			if(storage.addSubscribe(cu, createUser, target))
			{
				MyServletHelper.WriteLoginSuccessPanel(pw, "Successfully subscribed channel : "+target);
				MyServletHelper.WriteHTMLTail(pw);
				storage.sync();
				storage.closeEnvironment();
				return;
			}
			else
			{
				MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...subscribe fail");
				MyServletHelper.WriteHTMLTail(pw);
				storage.closeEnvironment();
				return;
			}
			
		}
		else if(opt.equals("UNSUBSCRIBE"))
		{
			resp.setContentType("text/html");
			MyServletHelper.WriteHTMLHead(pw);
			String cu = (String) req.getSession().getAttribute("currentuser");
			String createUser = req.getParameter("createuser");
			String target=req.getParameter("targetchannel");
			
			if(storage.deleteSubscribe(cu, createUser, target))
			{
				MyServletHelper.WriteLoginSuccessPanel(pw, "Successfully unsubscribed channel : "+target);
				MyServletHelper.WriteHTMLTail(pw);
				storage.sync();
				storage.closeEnvironment();
				return;
			}
			else
			{
				MyServletHelper.WriteLoginFailPanel(pw, "Sorry, weird things happened...unsubscribe fail");
				MyServletHelper.WriteHTMLTail(pw);
				storage.closeEnvironment();
				return;
			}		
		}
		else if (opt.equals("ADMINLOGIN"))
		{
			resp.setContentType("text/html");
			String adminpass = req.getParameter("adminpass");
			MyServletHelper.WriteHTMLHead(pw);
			if (adminpass != null && adminpass.equals("admin"))
			{
				pw.println("<div class=\"panel\">");
				pw.println("<div class=\"panel-header bg-lightBlue fg-white\">Suceeded!</div>");
				pw.println("<div class=\"panel-content text-center\" style=\"display: block;\">");
				pw.println("<p style=\"font-size: large;\">Successfully logged in as Admin!</p>");
				pw.println("<a href=\"/xpathcrawler\"><button class=\"button warning\" style=\"width: 28%;\">" +
						"<p style=\"font-size: x-large;margin-top: 7px;\">Start as Admin</p></button></a>");
				pw.println("<a href=\"/xpath\"><button class=\"button info\" style=\"width: 28%;\">" +
						"<p style=\"font-size: x-large;margin-top: 7px;\">Back to Home</p></button></a>");
				pw.println("</div></div>");
				MyServletHelper.WriteHTMLTail(pw);
				storage.closeEnvironment();
				return;
			}
			else
			{
				MyServletHelper.WriteLoginFailPanel(pw, "Admin Password Incorrect! It should be 'admin'!");
				MyServletHelper.WriteHTMLTail(pw);
				storage.closeEnvironment();
				return;
			}
		}
		else if(opt.equals("DISPLAY"))
		{
			resp.setContentType("text/xml");
			// WRITE THE FORMATTED XML
			String target=req.getParameter("targetchannel");
			String cu = req.getParameter("createuser");
			
			MyChannel todisplay = null;
			try {
				todisplay = storage.getChannel(cu, target);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<String> urls = todisplay.getURLs();
			writeFormattedXML(urls, todisplay.getXslURL(), pw);
			storage.closeEnvironment();
		}
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
			//System.out.println("URL: "+url);
			String type = storage.getType(url);
			//System.out.println("TYPE: "+type);
			if (type.equals("xmls"))
			{
				DocumentBuilder db = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document dom = db.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(data.getData()),"UTF-8")));
				boolean[] res = xpe.evaluate(dom);
				//System.out.println(Arrays.toString(res));
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
	
	private void writeFormattedXML(List<String> urls, String xslt, PrintWriter pw)
	{
		System.out.println("AM I EVEN WRITING?");
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""+xslt+"\"?>");
		pw.println("<documentcollection>");
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'kk:mm:ss");
		for(String u : urls)
		{
			String raw = storage.getDocument(u);
			Long modified = storage.getModified(u);
			raw = raw.replaceAll("<\\?[^>]*\\?>", "");
			pw.println("<document ");
			pw.println("crawled=\"");
			pw.println(sdf.format(new Date(modified)));
			pw.println("\" location=\""+u+"\">");
			pw.println(raw);
			pw.println("</document>");
		}
		pw.println("</documentcollection>");
	}
	/* TODO: Implement user interface for XPath engine here */

}
