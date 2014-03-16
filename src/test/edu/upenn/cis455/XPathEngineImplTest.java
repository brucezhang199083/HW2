package test.edu.upenn.cis455;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class XPathEngineImplTest extends TestCase {

	public void testValidateName() {
		XPathEngineImpl x = new XPathEngineImpl();
		String a = "abc";
		System.out.println(a.split("ac").length);
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
		String [] xpaths = {"/root/rot/rt",
							"/abc/def[text()=\"abc\"]",
							"/!@!@!/ddddd",
							"[hellothere]",
							"/foo/bar[@att=\"123\"]",
							"/blah[anotherElement]",
							"/this/that[something/else]",
							"/d/e/f[foo[text()=\"something\"]][bar]",
							"/a/b/c[text()  =   \"white Spaces Should Not Matter \"]",
							"/root[text ()=\"super \\\"f@ncy\\\" _*&^%<>anno0ying text\"]"};
		x.setXPaths(xpaths);
			
		boolean ans = x.isValid(0);
			assertTrue(ans);
			ans = x.isValid(1);
			assertTrue(ans);
			ans = x.isValid(2);
			assertFalse(ans);
			ans = x.isValid(3);
			assertFalse(ans);
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
	}

}
