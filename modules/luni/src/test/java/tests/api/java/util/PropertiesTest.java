/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.InvalidPropertiesFormatException;

import tests.support.resource.Support_Resources;

public class PropertiesTest extends junit.framework.TestCase {

	Properties tProps;

	byte[] propsFile;

	/**
	 * @tests java.util.Properties#Properties()
	 */
	public void test_Constructor() {
		// Test for method java.util.Properties()
		Properties p = new Properties();
		// do something to avoid getting a variable unused warning
		p.clear(); 
		assertTrue("Created incorrect Properties", true);
	}

	/**
	 * @tests java.util.Properties#Properties(java.util.Properties)
	 */
	public void test_ConstructorLjava_util_Properties() {
		// Test for method java.util.Properties(java.util.Properties)
		if (System.getProperty("java.vendor") != null) {
			try {
				Properties p = new Properties(System.getProperties());
				assertNotNull("failed to construct correct properties", p
						.getProperty("java.vendor"));
			} catch (Exception e) {
				fail("exception occured while creating construcotr" + e);
			}
		}

	}

	/**
	 * @tests java.util.Properties#getProperty(java.lang.String)
	 */
	public void test_getPropertyLjava_lang_String() {
		// Test for method java.lang.String
		// java.util.Properties.getProperty(java.lang.String)
		assertEquals("Did not retrieve property", "this is a test property", ((String) tProps
				.getProperty("test.prop")));
	}

	/**
	 * @tests java.util.Properties#getProperty(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_getPropertyLjava_lang_StringLjava_lang_String() {
		// Test for method java.lang.String
		// java.util.Properties.getProperty(java.lang.String, java.lang.String)
		assertEquals("Did not retrieve property", "this is a test property", ((String) tProps.getProperty(
				"test.prop", "Blarg")));
		assertEquals("Did not return default value", "Gabba", ((String) tProps
				.getProperty("notInThere.prop", "Gabba")));
	}

	/**
	 * @tests java.util.Properties#list(java.io.PrintStream)
	 */
	public void test_listLjava_io_PrintStream() {
		// Test for method void java.util.Properties.list(java.io.PrintStream)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		Properties myProps = new Properties();
		String propList;
		myProps.setProperty("Abba", "Cadabra");
		myProps.setProperty("Open", "Sesame");
		myProps.list(ps);
		ps.flush();
		propList = baos.toString();
		assertTrue("Property list innacurate", (propList
				.indexOf("Abba=Cadabra") >= 0)
				&& (propList.indexOf("Open=Sesame") >= 0));
	}

	/**
	 * @tests java.util.Properties#list(java.io.PrintWriter)
	 */
	public void test_listLjava_io_PrintWriter() {
		// Test for method void java.util.Properties.list(java.io.PrintWriter)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		Properties myProps = new Properties();
		String propList;
		myProps.setProperty("Abba", "Cadabra");
		myProps.setProperty("Open", "Sesame");
		myProps.list(pw);
		pw.flush();
		propList = baos.toString();
		assertTrue("Property list innacurate", (propList
				.indexOf("Abba=Cadabra") >= 0)
				&& (propList.indexOf("Open=Sesame") >= 0));
	}

