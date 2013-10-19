/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

public class ResURLConnection extends URLConnection {
	
	private static final String PREFIX = "res:";

	private Hashtable entries;
	
	protected ResURLConnection(URL url, Hashtable entries) {
		super(url);
		
		this.entries = entries;
	}

	public void connect() throws IOException {
	}

	public InputStream getInputStream() throws IOException {
		String location = url.toString();
		int idx = location.indexOf(PREFIX);
		if (idx == -1) {
			throw new IOException();
		}
		location = location.substring(idx + PREFIX.length());
		byte[] data = (byte[]) entries.get(location);
		if (data == null) {
			throw new IOException();
		}
		return new ByteArrayInputStream(data);
	}
	
}
