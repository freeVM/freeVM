/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, 
 *  as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.archive.internal.pack200;

/**
 * Represents a problem with a Pack200 coding/decoding issue.
 * 
 * @author Alex Blewitt
 * @version $Revision: $
 */
public class Pack200Exception extends Exception {

	/**
	 * Create a new Pack200 exception with the given message and cause
	 * 
	 * @param message
	 *            the text message to display
	 */
	public Pack200Exception(String message) {
		super(message);
	}

	/**
	 * Create a new Pack200 exception with the given message and cause
	 * 
	 * @param message
	 *            the text message to display
	 * @param cause
	 *            the throwable that caused this problem
	 */
	public Pack200Exception(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new Pack200 exception with the given message and cause
	 * 
	 * @param cause
	 *            the throwable that caused this problem
	 */
	public Pack200Exception(Throwable cause) {
		super(cause);
	}

}
