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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.microemu.log.Logger;


/**
 * @author vlads
 * 
 * This class allow redirection of stdout and stderr from MIDlet to MicroEmulator logger console
 * 
 */
public class MIDletOutputStreamRedirector extends PrintStream {

	public final static PrintStream out = outPrintStream();

	public final static PrintStream err = errPrintStream();

	static {
		Logger.addLogOrigin(MIDletOutputStreamRedirector.class);
		Logger.addLogOrigin(OutputStream2Log.class);
	}
	
	private static class OutputStream2Log extends OutputStream {

		boolean isErrorStream;
		
		StringBuffer buffer = new StringBuffer();

		OutputStream2Log(boolean error) {
			this.isErrorStream = error;
		}

		public void write(int b) throws IOException {
			if ((b == '\n') || (b == '\r')) {
				if (buffer.length() > 0) {
					if (isErrorStream) {
						Logger.error(buffer.toString());
					} else {
						Logger.info(buffer.toString());
					}
					buffer = new StringBuffer();
				}
			} else {
				buffer.append((char) b);
			}
		}

	}

	private MIDletOutputStreamRedirector(boolean error) {
		super(new OutputStream2Log(error));
	}

	private static PrintStream outPrintStream() {
		return new MIDletOutputStreamRedirector(false);
	}

	private static PrintStream errPrintStream() {
		return new MIDletOutputStreamRedirector(true);
	}

	//Override methods to be able to get proper stack trace

	public void print(boolean b) {
		super.print(b);
	}

	public void print(char c) {
		super.print(c);
	}

	public void print(char[] s) {
		super.print(s);
	}

	public void print(double d) {
		super.print(d);
	}

	public void print(float f) {
		super.print(f);
	}

	public void print(int i) {
		super.print(i);
	}

	public void print(long l) {
		super.print(l);
	}

	public void print(Object obj) {
		super.print(obj);
	}

	public void print(String s) {
		super.print(s);
	}

	public void println() {
		super.println();
	}

	public void println(boolean x) {
		super.println(x);
	}

	public void println(char x) {
		super.println(x);
	}

	public void println(char[] x) {
		super.println(x);
	}

	public void println(double x) {
		super.println(x);
	}

	public void println(float x) {
		super.println(x);
	}

	public void println(int x) {
		super.println(x);
	}

	public void println(long x) {
		super.println(x);
	}

	public void println(Object x) {
		super.println(x);
	}

	public void println(String x) {
		super.println(x);
	}

	public void write(byte[] buf, int off, int len) {
		super.write(buf, off, len);
	}

	public void write(int b) {
		super.write(b);
	}
	
}
