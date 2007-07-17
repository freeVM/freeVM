/*
 * Copyright 2007 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Aleksey Ignatenko
 * @version $Revision: 1.0 $
 */

package org.apache.harmony.test.reliability.api.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.harmony.test.reliability.share.Test;

/*
 * Goal: Test HttpURLConnection functionality
 *
 *  passed parameters:
 *  parameter[0] - urls for web pages you want to get, all urls are to be listed consequently
 *  via ";" with no blanks or tabs
 *  E.g http://harmony.apache.org/;http://issues.apache.org/jira/browse/HARMONY
 *  parameter[1] - proxy adress if any   
 *  parameter[2] - proxy port if any
 *  The test does the following:
 *    - For every url from parameter[0] establishes HttpURLConnection and tries to GET
 *    web page from it and read some info about connection. 
 *   
 *  No crash, hang or fail is excpected.
 *  
 *  Note: All input parameters are to be set properly as test could fail due to wrong 
 *  set proxy or Url address.
 *  */

public class HttpConnectionTest extends Test{
    String[] testedUrlStrings = null;
    Proxy prx = null;

    public static void main(String[] params){
        System.exit(new HttpConnectionTest().test(params));
    }
    
    public int test(String[] params){
        parseParams(params);
        if (testedUrlStrings == null){
            return fail("Urls to test are not set");
        }

        for (int i=0; i<testedUrlStrings.length;i++){
            URL url = null;            
            try {
                url = new URL(testedUrlStrings[i]);
            } catch (MalformedURLException e2) {
                return fail("Failed to create URL of " + testedUrlStrings[i]);
            }
            
            if (!checkUrl(url)){
                return fail("FAILED");
            }
        }
        
        return pass("OK");
    }
    
    boolean checkUrl(URL url){
        HttpURLConnection hur = null;
        try {
            if (prx == null){
                hur = (HttpURLConnection)url.openConnection();
            }else{
                hur = (HttpURLConnection)url.openConnection(prx);
            }
        } catch (IOException e) {
            log.add("Failed to open connection of " + url);
            return false;
        }
        
        BufferedReader br = null; 
        try {
            hur.setDoOutput(true);
            hur.connect();
            // get some info about http connection
            hur.getFollowRedirects();
            hur.getInstanceFollowRedirects();
            hur.getPermission();
            hur.getRequestMethod();
            hur.getResponseCode();
            hur.usingProxy();
            
            // get html page and scan it
            br = new BufferedReader(new InputStreamReader(hur.getInputStream()));
            String resultedString = "";
            String buf;
            while((buf = br.readLine()) != null){
                resultedString += buf;
            }
            //System.out.println(resultedString);
            hur.disconnect();
            
        } catch (IOException e) {
            log.add("Failed to connect to " + hur.getURL() + " : " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    public void parseParams(String[] params) {
        if (params.length >= 1) {
            String urls = params[0];
            Pattern p = Pattern.compile(";");
            testedUrlStrings = p.split(urls);
        }
        if (params.length >= 3) {
            prx = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(params[1], Integer.parseInt(params[2])));
        }
        
    }
}
