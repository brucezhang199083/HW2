package test.edu.upenn.cis455;

import edu.upenn.cis455.crawler.XPathCrawlerTest;
import edu.upenn.cis455.servlet.XPathServletTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
        // Class.forName("your.class.name.here") 
    		  MyHttpClientTest.class,
    		  XPathEngineImplTest.class,
    		  XPathServletTest.class,
    		  BDBStorageTest.class,
    		  RobotRulesTest.class,
    		  XPathCrawlerTest.class
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
