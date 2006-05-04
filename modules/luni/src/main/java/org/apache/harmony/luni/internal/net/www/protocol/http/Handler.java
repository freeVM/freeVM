/* Copyright 1998, 2006 The Apache Software Foundation or its licensors, as applicable
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.internal.net.www.protocol.http;


import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.harmony.luni.util.Msg;

/**
 * This is the handler that manages all transactions between the client and a HTTP remote server.
 *
 */
public class Handler extends URLStreamHandler {

/**
 * Answers a connection to the HTTP server specified by this <code>URL</code>.
 *
 * @param 		u 		the URL to which the connection is pointing to
 * @return 		a connection to the resource pointed by this url.
 *
 * @thows		IOException 	if this handler fails to establish a connection
 */
protected URLConnection openConnection(URL u) throws IOException {
	return new HttpURLConnection(u, getDefaultPort());
}

/**
 * Answers a connection, which is established via the <code>proxy</code>,
 * to the HTTP server specified by this <code>URL</code>. If the 
 * <code>proxy</code> is DIRECT type, the connection is made in normal way.
 *
 * @param 		u 		the URL which the connection is pointing to
 * @param		proxy	the proxy which is used to make the connection
 * @return 		a connection to the resource pointed by this url.
 *
 * @throws		IOException
 *                  if this handler fails to establish a connection.
 * @throws 		IllegalArgumentException	
 *                  if any argument is null or the type of proxy is wrong.
 * @throws 		UnsupportedOperationException	
 *                  if the protocol handler doesn't support this method.
 */
protected URLConnection openConnection(URL u, Proxy proxy) throws IOException {
	if(null == u || null == proxy){
		throw new IllegalArgumentException(Msg.getString("K034b"));
	}
	return new HttpURLConnection(u, getDefaultPort(), proxy);
}

/**
 * Return the default port.
 */
protected int getDefaultPort() {
	return 80;
}
}
