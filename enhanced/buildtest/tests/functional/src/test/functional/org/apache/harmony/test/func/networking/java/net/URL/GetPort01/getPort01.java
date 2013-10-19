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
/** 
 */  
/*
 * Created on 16.11.2004
 *
 */
package org.apache.harmony.test.func.networking.java.net.URL.GetPort01;

import java.net.*;

import org.apache.harmony.test.func.networking.java.net.share.URLTestFramework;

/**
 * 
 */

public class getPort01 extends URLTestFramework {

    public int test () {
        URL url = null;
        String urlString = getValidURLString();
        String urlProtocol = getValidProtocol();
        String urlHost = getValidHost();
        String urlFile = getValidFile();
        int defaultPort = -1;
        int urlPortReturned;
        
        try {
            url = new URL (urlString);
        }
        catch (MalformedURLException e) {
            return (fail("can't create URL: " + e.getMessage()));
        }
        
        urlPortReturned = url.getPort();
        
        try {
            url = new URL (urlProtocol, urlHost, urlFile);
        }    
        catch (MalformedURLException e) {
            return (fail("can't create URL: " + e.getMessage()));
        }
        
        urlPortReturned = url.getPort();
        
        if (urlPortReturned == defaultPort) {
            return (pass(url.toString()));
        }
        else {
            return (fail ("getPort() returns wrong result: " + urlPortReturned 
                    + "; should be: " + defaultPort));
        }
    }
    
    public static void main (String[] args) {
        System.exit(new getPort01().test(args));
    }
}
