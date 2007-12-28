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

package org.apache.harmony.tools.appletviewer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class HTMLParser {
	HTMLParser() {
	}
	
	Object []parse(String urls[], int start) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		ArrayList<AppletInfo> list = new ArrayList<AppletInfo>(urls.length-start);
		for (int i = start; i < urls.length; i++) {
			AppletHTMLHandler handler = new AppletHTMLHandler(urls[i], list);
			parser.parse(urls[i], handler);
		}
		return list.toArray();
	}

	private class AppletHTMLHandler extends DefaultHandler {
		private URL documentBase;
		private final ArrayList<AppletInfo> list;
		private AppletInfo appletInfo = null;
		private String startElement = null;
		
		public AppletHTMLHandler(String url, ArrayList<AppletInfo> list) throws MalformedURLException {
			super();
			
			// String could represent file path or URL
			try  {
				this.documentBase = new URL(url);
			} catch (MalformedURLException _) {
				File f = new File(url);
				this.documentBase = f.toURL();
			}
			this.list = list;
		}

		public void startElement(String uri, String lName, String qName, Attributes attrs) throws SAXException {
			if (qName.equalsIgnoreCase("APPLET") || qName.equalsIgnoreCase("OBJECT")) {
				if (startElement != null) {
					throw new SAXParseException(qName+" inside "+startElement, null);
				}
				
				startElement = qName;
				appletInfo = new AppletInfo();
				appletInfo.setDocumentBase(documentBase);
				appletInfo.setCode(attrs.getValue("code"));
				try {
					appletInfo.setCodeBase(attrs.getValue("codebase"));
				} catch (Exception e) {
					appletInfo.setCodeBase((URL)null);
				}
				appletInfo.setWidth(attrs.getValue("width"));
				appletInfo.setHeight(attrs.getValue("height"));
				list.add(appletInfo);
				return;
			}
			
			if (appletInfo != null && qName.equalsIgnoreCase("PARAM")) {
				appletInfo.setParameter(attrs.getValue("name"), attrs.getValue("value"));
			}
		}
		
		public void endElement(String uri, String lName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase(startElement))
				startElement = null;
		}
	}
}
