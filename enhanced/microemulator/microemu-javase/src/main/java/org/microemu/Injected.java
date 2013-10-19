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

package org.microemu;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.microemu.app.util.MIDletOutputStreamRedirector;
import org.microemu.app.util.MIDletResourceLoader;
import org.microemu.app.util.MIDletSystemProperties;
import org.microemu.log.Logger;

/**
 * @author vlads
 *
 * This code is added to MIDlet application to solve problems with security policy  while running in Applet and Webstart.
 * Also solves resource resource loading paterns commonly used in MIDlet and not aceptable in Java SE application
 * The calls to this code is injected by ClassLoader or "Save for Web...".
 * 
 * This class is used instead injected one when application is running in Applet with MicroEmulator. 
 *
 * Serializable is just internal flag to verify tha proper class is loaded by application.
 */
public final class Injected implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * This allow redirection of stdout to MicroEmulator console
	 */
	public final static PrintStream out = outPrintStream();

	public final static PrintStream err = errPrintStream();

	static {
		Logger.addLogOrigin(Injected.class);
	}
	
	/**
	 * We don't need to instantiate the class, all access is static
	 */
	private Injected() {
		
	}
	
	private static PrintStream outPrintStream() {
		//return System.out;
		return MIDletOutputStreamRedirector.out;
	}

	private static PrintStream errPrintStream() {
		//return System.err;
		return MIDletOutputStreamRedirector.err;
	}
	
	/**
	 * Redirect throwable.printStackTrace() to MicroEmulator console
	 */
	public static void printStackTrace(Throwable t) {
		Logger.error("MIDlet caught", t);
	}
	
	/**
	 * This code Ingected By MicroEmulator to enable access to System properties while running in Applet
     *
     * @param      key   the name of the system property.
     * @return     the string value of the system property,
     *             or <code>null</code> if there is no property with that key.
	 */
	public static String getProperty(String key) {
		return MIDletSystemProperties.getProperty(key);
	}
	
	public static InputStream getResourceAsStream(Class origClass, String name)  {
		return MIDletResourceLoader.getResourceAsStream(origClass, name);
	}

	/**
	 * TODO fix ChangeCallsMethodVisitor
	 */
	public static Throwable handleCatchThrowable(Throwable t) {
		Logger.error("MIDlet caught", t);
		return t;
	}
}
