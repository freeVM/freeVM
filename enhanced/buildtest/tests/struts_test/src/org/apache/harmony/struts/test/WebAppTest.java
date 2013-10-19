/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.struts.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.ClickableElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * WebAppTest class represents generic test driver for web applications testing.
 * 
 * It depends on
 * 
 *   - htmlUnit v. 1.11 framework (http://sourceforge.net/projects/htmlunit/);
 *   - jUnit v. 4.1 framework (http://sourceforge.net/projects/junit/).
 *   
 * There are two main working modes:
 * 
 *   - 'spider' (write) mode for 'golden' data/test scenario generation;
 *   - 'test' (read) mode for test scenario playback. Text data returned from
 *     server during scenario execution compared against pregenerated 'golden'
 *     data.
 * 
 * The following files are produced/used by the WebAppTest:
 *   
 *   - 'scenario.properties' - Main test scenario generated during 'spider' mode
 *      run and played during 'test' run. May be updated manually after
 *      generation.
 *      Sample (beginning fragment, note - form submission added manually here):
 *       10000.URL./struts2-showcase-2.0.6/showcase.jsp :file520468537.html
 *       10010.ANCHOR.3 :file1722219095.html
 *       10020.ANCHOR.0 :file-1080589601.html
 *       10030.ANCHOR.20 :file857505531.html
 *       10040.ANCHOR.19 :file1376914575.html
 *       10050.BREAK_LEVEL :4
 *       10060.BREAK_LEVEL :3
 *       10070.BREAK_LEVEL :2
 *       10080.ANCHOR.1 :file-2140644304.html
 *       10085.FORM.0.data.test.Update\ Content  :update_content.html
 *       10086.BREAK_LEVEL :3
 *       10090.ANCHOR.20 :file-263602398.html
 *       ...
 *   - 'gen.patterns.properties' - regex patterns to be applied to the 'golden'
 *      content (text server responses) before writing it to the 'golden' file.
 *      Must be presented if running in 'spider' mode. May contain no patterns.
 *      Use pattern description line syntax as follows:
 *        nnnn.<regex>
 *      in this case relacement string is "". OR
 *        nnnn.<regex> :<replacement-string>
 *      The patterns will be applied in their 'nnnn' ascending order.
 *      Sample:
 *       0010.;jsessionid\=\\w{32}
 *       ...
 *   - 'compare.patterns.properties' - regex patterns to be applied to the
 *      'golden' content and server responses before actual comparison.
 *      Must be presented if running in 'test' mode. May contain no patterns if
 *      there is no test execution context related info in golden files and
 *      server responses (such as timestamps, references to files with concrete
 *      paths etc). Use the same pattern description line syntax as
 *      cpecified for 'gen.patterns.properties' above.
 *      Sample:
 *       0010.;jsessionid\=\\w{32}
 *       0020.[0-9]{2,4}[/\.-][0-9]{2}[/\.-][0-9]{2,4}
 *       0030.[0-9]{1,2}\:[0-9]{2}[\:[0-9]{2}]*
 *       0040.[0-9]*\ @\ [0-9]*
 *       0050.<td>.*@\\w{5,}.*</td>
 *       0060."struts\.token"\ value\="\\w{30,}" :"struts\.token"\ value\=""
 *       0080.file%3A.*%2Fjakarta-tomcat-5\.0\.30 :file%3A%2Fjakarta-tomcat-5\.0\.30
 *       0090.nifty\.js\\?config\=.*" :"
 *       0100.<td>.*\ ago</td> :<td></td>
 *       0110.[A-Z][a-z]{2},*\ [0-9]{1,2},*\ [0-9]{4}
 *       0120.[AP]M</td> :</td>
 *       0130.[A-Z][a-z]{2}\ [A-Z][a-z]{2}\ [0-9]{2}
 *       0140.[A-Z]{4,5}\ [0-9]{4}
 *       ...
 *   - 'fileXXXXXXXX.yyyy' - 'golden' file containing test data as it was
 *      returned from server and processed by all replacement patterns from
 *      'gen.patterns.properties' file during 'spider' mode run.
 *      
 *   NOTE: ALL THE ABOVE FILES MUST BE IN THE DIR SPECIFIED BY
 *   '<app.name>.resource.path' SYSTEM PROPERTY DESCRIBED BELOW.
 *      
 * The following system properties are used to control WebAppTest execution:
 *   
 *   - 'webapptest.app.name' - mandatory. Name of your concrete test. We will
 *      refer it's value as '<app.name>' below;
 *   - 'webapptest.debug' - optional, default is false. Wether to print info
 *      about visited anchors to stdout in both modes or not;
 *   - 'webapptest.spider.mode' - optional, false by default. Wether to run
 *      in 'write' mode or not;
 *   - 'webapptest.spider.nestinglevel' - optional for both modes,
 *      default is '10'. Maximum nesting level for visiting anchors starting
 *      from web application home. MUST be the same for 'spider' and 'test'
 *      modes for tested web application;
 *   - '<app.name>.resource.path' - optional, default is relative to the test
 *      working dir path 'resources/<app.name>';
 *   - '<app.name>.app.host' - optional, default is 'localhost' (<app.host> below)
 *   - '<app.name>.app.port' - optional, default is '8080' (<app.port> below)
 *   - 'webapptest.spider.startfile' - mandatory for 'spider' mode. App home
 *      URL's tail (<spider.startfile> below) for 'spider' mode. The URL is
 *      constructed as:
 *          http://<app.host>:<app.port><spider.startfile>
 *   - 'http.proxy.host' - optional, specify if connection to the app. host is
 *      through proxy;
 *   - 'http.proxy.port' - optional, default is '-1'.
 */
public class WebAppTest {
    
    private static final String ACTION_URL = "URL";
    private static final String ACTION_ANCHOR = "ANCHOR";
    private static final String ACTION_FORM = "FORM";
    private static final String ACTION_ELEMENT = "ELEMENT";
    private static final String ACTION_BREAK_LEVEL = ".BREAK_LEVEL";
    
    private static final String APP_PROPERTIES_FNAME = "scenario.properties";
    private static final String APP_PATTERNS_FNAME_SUFFIX = "patterns.properties";
    
    private static final int URL_FLAG = -1;
    
    private WebClient wc;
    private int stepCounter = 10000;
    private boolean spiderMode =
        (System.getProperty("webapptest.spider.mode") != null);
    private boolean noCheckMode =
        (System.getProperty("webapptest.nocheck") != null);
    private String spiderStartFile =
        System.getProperty("webapptest.spider.startfile");
    private final int SPIDER_NESTING_LEVEL =
        Integer.parseInt(
                System.getProperty("webapptest.spider.nestinglevel", "10"));
    private final String appName =
        System.getProperty("webapptest.app.name");
    private final boolean debug =
        (System.getProperty("webapptest.debug") != null);
    private String APP_RESOURCE_PATH;
    
    private HashSet<String> visited = new HashSet<String>();

    private Map appPropertiesMap = new TreeMap();
    private Map appPatternsMap = new TreeMap();
    private URL baseUrl;
        
    // for pre 4.x test runners
    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(WebAppTest.class); 
    }
    
    public static void main(String[] args) {
        JUnitCore.runClasses(WebAppTest.class); 
    }
    
    @Before
    public void setUp() throws FileNotFoundException, IOException {
        initApp();
        initWebClient(BrowserVersion.MOZILLA_1_0);
    }
    
    @SuppressWarnings("unchecked")
    private void initApp() throws FileNotFoundException, IOException {
        if (this.appName == null) {
            throw new RuntimeException(
            "\"webapptest.app.name\" system property must be set");
        }
        String patternsFileNamePrefix = "compare.";
        if (this.spiderMode) {
            if(this.spiderStartFile == null) {
                throw new RuntimeException(
                "\"webapptest.spider.startfile\" system property must be set");
            }
            patternsFileNamePrefix = "gen.";
        }
        if (this.spiderMode && this.noCheckMode) {
            throw new RuntimeException(
            "\"webapptest.spider.mode\" OR \"webapptest.nocheck\" can be set");
        }
        this.APP_RESOURCE_PATH = System.getProperty(
                this.appName + ".resource.path", "resources" +
                File.separator + this.appName);
        String appHost =
            System.getProperty(this.appName + ".app.host", "localhost");
        int appPort = Integer.parseInt(
                System.getProperty(this.appName + ".app.port", "8080"));
        this.baseUrl = new URL("http", appHost, appPort, "");
        FileInputStream props = null;
        try{
            props = new FileInputStream(new File(
                    this.APP_RESOURCE_PATH,
                    patternsFileNamePrefix + APP_PATTERNS_FNAME_SUFFIX));
            Properties appPatterns = new Properties();
            appPatterns.load(props);
            this.appPatternsMap.putAll(appPatterns);
        } finally {
            if (props != null) {
                props.close();
            }
        }
    }
    
    private void initWebClient(BrowserVersion bw) {
        String proxyHost = System.getProperty("http.proxy.host");
        int proxyPort = -1;
        if (proxyHost != null) {
            proxyPort = Integer.parseInt(
                    System.getProperty("http.proxy.port", "-1"));
            wc = new WebClient(
                    bw, proxyHost, proxyPort);
        } else {
            wc = new WebClient(bw);
        }
        wc.setThrowExceptionOnScriptError(false);
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.setRedirectEnabled(true);
    }
    
    @Test
    public void test() throws SAXException, MalformedURLException, IOException {
        PrintStream errStream = null;
        try {
            // redirect stderr
            errStream = new PrintStream(new File(
                    this.APP_RESOURCE_PATH, this.appName + ".err"));
            System.setErr(errStream);
            if (this.spiderMode) {
                launchSpiderW(SPIDER_NESTING_LEVEL);
            } else {
                launchSpiderR(SPIDER_NESTING_LEVEL);
            }
            if (debug) {
                DBG(SPIDER_NESTING_LEVEL, "+---", "Stop");
            }            
        } finally {
            if (errStream != null) {
                errStream.close();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void launchSpiderW(int nestingLevel)
    throws RuntimeException, MalformedURLException, IOException, SAXException {
        DataOutputStream props = null;
        try {
            props = new DataOutputStream(new FileOutputStream(new File(
                    this.APP_RESOURCE_PATH, APP_PROPERTIES_FNAME)));
            if (debug) {
                DBG(nestingLevel, "----", "URL: " + this.spiderStartFile);
            }
            HtmlPage p = (HtmlPage)this.wc.getPage(
                    new URL(this.baseUrl, this.spiderStartFile));
            String pHtml = p.getWebResponse().getContentAsString();
            pHtml = preprocessUsingPatterns(pHtml);
            writeData(nestingLevel, props, URL_FLAG, this.spiderStartFile,
                    pHtml, ".html");
            visitAnchors(nestingLevel, new HashSet<String>(), p, props);
        } finally {
            if (props != null) {
                props.close();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void launchSpiderR(int nestingLevel)
    throws RuntimeException, MalformedURLException, IOException, SAXException {
        FileInputStream props = null;
        try{
            props = new FileInputStream(new File(
                    this.APP_RESOURCE_PATH, APP_PROPERTIES_FNAME));
            Properties appProperties = new Properties();
            appProperties.load(props);
            this.appPropertiesMap.putAll(appProperties);
            props.close();
            props = null;
            
            Iterator e = this.appPropertiesMap.keySet().iterator();            
            this.spiderStartFile  = (String)e.next();
            String goldenFileName =
                (String) this.appPropertiesMap.get(this.spiderStartFile);
            if (debug) {
                DBG(nestingLevel, "----", "URL " + this.spiderStartFile +
                        ": testing  with " + goldenFileName);
            }            
            HtmlPage p = (HtmlPage)performTest(nestingLevel, null,
                    this.spiderStartFile,
                    goldenFileName);
            
            visitAnchors(p, nestingLevel, e);
        } finally {
            if (props != null) {
                props.close();
            }
        }
    }
    
    // spider 'write' mode
    @SuppressWarnings("unchecked")
    private void visitAnchors(int nestingLevel,
            HashSet<String> toBeVisitedLater,
            HtmlPage page, 
            DataOutputStream props) throws IOException {

        String pPrefix = page.getTitleText() + ":";
        List<HtmlAnchor> l = page.getAnchors();
        int index = 0;
        for (ListIterator i = l.listIterator(); i.hasNext(); index++) {
            HtmlAnchor a = (HtmlAnchor)i.next();
            String pFile = preprocessUsingPatterns(a.getHrefAttribute());
            // skip some references
            if (skipHref(pFile)) {
                continue;
            }
            // skip refs that will be visited from caller page
            if (skipHref(toBeVisitedLater, pFile)) {
                continue;
            }
            // skip refs that have been already visited
            if (skipHref(visited, pPrefix+pFile)) {
                continue;
            }
            if (debug) {
                DBG(nestingLevel, "+---", "Visiting: " + pPrefix+pFile);
            }
            // Click on anchor
            Page p = a.click();
            String contentType = p.getWebResponse().getContentType();
            String pHtml = p.getWebResponse().getContentAsString();
            boolean writeGoldenData = false;
            if (contentType.startsWith("text") || "".equals(contentType)) {
                pHtml = preprocessUsingPatterns(pHtml);
                writeGoldenData = true;
            }
            // write data (and scenario) only for text content
            if (writeGoldenData) {
                writeData(nestingLevel, props, index, pFile, pHtml, 
                   ("".equals(contentType) ? ".html" :
                   ("." + contentType.substring(contentType.indexOf("/")+1))));
            }
            // wisit anchors on HtmlPage
            if (p instanceof HtmlPage) {                
                HashSet<String> toBeVisitedNew = toBeVisited(page);
                toBeVisitedNew.addAll(toBeVisitedLater);
                if (nestingLevel-1 > 0) {
                    // Visit anchors on retrieved HTML page
                    visitAnchors(nestingLevel-1, toBeVisitedNew,
                            (HtmlPage)p, props);
                }
            } else {
                if (debug) {
                    DBG(nestingLevel,
                            "|   ", "*************" + p.getClass().getName());
                }
            }
            this.visited.add(pPrefix+pFile);
        }
        String propsString =
            this.stepCounter + ACTION_BREAK_LEVEL + " :" +
            (SPIDER_NESTING_LEVEL-nestingLevel);
        this.stepCounter += 10;
        props.writeBytes(propsString + "\n");
        if (debug) {
            DBG(nestingLevel, "|   ", propsString);
        }
    }
    
    // test 'read' mode
    private void visitAnchors(HtmlPage page, int nestingLevel,
            Iterator e) throws SAXException, IOException {
        for (; e.hasNext();) {
            String anchorKey  = (String)e.next();
            if (anchorKey.indexOf(ACTION_BREAK_LEVEL) != -1) {
                if (debug) {
                    DBG(nestingLevel, "+---", "Leaving page " +
                            page.getTitleText());
                }
                break;
            }
            String goldenFileName =
                (String) this.appPropertiesMap.get(anchorKey);
            if (debug) {
                DBG(nestingLevel, "+---", "Page " + page.getTitleText() +
                        ": testing " + anchorKey + " with " + goldenFileName);
            }
            Page p = performTest(nestingLevel, page, anchorKey, goldenFileName);
            if (nestingLevel-1 > 0) {
                if (p instanceof HtmlPage) {
                    // Visit anchors on current HTML page
                    visitAnchors((HtmlPage)p, nestingLevel-1, e);
                } else {
                    if (debug) {
                        DBG(nestingLevel, "|   ", "*************" +
                                p.getClass().getName());
                    }
                }
            }
        }
    }
    
    private void writeData(int nestingLevel, DataOutputStream props,
            int anchorIndex, String fileOnHost, String html, String ext)
    throws IOException {
        DataOutputStream goldenStream = null;
        try {
            String goldenFileName = "file" + html.hashCode() + ext;
            File goldenFile = 
                new File(this.APP_RESOURCE_PATH, goldenFileName);
            if (!goldenFile.exists()) {
                goldenStream =
                    new DataOutputStream(new FileOutputStream(goldenFile));
                goldenStream.writeBytes(html);
            }

            String propsString = String.valueOf(this.stepCounter);
            if (anchorIndex == URL_FLAG) {
                propsString += (".URL." + fileOnHost);
            } else {
                propsString += (".ANCHOR." + anchorIndex);
            }
            propsString += (" :" + goldenFileName);
            props.writeBytes(propsString + "\n");

            this.visited.add(fileOnHost);
            
            this.stepCounter += 10;
            if (debug) {
                DBG(nestingLevel, "|   ", propsString);
            }
        } finally {
            if (goldenStream != null) {
                goldenStream.close();
            }
        }
    }
    
    private String preprocessUsingPatterns(String html) {
        Set keys = this.appPatternsMap.keySet();
        for (Iterator e = keys.iterator(); e.hasNext();) {
            String pattern = (String)e.next();
            String replacement = (String) this.appPatternsMap.get(pattern);
            pattern = pattern.substring(pattern.indexOf(".") + 1);
            html = html.replaceAll(pattern, replacement);
        }
        return html;
    }
    
    private Page performTest(int nestingLevel, Page currentResponse,
            String remoteFileKey,
            String localFileName) throws SAXException, IOException {
        String[] action = remoteFileKey.split("\\.", 3);
        // parse key, issue request and get response from server
        Page result =
            parseActionAndGetResponse(nestingLevel, currentResponse, action);
        if (!this.noCheckMode) {
            Assert.assertNotNull("could not perform action: '" +
                    remoteFileKey + "'", result);
            // compare current response against golden file
            String pHtml = result.getWebResponse().getContentAsString();
                pHtml = preprocessUsingPatterns(pHtml);
            checkResponse(pHtml, localFileName, false);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Page parseActionAndGetResponse(int nestingLevel,
            Page currentResponse, String[] action)
    throws SAXException, IOException {
        if (ACTION_URL.equals(action[1])) {
            return this.wc.getPage(new URL(this.baseUrl, action[2]));
        }
        Assert.assertNotNull("wrong action for seq # " + action[0],
                currentResponse);
        Assert.assertTrue("wrong response type: " +
                currentResponse.getClass().getName(),
                currentResponse instanceof HtmlPage);
        int index = -1;
        if (ACTION_ANCHOR.equals(action[1])) {
            // find anchor by it's index
            // and click it
            String[] linkDesc = action[2].split("\\.", 2);
            List<HtmlAnchor> l = ((HtmlPage)currentResponse).getAnchors();
            index = Integer.parseInt(linkDesc[0]);
            Assert.assertTrue("no anchor with index: '" + index + "'",
                    index < l.size());
            return l.get(index).click();
        }
        else if (ACTION_FORM.equals(action[1])) {
            // find form by it's index,
            // set specified form parameters
            // and click dubmit specified
            String[] formDesc = action[2].split("\\.");
            index = Integer.parseInt(formDesc[0]);
            List<HtmlForm> f = ((HtmlPage)currentResponse).getForms();
            Assert.assertTrue("no form with index: '" + index + "'",
                    index < f.size());
            HtmlForm myForm = (HtmlForm)f.get(index);
            for (int i=1; i<formDesc.length-2; i+=2) {
                if (debug) {
                    DBG(nestingLevel, "+---", formDesc[i] +
                            "->" + formDesc[i+1]);
                }
                myForm.getInputByName(
                        formDesc[i]).setValueAttribute(formDesc[i+1]);
            }
            return (HtmlPage)myForm.submit(myForm.getInputByValue(
                    formDesc[formDesc.length-1]));
        }
        else if (ACTION_ELEMENT.equals(action[1])) {
            String[] elemDesc = action[2].split("\\.");
            index = Integer.parseInt(elemDesc[1]);
            List<HtmlElement> e =
                ((HtmlPage)currentResponse).getDocumentElement().
                getHtmlElementsByTagName(elemDesc[0]);
            if (debug) {
                for (ListIterator i = e.listIterator(); i.hasNext();) {
                    DBG(nestingLevel, "+---", "E: " + (HtmlElement)i.next());
                }
            }
            Assert.assertTrue("no HTML element with index: '" + index +
                    "' and tag: '" + elemDesc[0] + "'", index < e.size());
            HtmlElement he = (HtmlElement)e.get(index);
//          if (elemDesc.length == 3) {
//          System.out.println("EH: " + elemDesc[2]);
//          // execute specified event handler
//          System.out.println("SO: " + he.getScriptObject().getClass().getName());
//          HTMLInputElement s = (HTMLInputElement)he.getScriptObject();
//          s.jsxFunction_click();
//          ;
//          Function func = he.getEventHandler(elemDesc[2]);
//          // DEBUG
//          System.out.println("F: " + func);
//          return (HtmlPage)((ScriptResult)wc.getScriptEngine().callFunction(currentResponse,
//          func, this, new Object[]{"test"}, he)).getNewPage();
//          return null;
//          } else {
            Assert.assertTrue("HTML element '" + he +
                    "' is not clickable", he instanceof ClickableElement);
//          if (he instanceof HtmlInput) {
//          if (elemDesc.length == 3) {
//          HTMLInputElement s = (HTMLInputElement)he.getScriptObject();
//          s.
//          return (HtmlPage)((HtmlInput)he).setValueAttribute(elemDesc[2]);
//          }
//          }
            return (HtmlPage)((ClickableElement)he).click();
        }
        return null;
    }
    
    private void checkResponse(String resp,
            String goldenFileName, boolean binary) throws IOException {
        DataInputStream localStream = null;
        try {
            boolean failed = false;
            File localFile = new File(this.APP_RESOURCE_PATH, goldenFileName);
            byte[] fileBuf = new byte[(int)localFile.length()];
            localStream = new DataInputStream(new FileInputStream(localFile));
            localStream.readFully(fileBuf);
            String expected = new String(fileBuf);
            expected = preprocessUsingPatterns(expected);
            // compare as byte arrays
            if (Arrays.equals(resp.getBytes(), expected.getBytes())) {
                return;
            }
            // Compare line by line
            String[] localLines = fixEOL(expected).split("\\n");
            String[] remoteLines = fixEOL(resp).split("\\n");
            String failureMessage = "";
            if (localLines.length != remoteLines.length) {
                failed = true;
                failureMessage =
                    "Lines number mismatch: local=" + localLines.length +
                    ", remote=" + remoteLines.length;
            } else {
                // will use the following sets in case
                // we have the same set of lines but in
                // different order
                Set<String> remoteLinesSet = new HashSet<String>();
                Set<String> localLinesSet = new HashSet<String>();
                for (int i=0, lineNumber = 1; i<localLines.length; i++, lineNumber++) {
                    if (!remoteLines[i].equals(localLines[i])) {
                        // check if we have lines containing hrefs with different
                        // order of the same parameters in the query string
                        if (!hrefsCompare(remoteLines[i], localLines[i])) {
                            // add only different lines for future comparison
                            remoteLinesSet.add(remoteLines[i]);
                            localLinesSet.add(localLines[i]);
                            // prepare failure message anyway
                            failed = true;
                            String currentFailureMessage =
                                goldenFileName + ": Lines " + lineNumber +
                                " don't match:\n" + "Remote: \"" + remoteLines[i] +
                                "\"\n Local: \"" + localLines[i] + "\"\n";
                            System.out.print(currentFailureMessage);
                            failureMessage =
                                failureMessage.concat(currentFailureMessage);
                        }
                    }
                }
                // last chance
                if (failed && remoteLinesSet.equals(localLinesSet)) {
                    // ok - we have the same generated HTML
                    // but with different lines order
                    System.out.println("OK - the same lines set but in different order");
                    failed = false;
                }
            }
            // check that there was no lines mismatches
	    if (failed) {
		System.out.println(failureMessage);
	    }
            Assert.assertFalse(failureMessage, failed);
        } finally {
            if (localStream != null) {
                localStream.close();
            }
        }
    }
    
    private String fixEOL(String text) {
        String fixed = text;
        if (fixed.indexOf("\r\n") >= 0) {
            fixed = fixed.replaceAll("\\r\\n", "\n");
        }
        if (fixed.indexOf("\r") >= 0) {
            fixed = fixed.replaceAll("\\r", "\n");
        }
        return fixed;
    }
    
    /**
     * Compares lines containing href values allowing different order
     * of the same parameters in href's query string
     *
     * @param remote - line of HTML response from remote host
     *  presumably containing hrefs
     * @param local  - "golden" line probably containing hrefs
     * @return true if lines equal
     */
    private boolean hrefsCompare(String remote, String local) {
        if (remote.length() != local.length()) {
            return false;
        }
        String r = remote.replaceAll("href=\".*\"", "href=\"\"");
        String l = local.replaceAll("href=\".*\"", "href=\"\"");
        if (!r.equals(l)) {
            return false;
        }
        remote = remote.replaceAll("&amp;", "&");
        local = local.replaceAll("&amp;", "&");
        int startIndex = 0, index = -1;
        while ((index=remote.indexOf("href=", startIndex)) != -1) {
            startIndex = remote.indexOf("\"", index+6);
            String rHref = remote.substring(index+6, startIndex);
            String[] rHrefParts = rHref.split("\\?", 2);
            String[] rQueryParts = rHrefParts[1].split("&");
            String lHref = local.substring(index+6, startIndex);
            if (lHref.indexOf(rHrefParts[0]) == -1 ) {
                return false;
            }
            for (int i=0; i<rQueryParts.length; i++) {
                if (lHref.indexOf(rQueryParts[i]) == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> toBeVisited(HtmlPage page) {
        HashSet<String> ret = new HashSet<String>();
        List<HtmlAnchor> l = page.getAnchors();
        for (ListIterator i = l.listIterator(); i.hasNext();) {
            HtmlAnchor a = (HtmlAnchor)i.next();
            String pFile = preprocessUsingPatterns(a.getHrefAttribute());
            // skip external references
            if (skipHref(pFile)) {
                continue;
            }
            ret.add(pFile);
        }
        return ret;
    }

    private boolean skipHref(String href) {
        if (href.startsWith(this.spiderStartFile) ||
            href.startsWith("http://") ||
            href.startsWith(".."))
        {
            return true;
        }
        return false;
    }

    private boolean skipHref(HashSet<String> skipSet, String href) {
        Iterator<String> i = skipSet.iterator();
        for (;i.hasNext();) {
            String hr = (String) i.next();
            if (href.startsWith(hr)) {
                return true;
            }
        }
        return false;
    }

    private void DBG(int tn, String prefix, String msg) {
        tn = this.SPIDER_NESTING_LEVEL-tn;
        while(tn-->0) {
            System.out.print("|   ");
        }
        System.out.println(prefix + msg);
    }
}
