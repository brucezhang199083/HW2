package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.servlet.MyHttpClient;

public class XPathEngineImpl implements XPathEngine {

	ArrayList<String> XPathArray;
	ArrayList<Document> XPathTreeArray;
	DocumentBuilder documentBuilder;
  public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
	  XPathArray = new ArrayList<String>();
	  XPathTreeArray = new ArrayList<Document>();
	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  try 
	  {
		  documentBuilder = dbf.newDocumentBuilder();
	  } 
	  catch (ParserConfigurationException e)
	  {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }
  }
	
  public void setXPaths(String[] s) {
    /* TODO: Store the XPath expressions that are given to this method */
	  XPathArray.clear();
	  XPathTreeArray.clear();
	  for(String xpath : s)
	  {
		  XPathArray.add(xpath);
		  XPathTreeArray.add(null);
	  }
  }
  /* 
   * Check which of the XPath expressions are valid 
   */
  public boolean isValid(int i) {
	  
	  if(i >= XPathArray.size())
		  return false;
	  StringTokenizer tokenizer = new StringTokenizer(XPathArray.get(i), "/[]\"", true);
	  XPathTreeArray.set(i, documentBuilder.newDocument());
	  return recurIsValid(tokenizer, 0, 0, XPathTreeArray.get(i), null);
  }
	
  public boolean[] evaluate(Document d) { 
    /* TODO: Check whether the document matches the XPath expressions */

	  boolean [] result = new boolean[XPathArray.size()];
	  for(int i = 0; i < XPathArray.size(); i++)
	  {
		  
	  }
    return null; 
  }
  /*
   * Recursive function to check if the XPath is valid.
   */
  private boolean recurIsValid(StringTokenizer tokens, int depth, int stacks, Document document, Element element)
  {
	  String t = null;
	  if(tokens.hasMoreTokens())
		  t = tokens.nextToken();
	  else
	  {
		  if (stacks != 0)
			  return false;
		  else
			  return true;
	  }
	  if(t.equals("/"))			//elements
	  {
		  String name = tokens.nextToken();
		  if(validateName(name))
		  {
			  if(element == null && depth == 0)
			  {
				  Element current = document.createElement(name);
				  return recurIsValid(tokens, depth+1, stacks, document, current);
			  }
			  else
			  {
				  Element current = document.createElement(name);
				  element.appendChild(current);
				  return recurIsValid(tokens, depth+1, stacks, document, current);
			  }
		  }
		  else
			  return false;
	  }
	  else if(t.equals("["))	//predicates
	  {
		  if(depth == 0)	// axis must be '/'
			  return false;
		  else
		  {
			  String test = tokens.nextToken();
			  //First test case : text()=""
			  if(test.trim().matches("text\\s*\\(\\s*\\)\\s*="))
			  {
				  // Test if the test is a valid text() or contains(text(),...) matching
				  Boolean openQuote = null;
				  String subtoken = null;
				  String lasttoken = null;
				  StringBuffer text = new StringBuffer();
				  while(tokens.hasMoreTokens())
				  {
					  lasttoken = subtoken;
					  subtoken = tokens.nextToken();
					  if(subtoken.equals("\""))
					  {
						  //This case we're encountering a quote that is escaped, so we ignore it.
						  if(lasttoken != null && lasttoken.matches(".*\\\\(\\\\\\\\)*"))
						  {
							  text.append(subtoken);
							  continue;
						  }
						  else
						  {
							  if(openQuote == null)
								  openQuote = true;
							  else if (openQuote == true)// finish the quote
							  {
								  openQuote = false;
								  break;
							  }
						  }
					  }
					  else
					  {
						  if(openQuote != null && openQuote == true)
						  {
							  text.append(subtoken);
							  continue;
						  }
						  else	//Malformed xpath text() or contains part
							  return false;
					  }
				  } // while has more tokens
				  if(openQuote != false)	// not closed when finish
					  return false;
				  // Check if it is closed by ']'
				  boolean openPredicate = true;
				  while (tokens.hasMoreTokens())
				  {
					  String closetok = tokens.nextToken();
					  if(closetok.matches("\\s*")) //Only part that is different from text()=
						  continue;
					  else if(closetok.equals("]"))
					  {
						  openPredicate = false;
						  break;
					  }
					  else
						  return false;
				  }
				  if(openPredicate == false)
				  {
					  element.appendChild(document.createTextNode(text.toString()));
					  return recurIsValid(tokens, depth, stacks, document, element);
				  }
				  else
					  return false;
			  }	//text()
			  else if(test.trim().matches("contains\\s*\\(\\s*text\\s*\\(\\s*\\)\\s*,"))
			  {
				// Test if the test is a valid text() or contains(text(),...) matching
				  Boolean openQuote = null;
				  String subtoken = null;
				  String lasttoken = null;
				  while(tokens.hasMoreTokens())
				  {
					  lasttoken = subtoken;
					  subtoken = tokens.nextToken();
					  if(subtoken.equals("\""))
					  {
						  //This case we're encountering a quote that is escaped, so we ignore it.
						  if(lasttoken != null && lasttoken.matches(".*\\\\(\\\\\\\\)*"))
							  continue;
						  else
						  {
							  if(openQuote == null)
								  openQuote = true;
							  else if (openQuote == true)// finish the quote
							  {
								  openQuote = false;
								  break;
							  }
						  }
					  }
					  else
					  {
						  if(openQuote != null && openQuote == true)
						  {
							  continue;
						  }
						  else	//Malformed xpath text() or contains part
							  return false;
					  }
				  }
				  if(openQuote != false)	// not closed when finish
					  return false;
				  // Check if it is properly closed
				  boolean openPredicate = true;
				  boolean openParenthesis = true;
				  while (tokens.hasMoreTokens())
				  {
					  String closetok = tokens.nextToken();
					  if(closetok.matches("\\s*\\)\\s*")) //Only part that is different from text()=
					  {  
						  openParenthesis = false;
						  continue;
					  }
					  else if(closetok.equals("]"))
					  {
						  openPredicate = false;
						  break;
					  }
					  else
						  return false;
				  }
				  if(openPredicate == false && openParenthesis == false)
					  return recurIsValid(tokens, depth, stacks, document, element);
				  else
					  return false;
			  } //contains()
			  else if(test.trim().startsWith("@"))
			  {
				  String [] possible = test.trim().substring(1).split("=");
				  if(possible.length != 1)
				  {	//Malformed @attname test
					  return false;
				  }
				  else
				  {
					  String attr = possible[0].replaceAll("\\s*$", "");
					  //Check if the attname is validate
					  if(validateName(attr))
					  {
						  // same procedure as text()
						  Boolean openQuote = null;
						  String subtoken = null;
						  String lasttoken = null;
						  while(tokens.hasMoreTokens())
						  {
							  lasttoken = subtoken;
							  subtoken = tokens.nextToken();
							  if(subtoken.equals("\""))
							  {
								  //This case we're encountering a quote that is escaped, so we ignore it.
								  if(lasttoken != null && lasttoken.matches(".*\\\\(\\\\\\\\)*"))
									  continue;
								  else
								  {
									  if(openQuote == null)
										  openQuote = true;
									  else if (openQuote == true)// finish the quote
									  {
										  openQuote = false;
										  break;
									  }
								  }
							  }
							  else
							  {
								  if(openQuote != null && openQuote == true)
								  {
									  continue;
								  }
								  else	//Malformed xpath text() or contains part
									  return false;
							  }
						  }
						  if(openQuote != false)	// not closed when finish
							  return false;
						  // check closure
						  boolean openPredicate = true;
						  while (tokens.hasMoreTokens())
						  {
							  String closetok = tokens.nextToken();
							  if(closetok.matches("\\s*")) //Only part that is different from text()=
								  continue;
							  else if(closetok.equals("]"))
							  {
								  openPredicate = false;
								  break;
							  }
							  else
								  return false;
						  }
						  if(openPredicate == false)
							  return recurIsValid(tokens, depth, stacks);
						  else
							  return false;
					  }	// if(validateName(attr))
					  else
						  return false;
				  }
			  } //starts with @
			  else	// recursive call to match step
			  {
				  if(validateName(test))
					  return recurIsValid(tokens, depth+1, stacks+1);
				  else
					  return false;
			  }
		  }
	  }
	  else if(t.equals("]"))	//predicates close
	  {
		  if(depth == 0 || stacks <= 0)
			  return false;
		  else
		  {
			  return recurIsValid(tokens, depth, stacks-1);
		  }
	  }
	  else
	  {
		  return false;
	  }
  }
  
  
  /*
   * The helper function to check if the nodename or attname is valid
   */
  public boolean validateName(String xpath)
  {
	  String startcharset = "_\\x41-\\x5A\\x61-\\x7A\\xC0-\\xD6\\xD8-\\xF6\\xF8-\\xFF" +
	  					 "\\x370-\\x37D\\x{037F}-\\x{1FFF}\\x{200C}-\\x{200D}\\x{2070}-\\x{218F}" +
	  					 "\\x{2C00}-\\x{2FEF}\\x{3001}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFFD}" +
	  					 "\\x{10000}-\\x{EFFFF}";
	  String nameregex = "[-"+startcharset+"0-9\\.\\xB7\\x{0300}-\\x{036F}\\x{203F}-\\x{2040}]+";
	  if (xpath != null && xpath.length() != 0)
	  {
		  String start = xpath.substring(0,1);
		  if (!Character.isDigit(start.charAt(0)) && start.matches("["+startcharset+"]"))
		  {
			  return xpath.matches(nameregex);
		  }
		  else
			  return false;
	  }
	  else
	  {
		  return false;
	  }
  }
}
