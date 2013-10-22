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
package org.apache.harmony.eut.extractor;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.io.File;
import java.io.FileReader;

/**
 * Parses test.xml file to collect available suite lists.
 */
public final class EUTSuiteExtractor extends DefaultHandler {

    /** Keeps the name of ant property the suite list is assigned to. */
    private static final String SUITE_PROPERTIES_NAME = "tests.list";

    /** True if "all" target is being parsed. */
    private boolean inAllTarget;

    /** True if first suite is printed and comma separator is required. */
    private boolean needComma;

    /** Called by parser when new XML tag is found. */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (!inAllTarget && qName.equals("target") &&
                attributes.getValue("name").equals("all")) {
            inAllTarget = true;
            System.out.print(SUITE_PROPERTIES_NAME + "=");
            return;
        }

        if (inAllTarget && qName.equals("antcall")) {
            if (!needComma) {
                needComma = true;
            } else {
                System.out.print(",");
            }
            System.out.print(attributes.getValue("target"));
            return;
        }
    }

    /** Called by parser when XML tag processing is being completed. */
     public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (inAllTarget && qName.equals("target")) {
            inAllTarget = false;
            System.out.println();
        }
    }

    /** Parses test.xml file to collect available suite lists. */
    public static void main(String[] args) throws Exception {
        FileReader testXML = new FileReader(new File(args[0]));
        XMLReader xr = XMLReaderFactory.createXMLReader();
        DefaultHandler handler = new EUTSuiteExtractor();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        xr.parse(new InputSource(testXML));
        testXML.close();
    }
} // end of class 'EUTSuiteExtractor' definition
