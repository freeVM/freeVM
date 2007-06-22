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

package org.apache.harmony.test.jetty;

import com.gargoylesoftware.htmlunit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Vector;

import junit.framework.TestCase;

public class JettyTestCase extends TestCase {

    protected static final String proxyHost = System.getProperty("http.proxy.host", null);
    protected static final int    proxyPort = Integer.valueOf(System.getProperty("http.proxy.port", "0")).intValue();

    protected static String scenarioRoot;
    protected static WebClient webClient;

    protected static final String resourcePath = System.getProperty("jetty.scenario.resources.path", "resources");
    protected static final String outputPath   = System.getProperty("jetty.scenario.results.path", "results");
    protected static final String savePages    = System.getProperty("jetty.scenario.savepages", "no");

    protected static PrintStream LOG_OUT = System.out;
    protected static PrintStream LOG_ERR = System.err;

    protected static final int SERVER_START_TIMEOUT = Integer.valueOf(
        System.getProperty("jetty.server.startup.timeout", "60")).intValue() * 1000;

    static {
        scenarioRoot = ".";
        if (proxyHost != null) {
            webClient = new WebClient(BrowserVersion.FULL_FEATURED_BROWSER, proxyHost, proxyPort);
        } else {
            webClient = new WebClient();
        }
        LOG_OUT.println("Setup completed.");
    }

    protected Page downloadPage(String link) {
        Page page = null;
        URL url = null;
        try {
            url = new URL(link);
            LOG_OUT.println("Loading page from server: " + link);
            page = webClient.getPage(url);
        } catch (MalformedURLException mue) {
            LOG_OUT.println("donwloadPage(): can't create URL for \"" + link + "\": " + mue);
            mue.printStackTrace(LOG_OUT);
            return null;
        } catch (IOException ioe1) {
            LOG_OUT.println("Looks like server is not started yet, waiting for timeout...");
            try {
                Thread.sleep(SERVER_START_TIMEOUT);
                page = webClient.getPage(url);
            } catch (IOException ioe2) {
                LOG_OUT.println("donwloadPage(): can't download URL \"" + link + "\": " + ioe2);
                ioe2.printStackTrace(LOG_OUT);
                return null;
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        return page;
    }

    protected boolean comparePageContent(String content, File etalon) {

        LOG_OUT.println("Comparing page content with etalon: " + etalon.getName());

        LineNumberReader creader = new LineNumberReader(new StringReader(content));
        LineNumberReader freader = null;
        try {
            freader = new LineNumberReader(new FileReader(etalon));
        } catch (FileNotFoundException fnfe) {
            LOG_OUT.println("File \"" + etalon.getName() + "\" not found: " + fnfe);
            fnfe.printStackTrace(LOG_OUT);
            return false;
        }

        String cs = null;
        String fs = null;
        Vector cv = new Vector();
        Vector fv = new Vector();
        Vector sv = new Vector();
        try {
            cs = creader.readLine();
            fs = freader.readLine();
            while (cs != null && fs != null) {
                if (!compareLines(cs, fs)) {
                    cv.add(cs);
                    sv.add(fs);
                }
                fv.add(fs);
                cs = creader.readLine();
                fs = freader.readLine();
            }
        } catch (IOException ioe) {
            LOG_OUT.println("Exception while comparing page content: " + ioe);
            ioe.printStackTrace(LOG_OUT);
            return false;
        }

        if (cs != null || fs != null) {
            LOG_OUT.println("Page have different number of lines than etalon.");
            return false;
        }

        if (cv.size() != 0 || fv.size() != 0) {
            if (!compareRegardlessOrder(cv, sv, fv)) {
                LOG_OUT.println("Page differs from etalon (even if lines order is not checked).");
                return false;
            }
        }

        try {
            creader.close();
            freader.close();
        } catch (IOException ioe) {
            LOG_OUT.println("Exception while finishing comparision of page content: " + ioe);
            ioe.printStackTrace(LOG_OUT);
            return false;
        }
        LOG_OUT.println("Page is equal to etalon.");
        return true;
    }

    protected boolean compareRegardlessOrder(Vector first, Vector second, Vector third) {
        for (int i = 0; i < first.size(); i++) {
            boolean ok = false;
            for (int j = 0; j < third.size(); j++) {
                if (compareLines((String)first.elementAt(i), (String)third.elementAt(j))) {
                    ok = true;
                }
            }
            if (!ok) {
                LOG_OUT.println("Lines:");
                int linesCount = (first.size() > 20) ? 20 : first.size();
                for (int j = 0; j < linesCount; j++) {
                    LOG_OUT.println("--content-----------------------------------------------------------------------");
                    LOG_OUT.println((String)first.elementAt(j));
                    LOG_OUT.println("--etalon------------------------------------------------------------------------");
                    LOG_OUT.println((String)second.elementAt(j));
                }
                LOG_OUT.println("--------------------------------------------------------------------------------");
                if (first.size() > 20) {
                    LOG_OUT.println("and " + (first.size() - 20) + " lines more");
                }
                LOG_OUT.println("do not match each other.");
                return false;
            }
        }
        return true;
    }

    protected boolean compareLines(String first, String second) {
        if (second.startsWith("$$$")) {
            return first.matches(second.substring(3));
        }
        return first.equals(second);
    }

    protected File getResourceFile(String resourceName) {
        LOG_OUT.println("Getting resource with name: " + resourceName);
        String s = File.separator;
        String path = resourcePath + s + scenarioRoot + s + resourceName;
        File file = null;
        file = new File(path);
        assertTrue("Path \"" + path + "\" does not exist.", file.exists());
        assertTrue("\"" + path + "\" is not a file.", file.isFile());
        LOG_OUT.println("Found resource: " + path);
        return file;
    }

    protected void savePageContent(String name, String content) {
        if (!savePages.equals("yes")) {
            return;
        }
        String s = File.separator;
        String dname = outputPath + s + scenarioRoot;
        File dir = new File(dname);
        if (!dir.exists()) {
            assertTrue("Can't create dir for output files: \"" + dname + "\".", dir.mkdirs());
        }
        String fname = dname + s + name;
        LOG_OUT.println("Saving content of downloaded page to: " + fname);
        File file = new File(fname);
        if (!file.exists()) {
            try {
                assertTrue("Can't create output file: \"" + fname + "\".", file.createNewFile());
            } catch (IOException ioe) {
                LOG_OUT.println("Exception while creating \"" + fname + "\" output file: " + ioe);
                ioe.printStackTrace(LOG_OUT);
            }
        }
        assertTrue("Can't write to output file: \"" + fname + "\".", file.canWrite());
        PrintStream writer = null;
        try {
            writer = new PrintStream(file);
        } catch (FileNotFoundException fnfe) {
            LOG_OUT.println("File \"" + fname + "\" not found: " + fnfe);
            fnfe.printStackTrace(LOG_OUT);
        }
        writer.print(content);
        writer.close();
    }
}
