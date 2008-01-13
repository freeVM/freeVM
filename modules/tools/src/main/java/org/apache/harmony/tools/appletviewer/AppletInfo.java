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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JLabel;

public class AppletInfo {
    private static final int DEFAULT_WIDTH=300;
    private static final int DEFAULT_HEIGHT=200;    
    
    private URL documentBase;
    private URL codeBase;
    private URL archive;
    private String code;
    private int width;
    private int height;
    private HashMap<String, String> params;
    private JLabel statusLabel = null;
    
    public AppletInfo() {
        params = new HashMap<String, String>();
    }

    public URL getDocumentBase() {
        return documentBase;
    }

    public void setDocumentBase(URL documentBase) {
        this.documentBase = documentBase;
    }

    public URL getCodeBase() {
        return codeBase;
    }

    public void setCodeBase(URL codeBase) {
        this.codeBase = codeBase;
    }

    public void setCodeBase(String codeBaseStr) throws MalformedURLException {
        this.codeBase = new URL(this.documentBase, (codeBaseStr == null)?"./":codeBaseStr);
    }

    public URL getArchive() {
        return archive;
    }

    public void setArchive(URL archive) {
        this.archive = archive;
    }

    public void setArchive(String archive) throws MalformedURLException {
        this.archive = (archive == null)?null:new URL(this.documentBase, archive);
    }

    public String getParameter(String name) {
        return params.get(name.toUpperCase());
    }

    public void setParameter(String name, String value) {
        params.put(name.toUpperCase(), value);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = (code.endsWith(".class"))?code.substring(0, code.length()-6):code;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setWidth(String widthStr) {     
        this.width = (widthStr == null)?DEFAULT_WIDTH:Integer.parseInt(widthStr);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }   

    public void setHeight(String heightStr) {       
        this.height = (heightStr == null)?DEFAULT_HEIGHT:Integer.parseInt(heightStr);
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel  = statusLabel;
    }

    public void setStatus(String text) {
        if (statusLabel != null)
            statusLabel.setText(text);
    }
    
    public URL []getClassLoaderURLs() {
    	URL []res = (archive == null)?new URL[1]:new URL[2];
    	switch (res.length) {
    		case 2: res[1] = archive;
    		case 1: res[0] = codeBase;
    	}
    	return res;
    }
}
