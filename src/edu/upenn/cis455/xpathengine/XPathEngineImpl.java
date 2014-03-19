package edu.upenn.cis455.xpathengine;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.servlet.MyHttpClient;
import edu.upenn.cis455.servlet.XPathServlet;

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
	  if(!tokenizer.hasMoreElements())
		  return false;
	  XPathTreeArray.set(i, documentBuilder.newDocument());
	  Stack<Element> prevStack = new Stack<Element>();
	  boolean b = recurIsValid(tokenizer, 0, XPathTreeArray.get(i), null, prevStack);
	  if (b == false)
	  {
		  XPathTreeArray.set(i, null);
	  }
	  //Print to verify
	  
	return b;
	  
  }
	
  public boolean[] evaluate(Document d) { 
    /* TODO: Check whether the document matches the XPath expressions */
	  
	  boolean [] result = new boolean[XPathArray.size()];
	  for(int i = 0; i < XPathArray.size(); i++)
	  {
		  Document xpathdoc = XPathTreeArray.get(i);
		  if(xpathdoc == null)
		  {
			  boolean valid = this.isValid(i);
			  if(valid == false)
			  {
				  result[i] = false;
				  continue;
			  }
			  else	// Start evaluation
			  {
				  result[i] = recurEvaluate(d.getDocumentElement(), XPathTreeArray.get(i).getDocumentElement(), 0);
			  }
		  }
		  else
		  {
			  result[i] = recurEvaluate(d.getDocumentElement(), XPathTreeArray.get(i).getDocumentElement(), 0);
		  }
	  }
    return result; 
  }
  /*
   * Recursive function to check if the XPath is valid.
   */
  private boolean recurIsValid(StringTokenizer tokens, int depth, Document document, Element element, Stack<Element> stack)
  {
	  String t = null;
	  if(tokens.hasMoreTokens())
		  t = tokens.nextToken();
	  else
	  {
		  if (!stack.empty())
			  return false;
		  else
			  return true;
	  }
	  if(t.equals("/"))			//elements
	  {
		  String name = tokens.nextToken();
		  if(validateName(name.trim()))
		  {
			  if(element == null)
			  {
				  Element current = document.createElement(name.trim());
				  document.appendChild(current);
				  return recurIsValid(tokens, depth+1, document, current, stack);
			  }
			  else
			  {
				  Element current = document.createElement(name.trim());
				  element.appendChild(current);
				  return recurIsValid(tokens, depth+1, document, current, stack);
			  }
		  }
		  else
			  return false;
	  }
	  else if(t.equals("["))	//predicates
	  {
		  if(element == null)	// axis must be '/'
			  return false;
		  else
		  {
			  String test = tokens.nextToken();
			  //First test case : text()=""
			  if(test.trim().matches("text\\s*\\(\\s*\\)\\s*="))
			  {
				  // Test if the test is a valid text() matching
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
						  //in case we're encountering a quote that is escaped, ignore it.
						  if(lasttoken != null && lasttoken.matches(".*(\\\\\\\\)*\\\\"))
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
						  // as long as open quote, append any character
						  if(openQuote != null && openQuote == true)
						  {
							  text.append(subtoken);
							  continue;
						  }
						  else	//Malformed xpath text() or contains part
							  return false;
					  }
				  } // while has more tokens
				  
				  if(openQuote == null || openQuote == true)	// not closed when finish, or not even started
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
					  // Escape the quotes
					  String mytext = text.toString().replace("\\\"", "\"");
					  mytext = text.toString().replace("\\\\", "\\");
					  // Create a ProcessingInstruction node 
					  element.appendChild(document.createProcessingInstruction("text", mytext));
					  return recurIsValid(tokens, depth, document, element, stack);
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
				  StringBuffer text = new StringBuffer();
				  while(tokens.hasMoreTokens())
				  {
					  lasttoken = subtoken;
					  subtoken = tokens.nextToken();
					  if(subtoken.equals("\""))
					  {
						  //This case we're encountering a quote that is escaped, so we ignore it.
						  if(lasttoken != null && lasttoken.matches(".*(\\\\\\\\)*\\\\"))
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
				  }
				  if(openQuote == null || openQuote == true)	// not closed when finish
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
				  {
					  String mytext = text.toString().replace("\\\"", "\"");
					  mytext = text.toString().replace("\\\\", "\\");
					  element.appendChild(document.createProcessingInstruction("contains", mytext));
					  return recurIsValid(tokens, depth, document, element, stack);
				  }
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
					  String attr = possible[0].trim();
					  //Check if the attname is validate
					  if(validateName(attr))
					  {
						  // same procedure as text()
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
								  if(lasttoken != null && lasttoken.matches(".*(\\\\\\\\)*\\\\"))
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
						  }
						  if(openQuote == null || openQuote == true)	// not closed when finish
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
						  {
							  String mytext = text.toString().replace("\\\"", "\"");
							  mytext = text.toString().replace("\\\\", "\\");
							  ((Element)element).setAttribute(attr, mytext);
							  return recurIsValid(tokens, depth, document, element, stack);
						  }
						  else
							  return false;
					  }	// if(validateName(attr))
					  else
						  return false;
				  }
			  } //starts with @
			  else	// recursive call to match step
			  {
				  
				  if(validateName(test.trim()))
				  {
					  Element child = document.createElement(test.trim());
					  element.appendChild(child);
					  stack.push(element);
					  return recurIsValid(tokens, depth+1, document, child, stack);
				  }
				  else
					  return false;
			  }
		  }
	  }
	  else if(t.equals("]"))	//predicates close
	  {
		  if(element == null || stack.empty())
			  return false;
		  else
		  {
			  Element prev = stack.pop();
			  return recurIsValid(tokens, depth-1, document, prev, stack);
		  }
	  }
	  else if(t.matches("\\s*"))
	  {
		  return recurIsValid(tokens, depth, document, element, stack);
	  }
	  else
	  {
		  return false;
	  }
  }
  
  /*
   * The recursive function to check if the dom matches a specific xpathtree
   */
  
  public boolean recurEvaluate(Element domElement, Element xpathElement, int depth)
  {
	  // Check tagname first
	  
	  if (! domElement.getTagName().equals(xpathElement.getTagName()))
	  {
		  return false;
	  }
	  // Check attributes
	  NamedNodeMap nnm = xpathElement.getAttributes();
	  boolean attrMatch = true;
	  for(int a = 0 ; a < nnm.getLength() ; a++)
	  {
		  String attrName = ((Attr)nnm.item(a)).getName();
		  String attrValue = ((Attr)nnm.item(a)).getValue();
		  if (domElement.hasAttribute(attrName))
		  {
			  if (domElement.getAttribute(attrName).equals(attrValue))
			  {
				  continue;
			  }
			  else
			  {
				  attrMatch = false;
				  break;
			  }
		  }
		  else
		  {
			  attrMatch = false;
			  break;
		  }
	  }
	  if (!attrMatch)
	  {
		  return false;
	  }
	  // Check child nodes recursively
	  NodeList nl = xpathElement.getChildNodes();
	  for(int i = 0 ; i < nl.getLength() ; i++)
	  {
		  Node node = nl.item(i);
		  typeswitch:
		  switch (node.getNodeType())
		  {
		  case Node.ELEMENT_NODE:	// An element, where we encounter a recursive call
			  //System.out.println("Element!");
			  NodeList nldom = domElement.getChildNodes();
			  Node nodedom = null;
			  boolean match = false;
			  for(int idom = 0 ; idom < nldom.getLength() ; idom++)
			  {
				  nodedom = nldom.item(idom);
				  if (nodedom.getNodeType() == Node.ELEMENT_NODE)	// matching element
				  {
					  match = recurEvaluate((Element)nodedom, (Element)node, depth+1);
					  if(match)
						  break typeswitch;
				  }
			  }
			  if(!match)
				  return false;
			  break;
		  case Node.PROCESSING_INSTRUCTION_NODE:
			  //System.out.println("Instruction!");
			  String target = ((ProcessingInstruction)node).getTarget();
			  String data = ((ProcessingInstruction)node).getData();
			  //System.out.println(target);
			  //System.out.println(data);
			  String textcontent = domElement.getTextContent();
			  NodeList textlist = domElement.getChildNodes();
			  HashSet<String> textset = new HashSet<String>();
			  for (int it = 0; it < textlist.getLength(); it++)
			  {
				  Node nt = textlist.item(it);
				  if(nt.getNodeType() == Node.TEXT_NODE)
				  {
					  textset.add(nt.getTextContent());
				  }
			  }
			  
			  if (target.equals("text"))	// deal with text()="..."
			  {
				  if (!textset.contains(data))
				  {
					  return false;
				  }
			  }
			  else if (target.equals("contains")) // deal with contains( text(), "...")
			  {
				  if (!textcontent.contains(data))
				  {
					  return false;
				  }
			  }
			  break;
		  default:
			  System.out.println("Other kind of node detected!!!!!!: "+node.getNodeType());
			  return false;
		  }
	  }
	  return true;
  }
  
  /*
   * The helper function to check if the nodename or attname is valid
   */
  public boolean validateName(String xpath)
  {
	  // Exactly from W3C XML Spec
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
  
  public String transformDoc(Document doc)
  {
	  Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			  //initialize StreamResult with File object to save to file
			  StreamResult result = new StreamResult(new StringWriter());
			  DOMSource source = new DOMSource(doc);
			  try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  String xmlString = result.getWriter().toString();
			  return xmlString;
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
  }
}
