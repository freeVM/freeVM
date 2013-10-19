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

package org.microemu.cldc.socket;

import java.io.IOException;

import org.microemu.cldc.ClosedConnection;

public class Connection implements ClosedConnection {

	public javax.microedition.io.Connection open(String name) throws IOException {

		if (!org.microemu.cldc.http.Connection.isAllowNetworkConnection()) {
			throw new IOException("No network");
		}

		int portSepIndex = name.lastIndexOf(':');
		int port = Integer.parseInt(name.substring(portSepIndex + 1));
		String host = name.substring("socket://".length(), portSepIndex);

		if (host.length() > 0) {
			return new SocketConnection(host, port);
		} else {
			return new ServerSocketConnection(port);
		}
	}

	public void close() throws IOException {
		// Implemented in SocketConnection or ServerSocketConnection
	}

}
