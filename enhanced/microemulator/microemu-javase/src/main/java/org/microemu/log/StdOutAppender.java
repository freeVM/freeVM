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

import java.io.PrintStream;

/**
 * @author vlads
 * 
 */
public class StdOutAppender implements LoggerAppender {

	public static boolean enabled = true;
	
	public static String formatLocation(StackTraceElement ste) {
		if (ste == null) {
			return "";
		}
		// Make Line# clickable in eclipse
		return ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber()
				+ ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.microemu.log.LoggerAppender#append(org.microemu.log.LoggingEvent)
	 */
	public void append(LoggingEvent event) {
		if (!enabled) {
			return;
		}
		PrintStream out = System.out; 
    	if (event.getLevel() == LoggingEvent.ERROR) {
    		out = System.err;
    	}
    	String data = "";
    	if (event.hasData()) {
    		data = " [" + event.getFormatedData() + "]";
    	}
    	String location = formatLocation(event.getLocation());
    	if (location.length() > 0) {
    		location = "\n\t  " + location;
    	}
    	out.println(event.getMessage() + data + location);
    	if (event.getThrowable() != null) {
    		event.getThrowable().printStackTrace(out);
    	}

	}

}
