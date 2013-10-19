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
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class JettyScenario extends JettyTestCase {

    static {
        scenarioRoot = "JettyScenario";
    }

    public void testInitialPage() {
        LOG_OUT.println("\n==============================================================testInitialPage===");
        LOG_OUT.println("Starting initial page test case.");
        HtmlPage page = (HtmlPage)downloadPage("http://localhost:7070/test/");
        WebResponse wr = page.getWebResponse();
        String content = wr.getContentAsString();
        File etalon = getResourceFile("initial_page.html");
        savePageContent("initial_page.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Initial page test case passed.");
    }

    public void testStaticContent() {
        LOG_OUT.println("\n============================================================testStaticContent===");
        LOG_OUT.println("Starting static content page test case.");
        Page page = downloadPage("http://localhost:7070/test/data.txt");
        WebResponse wr = page.getWebResponse();
        String content = wr.getContentAsString();
        File etalon = getResourceFile("static_content.txt");
        savePageContent("static_content.txt", content);
        assertTrue("Content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Static content page test case passed.");
    }

    public void testSessionDumpServlet() {
        LOG_OUT.println("\n=======================================================testSessionDumpServlet===");
        LOG_OUT.println("Starting servlet test case.");
        HtmlPage page = (HtmlPage)downloadPage("http://localhost:7070/test/session/");
        WebResponse wr = page.getWebResponse();
        String content = wr.getContentAsString();
        File etalon = getResourceFile("session_dump.html");
        savePageContent("session_dump.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Session dump servlet page is OK.");

        // Test create new session

        HtmlForm[] forms = (HtmlForm[])page.getForms().toArray(new HtmlForm[0]);
        HtmlForm form = forms[0];
        try {
            page = (HtmlPage)form.submit("Action");
        } catch (IOException ioe) {
            LOG_OUT.println("Can't submit the form: " + ioe);
            ioe.printStackTrace(LOG_OUT);
        }
        wr = page.getWebResponse();
        content = wr.getContentAsString();
        etalon = getResourceFile("session_dump_new.html");
        savePageContent("session_dump_new.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Session create page is OK.");

        // Test set new value

        forms = (HtmlForm[])page.getForms().toArray(new HtmlForm[0]);
        form = forms[0];
        HtmlInput text1 = form.getInputByName("Name");
        text1.setValueAttribute("somename");
        HtmlInput text2 = form.getInputByName("Value");
        text2.setValueAttribute("somevalue");
        try {
            HtmlInput set = form.getInputByValue("Set");
            page = (HtmlPage)form.submit(set);
        } catch (IOException ioe) {
            LOG_OUT.println("Can't submit the form: " + ioe);
            ioe.printStackTrace(LOG_OUT);
        }
        wr = page.getWebResponse();
        content = wr.getContentAsString();
        etalon = getResourceFile("session_dump_set.html");
        savePageContent("session_dump_set.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Session set page is OK.");

        // Test remove value

        forms = (HtmlForm[])page.getForms().toArray(new HtmlForm[0]);
        form = forms[0];
        text1 = form.getInputByName("Name");
        text1.setValueAttribute("somename");
        try {
            HtmlInput remove = form.getInputByValue("Remove");
            page = (HtmlPage)form.submit(remove);
        } catch (IOException ioe) {
            LOG_OUT.println("Can't submit the form: " + ioe);
            ioe.printStackTrace(LOG_OUT);
        }
        wr = page.getWebResponse();
        content = wr.getContentAsString();
        etalon = getResourceFile("session_dump_remove.html");
        savePageContent("session_dump_remove.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Session remove page is OK.");

        // Test invalidate session

        forms = (HtmlForm[])page.getForms().toArray(new HtmlForm[0]);
        form = forms[0];
        try {
            HtmlInput invalidate = form.getInputByValue("Invalidate");
            page = (HtmlPage)form.submit(invalidate);
        } catch (IOException ioe) {
            LOG_OUT.println("Can't submit the form: " + ioe);
            ioe.printStackTrace(LOG_OUT);
        }
        wr = page.getWebResponse();
        content = wr.getContentAsString();
        etalon = getResourceFile("session_dump_invalidate.html");
        savePageContent("session_dump_invalidate.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Session invalidate page is OK.");

        LOG_OUT.println("Servlet test case passed.");
    }

    public void testRequestDumpJSP() {
        LOG_OUT.println("\n===========================================================testRequestDumpJSP===");
        LOG_OUT.println("Starting JSP test case.");
        HtmlPage page = (HtmlPage)downloadPage("http://localhost:7070/test/snoop.jsp");
        WebResponse wr = page.getWebResponse();
        String content = wr.getContentAsString();
        File etalon = getResourceFile("request_dump_JSP.html");
        savePageContent("request_dump_JSP.html", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("JSP test case passed.");
    }

    public void testGZippedStaticContent() {
        LOG_OUT.println("\n=====================================================testGZippedStaticContent===");
        LOG_OUT.println("Starting gzipped static content test case.");
        Page page = downloadPage("http://localhost:7070/test/data.txt.gz");
        WebResponse wr = page.getWebResponse();
        String content = new String();

        LOG_OUT.println("Unpacking content from archive.");
        try {
            InputStream contentStream = wr.getContentAsStream();
            GZIPInputStream gzip = new GZIPInputStream(contentStream);
            assertNotNull("Can't read from .gz file.", gzip);
            byte[] buf = new byte[10000];
            int total_bytes = 0;
            while (gzip.available() > 0) {
                int bytes = gzip.read(buf, 0, 10000);
                if (bytes > 0) {
                    content += new String(buf, 0, bytes);
                    total_bytes += bytes;
                }
            }
            gzip.close();
        } catch (IOException ioe) {
            LOG_OUT.println("Exception while unpacking gzip file: " + ioe);
            ioe.printStackTrace(LOG_OUT);
        }

        File etalon = getResourceFile("static_content_ungzipped.txt");
        savePageContent("static_content_ungzipped.txt", content);
        assertTrue("Generated content is not correct.", comparePageContent(content, etalon));
        LOG_OUT.println("Gzipped static content test case passed.");
    }
}
