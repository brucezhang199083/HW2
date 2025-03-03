package test.edu.upenn.cis455;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class XPathEngineImplTest extends TestCase {

	String [] validxpaths = {"/test[ a/b1[ c1[p]/d[p] ] /n1[a]/n2 [c2/d[p]/e[text()=\"/asp[&123(123*/]\"]]]",
			"/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy]",
			"/a1[text()=\"@#$%^\"]/ddddd",
			"/helloworld[hellothere]",
			"/foo/bar[@att=\"123\"]",
			"/foo/bar[contains ( text() , \"valid!\")]/nextbar[contains ( text() , \"valid!\")]/thirdbar",
			"/blah[anotherElement]",
			"/this/that[something/else]",
			"/d/e/f[foo[text()=\"something\"]][bar]",
			"/a/b/c[   text (  )  =   \"  White Spaces Should Not Matter \"   ]",
			"/root[text ()=\"super \\\"[]f@ncy\\\" _*&^%<>anno0ying text\"]"
			};
	String [] invalidxpaths = {
			"//root",
			"/123abc/456def",
			"/+_+[*_*]",
			"/root/foo[text()=\"bar\"[text()=blah]]",
			"/[contains(text(), \"\\\"\")]",
			"[invalid]",
			"/foo[@attname=\"unclosed quote]",
			"/foo[text()=",
			"/foo[ contains(text(), \"unclosed parenthesis\"]",
			"/foo[@1nvalidattname=\"123\"]",
			"/foo[bar[text()=\"wrong stacks\"]"	};
	
	public void testValidateName() {
		XPathEngineImpl x = new XPathEngineImpl();
		assertTrue(x.validateName("ABCDEFG"));
		assertTrue(x.validateName("shift_a"));
		assertFalse(x.validateName("hey babe"));
		assertFalse(x.validateName("[]"));
		assertTrue(x.validateName("_123abc"));
		assertFalse(x.validateName("7abc"));
		assertFalse(x.validateName("!@#$%$"));
		assertFalse(x.validateName("/"));
		assertFalse(x.validateName("abc:def"));
		assertFalse(x.validateName("@attname"));

	}
	
	public void testIsValid()
	{
		XPathEngineImpl x = new XPathEngineImpl();
		
			x.setXPaths(validxpaths);
			boolean ans = x.isValid(0);	
			assertTrue(ans);
			ans = x.isValid(1);
			assertTrue(ans);
			ans = x.isValid(2);
			assertTrue(ans);
			ans = x.isValid(3);
			assertTrue(ans);
			ans = x.isValid(4);
			assertTrue(ans);
			ans = x.isValid(5);
			assertTrue(ans);
			ans = x.isValid(6);
			assertTrue(ans);
			ans = x.isValid(7);
			assertTrue(ans);
			ans = x.isValid(8);
			assertTrue(ans);
			ans = x.isValid(9);
			assertTrue(ans);
			ans = x.isValid(10);
			assertTrue(ans);
			
			x.setXPaths(invalidxpaths);
			ans = x.isValid(0);
			assertFalse(ans);
			ans = x.isValid(1);
			assertFalse(ans);
			ans = x.isValid(2);
			assertFalse(ans);
			ans = x.isValid(3);
			assertFalse(ans);
			ans = x.isValid(4);
			assertFalse(ans);
			ans = x.isValid(5);
			assertFalse(ans);
			ans = x.isValid(6);
			assertFalse(ans);
			ans = x.isValid(7);
			assertFalse(ans);
			ans = x.isValid(8);
			assertFalse(ans);
			ans = x.isValid(9);
			assertFalse(ans);
			ans = x.isValid(10);
			assertFalse(ans);
		
	}

	public void testEvaluate()
	{
		XPathEngineImpl x = new XPathEngineImpl();
		String [] onlyone = {"/root[abc[@att2=\"num2\"]][abc[@att1=\"num1\"]][l1/l2/l3/l4/l5/l6[@says=\"deep\"][contains(text(), \"eepd\")]]"};
		x.setXPaths(onlyone);
		x.isValid(0);
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse("http://www.seas.upenn.edu/~zhanghao/test2.xml");
			boolean [] result = x.evaluate(doc);
			assertTrue(result[0]);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String [] another = {"/html[@xmlns =\"http://www.w3.org/1999/xhtml\"][head/title[contains(text(), \"Computer & Inf\")]]"};
		x.setXPaths(another);
		x.isValid(0);
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse("http://www.seas.upenn.edu/~zhanghao/cishome.xml");
			boolean [] result = x.evaluate(doc);
			assertTrue(result[0]);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
