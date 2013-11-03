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

package org.microemu.android.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.microemu.android.MicroEmulator;
import org.microemu.log.LoggerAppender;
import org.microemu.log.LoggingEvent;

import android.util.Log;

public class AndroidLoggerAppender implements LoggerAppender {

	public static String formatLocation(StackTraceElement ste) {
		if (ste == null) {
			return "";
		}
		// Make Line# clickable in eclipse
		return ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber()
				+ ")";
	}

	public void append(LoggingEvent event) {
    	String data = "";
    	if (event.hasData()) {
    		data = " [" + event.getFormatedData() + "]";
    	}
		Log.v(MicroEmulator.LOG_TAG, event.getMessage() + data +  "\n\t  " + formatLocation(event.getLocation()));
    	if (event.getThrowable() != null) {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		event.getThrowable().printStackTrace(new PrintStream(baos));
    		Log.v(MicroEmulator.LOG_TAG, baos.toString());
    	}

	}

}
