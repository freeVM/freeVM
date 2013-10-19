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

/**
 * @author vlads
 *
 */
public class LoggingEvent {

	public final static int DEBUG = 1;

	public final static int INFO = 2;

	public final static int WARN = 3;

	public final static int ERROR = 4;
	
	protected int level;

	protected String message;
    
	protected StackTraceElement location; 

	protected boolean hasData = false;

	protected Object data;

	protected Throwable throwable;

	protected long eventTime;


    public LoggingEvent() {
    	this.eventTime = System.currentTimeMillis();
    }
    
	public LoggingEvent(int level, String message, StackTraceElement location, Throwable throwable) {
		this();
		this.level = level;
		this.message = message;
		this.location = location;
		this.throwable = throwable;
	}
	
	public LoggingEvent(int level, String message, StackTraceElement location, Throwable throwable, Object data) {
		this(level, message, location, throwable);
		setData(data);
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
		this.hasData = true;
	}

	public boolean hasData() {
		return this.hasData;
	}

	public String getFormatedData() {
		if (hasData()) {
			if (getData() == null) {
				return "{null}";
			} else {
				return getData().toString();
			}
		} else {
			return "";
		}
	}
	
	public long getEventTime() {
		return this.eventTime;
	}

	public int getLevel() {
		return this.level;
	}

	public StackTraceElement getLocation() {
		return this.location;
	}

	public String getMessage() {
		return this.message;
	}

	public Throwable getThrowable() {
		return this.throwable;
	}

}