	/**
	 * @tests java.util.Properties#load(java.io.InputStream)
	 */
	public void test_loadLjava_io_InputStream() {
		// Test for method void java.util.Properties.load(java.io.InputStream)
		Properties prop = null;
		try {
			InputStream is;
			prop = new Properties();
			prop.load(is = new ByteArrayInputStream(writeProperties()));
			is.close();
		} catch (Exception e) {
			fail("Exception during load test : " + e.getMessage());
		}
		assertEquals("Failed to load correct properties", "harmony.tests", prop.getProperty(
				"test.pkg"));
		assertNull("Load failed to parse incorrectly", prop
				.getProperty("commented.entry"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream("=".getBytes()));
		} catch (IOException e) {
		}
		assertTrue("Failed to add empty key", prop.get("").equals(""));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(" = ".getBytes()));
		} catch (IOException e) {
		}
		assertTrue("Failed to add empty key2", prop.get("").equals(""));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(" a= b".getBytes()));
		} catch (IOException e) {
		}
		assertEquals("Failed to ignore whitespace", "b", prop.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(" a b".getBytes()));
		} catch (IOException e) {
		}
		assertEquals("Failed to interpret whitespace as =", 
				"b", prop.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream("#\u008d\u00d2\na=\u008d\u00d3"
					.getBytes("ISO8859_1")));
		} catch (IOException e) {
		}
		assertEquals("Failed to parse chars >= 0x80", 
				"\u008d\u00d3", prop.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(
					"#properties file\r\nfred=1\r\n#last comment"
							.getBytes("ISO8859_1")));
		} catch (IOException e) {
		} catch (IndexOutOfBoundsException e) {
			fail("IndexOutOfBoundsException when last line is a comment with no line terminator");
		}
		assertEquals("Failed to load when last line contains a comment", "1", prop
				.get("fred"));
	}

	/**
	 * @tests java.util.Properties#load(java.io.InputStream)
	 */
	public void test_loadLjava_io_InputStream_subtest0() {
		try {
			InputStream is = Support_Resources
					.getStream("hyts_PropertiesTest.properties");
			Properties props = new Properties();
			props.load(is);
			is.close();
			assertEquals("1", "\n \t \f", props.getProperty(" \r"));
			assertEquals("2", "a", props.getProperty("a"));
			assertEquals("3", "bb as,dn   ", props.getProperty("b"));
			assertEquals("4", ":: cu", props.getProperty("c\r \t\nu"));
			assertEquals("5", "bu", props.getProperty("bu"));
			assertEquals("6", "d\r\ne=e", props.getProperty("d"));
			assertEquals("7", "fff", props.getProperty("f"));
			assertEquals("8", "g", props.getProperty("g"));
			assertEquals("9", "", props.getProperty("h h"));
			assertEquals("10", "i=i", props.getProperty(" "));
			assertEquals("11", "   j", props.getProperty("j"));
			assertEquals("12", "   c", props.getProperty("space"));
			assertEquals("13", "\\", props.getProperty("dblbackslash"));
		} catch (IOException e) {
			fail("Unexpected: " + e);
		}
	}

	/**
	 * @tests java.util.Properties#propertyNames()
	 */
	public void test_propertyNames() {
		// Test for method java.util.Enumeration
		// java.util.Properties.propertyNames()

		Enumeration names = tProps.propertyNames();
		int i = 0;
		while (names.hasMoreElements()) {
			++i;
			String p = (String) names.nextElement();
			assertTrue("Incorrect names returned", p.equals("test.prop")
					|| p.equals("bogus.prop"));
		}
	}

	/**
	 * @tests java.util.Properties#save(java.io.OutputStream, java.lang.String)
	 */
	public void test_saveLjava_io_OutputStreamLjava_lang_String() {
		// Test for method void java.util.Properties.save(java.io.OutputStream,
		// java.lang.String)
		Properties myProps = new Properties();
		Properties myProps2 = new Properties();
		Enumeration e;
		String nextKey;

		myProps.setProperty("Property A", "aye");
		myProps.setProperty("Property B", "bee");
		myProps.setProperty("Property C", "see");

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			myProps.save(out, "A Header");
			out.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out
					.toByteArray());
			myProps2.load(in);
			in.close();
		} catch (IOException ioe) {
			fail("IOException occurred reading/writing file : "
					+ ioe.getMessage());
		}

		e = myProps.propertyNames();
		while (e.hasMoreElements()) {
			nextKey = (String) e.nextElement();
			assertTrue("Stored property list not equal to original", myProps2
					.getProperty(nextKey).equals(myProps.getProperty(nextKey)));
		}

	}

	/**
	 * @tests java.util.Properties#setProperty(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_setPropertyLjava_lang_StringLjava_lang_String() {
		// Test for method java.lang.Object
		// java.util.Properties.setProperty(java.lang.String, java.lang.String)
		Properties myProps = new Properties();
		myProps.setProperty("Yoink", "Yabba");
		assertEquals("Failed to set property", "Yabba", myProps.getProperty("Yoink")
				);
		myProps.setProperty("Yoink", "Gab");
		assertEquals("Failed to reset property", "Gab", myProps.getProperty("Yoink")
				);
	}

	/**
	 * @tests java.util.Properties#store(java.io.OutputStream, java.lang.String)
	 */
	public void test_storeLjava_io_OutputStreamLjava_lang_String() {
		// Test for method void java.util.Properties.store(java.io.OutputStream,
		// java.lang.String)
		Properties myProps = new Properties();
		Properties myProps2 = new Properties();
		Enumeration e;
		String nextKey;

		myProps.put("Property A", " aye\\\f\t\n\r\b");
		myProps.put("Property B", "b ee#!=:");
		myProps.put("Property C", "see");

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			myProps.store(out, "A Header");
			out.close();
			ByteArrayInputStream in = new ByteArrayInputStream(out
					.toByteArray());
			myProps2.load(in);
			in.close();
		} catch (IOException ioe) {
			fail("IOException occurred reading/writing file : "
					+ ioe.getMessage());
		}

		e = myProps.propertyNames();
		while (e.hasMoreElements()) {
			nextKey = (String) e.nextElement();
			assertTrue("Stored property list not equal to original", myProps2
					.getProperty(nextKey).equals(myProps.getProperty(nextKey)));
		}

	}

	/**
     * @tests java.util.Properties#loadFromXML(java.io.InputStream)
     */
    public void test_loadFromXMLLjava_io_InputStream() {
        // Test for method void java.util.Properties.loadFromXML(java.io.InputStream)
        Properties prop = null;
        try {
            InputStream is;
            prop = new Properties();
            prop.loadFromXML(is = new ByteArrayInputStream(writePropertiesXMLUTF_8()));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "value3", prop.getProperty(
        "key3"));
        assertEquals("Failed to load correct properties", "value1", prop.getProperty(
        "key1"));
                
        try {
            InputStream is;
            prop = new Properties();
            prop.loadFromXML(is = new ByteArrayInputStream(writePropertiesXMLISO_8859_1()));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "value2", prop.getProperty(
        "key2"));
        assertEquals("Failed to load correct properties", "value1", prop.getProperty(
        "key1"));
    }
    
    /**
     * @tests java.util.Properties#storeToXML(java.io.OutputStream, java.lang.String, java.lang.String)
     */
    public void test_storeToXMLLjava_io_OutputStreamLjava_lang_StringLjava_lang_String() {
        // Test for method void java.util.Properties.storeToXML(java.io.OutputStream,
        // java.lang.String, java.lang.String)
        Properties myProps = new Properties();
        Properties myProps2 = new Properties();
        Enumeration e;
        String nextKey;

        myProps.setProperty("key1", "value1");
        myProps.setProperty("key2", "value2");
        myProps.setProperty("key3", "value3");
        myProps.setProperty("<a>key4</a>", "\"value4");
        myProps.setProperty("key5   ", "<h>value5</h>");
        myProps.setProperty("<a>key6</a>", "   <h>value6</h>   ");
        myProps.setProperty("<comment>key7</comment>", "value7");
        myProps.setProperty("  key8   ", "<comment>value8</comment>");
        myProps.setProperty("&lt;key9&gt;", "\u0027value9");
        myProps.setProperty("key10\"", "&lt;value10&gt;");
        myProps.setProperty("&amp;key11&amp;", "value11");
        myProps.setProperty("key12", "&amp;value12&amp;");
        myProps.setProperty("<a>&amp;key13&lt;</a>", "&amp;&value13<b>&amp;</b>");
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            //store in UTF-8 encoding
            myProps.storeToXML(out, "comment");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2.loadFromXML(in);
            in.close();
        } catch (InvalidPropertiesFormatException ipfe) {
            fail("InvalidPropertiesFormatException occurred reading file: "
                    + ipfe.getMessage());
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        e = myProps.propertyNames();
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            //store in ISO-8859-1 encoding
            myProps.storeToXML(out, "comment", "ISO-8859-1");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2 = new Properties();
            myProps2.loadFromXML(in);
            in.close();
        } catch (InvalidPropertiesFormatException ipfe) {
            fail("InvalidPropertiesFormatException occurred reading file: "
                    + ipfe.getMessage());
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        e = myProps.propertyNames();
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }        
    }
    
    /**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		tProps = new Properties();
		tProps.put("test.prop", "this is a test property");
		tProps.put("bogus.prop", "bogus");
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected byte[] writeProperties() throws IOException {
		PrintStream ps = null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ps = new PrintStream(bout);
		ps.println("#commented.entry=Bogus");
		ps.println("test.pkg=harmony.tests");
		ps.println("test.proj=Automated Tests");
		ps.close();
		return bout.toByteArray();
	}
    
    protected byte[] writePropertiesXMLUTF_8() throws IOException {
        PrintStream ps = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ps = new PrintStream(bout, true, "UTF-8");
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        ps.println("<properties>");
        ps.println("<comment>comment</comment>");
        ps.println("<entry key=\"key4\">value4</entry>");
        ps.println("<entry key=\"key3\">value3</entry>");
        ps.println("<entry key=\"key2\">value2</entry>");
        ps.println("<entry key=\"key1\"><!-- xml comment -->value1</entry>");
        ps.println("</properties>");
        ps.close();
        return bout.toByteArray();
    }
    
    protected byte[] writePropertiesXMLISO_8859_1() throws IOException {
        PrintStream ps = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ps = new PrintStream(bout, true, "ISO-8859-1");
        ps.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        ps.println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        ps.println("<properties>");
        ps.println("<comment>comment</comment>");
        ps.println("<entry key=\"key4\">value4</entry>");
        ps.println("<entry key=\"key3\">value3</entry>");
        ps.println("<entry key=\"key2\">value2</entry>");
        ps.println("<entry key=\"key1\"><!-- xml comment -->value1</entry>");
        ps.println("</properties>");
        ps.close();
        return bout.toByteArray();
    }
}
