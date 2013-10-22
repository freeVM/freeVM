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
 * Created on 25.11.2004
 *
 */
package org.apache.harmony.test.func.networking.java.net.share;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 *
 */
public class MyURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /* (non-Javadoc)
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    public URLStreamHandler createURLStreamHandler(String arg0) {
        if (arg0.equals("net-test")) {
            return new MyURLStreamHandler();
        }
        if (arg0.equals("test-net")) {
            return new MyURLStreamHandler2();
        }
        else {
            return null;
        }
    }
}
