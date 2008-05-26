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

package org.microemu.log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * This class is used as abstraction layer for log messages Minimum Log4j
 * implemenation with multiple overloaded functions
 * 
 * @author vlads
 * 
 */
public class Logger {

	private static final String FQCN = Logger.class.getName();

	private static final Set fqcnSet = new HashSet();

	private static final Set logFunctionsSet = new HashSet();

	private static boolean java13 = false;
	
	private static boolean locationEnabled = true;

	private static List loggerAppenders = new Vector();

	static {
		fqcnSet.add(FQCN);
		// Message class can be moved to different sub project, See call to
		// addLogOrigin
		// Also Message calss can be refactored by ProGuard
		// fqcnSet.add("org.microemu.app.ui.Message");

		addAppender(new StdOutAppender());

		// This is done for MIDletInternlaLogger a wrapper for
		// System.out.println functions.
		logFunctionsSet.add("debug");
		logFunctionsSet.add("log");
		logFunctionsSet.add("error");
		logFunctionsSet.add("fatal");
		logFunctionsSet.add("info");
		logFunctionsSet.add("warn");
	}

	public static boolean isDebugEnabled() {
		return true;
	}

	public static boolean isErrorEnabled() {
		return true;
	}
	
	public static boolean isLocationEnabled() {
		return locationEnabled;
	}
	
	public static void setLocationEnabled(boolean state) {
		locationEnabled = state;
	}

	private static StackTraceElement getLocation() {
		if (java13 || !locationEnabled) {
			return null;
		}
		try {
			StackTraceElement[] ste = new Throwable().getStackTrace();
			boolean wrapperFound = false;
			for (int i = 0; i < ste.length - 1; i++) {
				if (fqcnSet.contains(ste[i].getClassName())) {
					wrapperFound = false;
					String nextClassName = ste[i + 1].getClassName();
					if (nextClassName.startsWith("java.") || nextClassName.startsWith("sun.")) {
						continue;
					}
					if (!fqcnSet.contains(nextClassName)) {
						if (logFunctionsSet.contains(ste[i + 1].getMethodName())) {
							wrapperFound = true;
						} else {
							// if dynamic proxy classes
							if (nextClassName.startsWith("$Proxy")) {
								return ste[i + 2];
							} else {
								return ste[i + 1];
							}
						}
					}
				} else if (wrapperFound) {
					if (!logFunctionsSet.contains(ste[i].getMethodName())) {
						return ste[i];
					}
				}
			}
			return ste[ste.length - 1];
		} catch (Throwable e) {
			java13 = true;
		}
		return null;
	}

	private static void write(int level, String message, Throwable throwable) {
		callAppenders(new LoggingEvent(level, message, getLocation(), throwable));
	}

	private static void write(int level, String message, Throwable throwable, Object data) {
		callAppenders(new LoggingEvent(level, message, getLocation(), throwable, data));
	}

	public static void debug(String message) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null);
		}
	}

	public static void debug(String message, Throwable t) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, t);
		}
	}

	public static void debug(Throwable t) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, "error", t);
		}
	}

	public static void debug(String message, String v) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, v);
		}
	}

	public static void debug(String message, Object o) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper(o));
		}
	}

	public static void debug(String message, String v1, String v2) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper(v1, v2));
		}
	}

	public static void debug(String message, long v) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper(v));
		}
	}

	public static void debug0x(String message, long v) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper("0x" + Long.toHexString(v)));
		}
	}

	public static void debug(String message, long v1, long v2) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper(v1, v2));
		}
	}

	public static void debug(String message, boolean v) {
		if (isDebugEnabled()) {
			write(LoggingEvent.DEBUG, message, null, new LoggerDataWrapper(v));
		}
	}

	public static void debugClassLoader(String message, Object obj) {
		if (obj == null) {
			write(LoggingEvent.DEBUG, message + " no class, no object", null, null);
			return;
		}
		Class klass;
		StringBuffer buf = new StringBuffer();
		buf.append(message).append(" ");
		if (obj instanceof Class) {
			klass = (Class) obj;
			buf.append("class ");
		} else {
			klass = obj.getClass();
			buf.append("instance ");
		}
		buf.append(klass.getName() + " loaded by ");
		if (klass.getClassLoader() != null) {
			buf.append(klass.getClassLoader().hashCode());
			buf.append(" ");
			buf.append(klass.getClassLoader().getClass().getName());
		} else {
			buf.append("system");
		}
		write(LoggingEvent.DEBUG, buf.toString(), null, null);
	}

	public static void info(String message) {
		if (isErrorEnabled()) {
			write(LoggingEvent.INFO, message, null);
		}
	}

	public static void info(String message, String data) {
		if (isErrorEnabled()) {
			write(LoggingEvent.INFO, message, null, data);
		}
	}

	public static void warn(String message) {
		if (isErrorEnabled()) {
			write(LoggingEvent.WARN, message, null);
		}
	}

	public static void error(String message) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + message, null);
		}
	}

	public static void error(String message, long v) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + message, null, new LoggerDataWrapper(v));
		}
	}

	public static void error(String message, String v) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + message, null, v);
		}
	}

	public static void error(String message, String v, Throwable t) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + message, t, v);
		}
	}

	public static void error(Throwable t) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + t.toString(), t);
		}
	}

	public static void error(String message, Throwable t) {
		if (isErrorEnabled()) {
			write(LoggingEvent.ERROR, "error " + message + " " + t.toString(), t);
		}
	}

	private static void callAppenders(LoggingEvent event) {
		for (Iterator iter = loggerAppenders.iterator(); iter.hasNext();) {
			LoggerAppender a = (LoggerAppender) iter.next();
			a.append(event);
		}
		;
	}

	/**
	 * Add the Class which serves as entry point for log message location.
	 * 
	 * @param origin
	 *            Class
	 */
	public static void addLogOrigin(Class origin) {
		fqcnSet.add(origin.getName());
	}

	public static void addAppender(LoggerAppender newAppender) {
		loggerAppenders.add(newAppender);
	}

	public static void removeAppender(LoggerAppender appender) {
		loggerAppenders.remove(appender);
	}

	public static void removeAllAppenders() {
		loggerAppenders.clear();
	}

}
