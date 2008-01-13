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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.TagElement;

class HTMLParser {
	private final ParserImpl parser;
	
    HTMLParser() throws IOException {
        DTD dtd = DTD.getDTD("reader");
        dtd.read(new DataInputStream(dtd.getClass().getResourceAsStream("html32.bdtd")));//transitional401.bdtd")));
        parser = new ParserImpl(dtd);
    }
    
    synchronized Object []parse(String urls[], int start) throws IOException {    	
        ArrayList<AppletInfo> list = new ArrayList<AppletInfo>(urls.length-start);
        for (int i = start; i < urls.length; i++) {
            parser.parse(urls[i], list);
        }
        return list.toArray();
    }
    
    private class ParserImpl extends javax.swing.text.html.parser.Parser {
    	private URL documentBase;
		private ArrayList<AppletInfo> list;
        private AppletInfo appletInfo = null;
        private HTML.Tag startElement = null;
    	
    	public ParserImpl(DTD dtd) {
    		super(dtd);
		}
    	
    	public void parse(String url, ArrayList<AppletInfo> list) throws IOException {
            try  {
                this.documentBase = new URL(url);
            } catch (MalformedURLException _) {
                File f = new File(url);
                this.documentBase = f.toURL();
            }
            this.list = list;
            
            // Open the stream
            InputStreamReader isr = new InputStreamReader(documentBase.openStream());
            parse(isr);
    	}

		@Override
		protected void handleStartTag(TagElement tag) {
			if (tag == null)
				return;
			HTML.Tag htmlTag = tag.getHTMLTag();
            if (htmlTag == HTML.Tag.APPLET || htmlTag == HTML.Tag.OBJECT) {
                if (startElement != null) {
                    throw new RuntimeException(htmlTag+" inside "+startElement);
                }
                
                startElement = htmlTag;
                appletInfo = new AppletInfo();
                appletInfo.setDocumentBase(documentBase);
                SimpleAttributeSet attributes = getAttributes();
                appletInfo.setCode((String)attributes.getAttribute(HTML.Attribute.CODE));
                try {
					appletInfo.setCodeBase((String)attributes.getAttribute(HTML.Attribute.CODEBASE));
				} catch (Exception e) {
					appletInfo.setCodeBase((URL)null);
				}
                appletInfo.setWidth((String)attributes.getAttribute(HTML.Attribute.WIDTH));
                appletInfo.setHeight((String)attributes.getAttribute(HTML.Attribute.HEIGHT));
                list.add(appletInfo);
                return;
            }            

            if (appletInfo != null && htmlTag == HTML.Tag.PARAM) {
                SimpleAttributeSet attributes = getAttributes();
                appletInfo.setParameter((String)attributes.getAttribute(HTML.Attribute.NAME), (String)attributes.getAttribute(HTML.Attribute.VALUE));
            }
		}

		@Override
		protected void handleEndTag(TagElement tag) {
			if (tag != null && tag.getHTMLTag() == startElement)
                startElement = null;
		}

		@Override
		protected void handleEmptyTag(TagElement tag)
				throws ChangedCharSetException {
			HTML.Tag htmlTag = tag.getHTMLTag();
            if (appletInfo != null && htmlTag == HTML.Tag.PARAM) {
                SimpleAttributeSet attributes = getAttributes();
                appletInfo.setParameter((String)attributes.getAttribute(HTML.Attribute.NAME), (String)attributes.getAttribute(HTML.Attribute.VALUE));
            }
		}
    }
}
