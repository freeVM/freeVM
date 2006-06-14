/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
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

 package javax.naming.directory;

import javax.naming.NamingException;

/**
 * Thrown when an attempt is made to set attributes that are invalid for
 * the entry they are being targetted.
 * <p>
 * Examples include schema restrictions for attributes such as specific values
 * required, attributes that must be set exclusively of others, and so on.</p>
 * <p>
 * The list of invalid cases is defined by the directory service provider.</p>
 * 
 * @see NamingException
 * 
 */
public class InvalidAttributesException extends NamingException {

	/*
	 * -------------------------------------------------------------------
	 * Constants
	 * -------------------------------------------------------------------
	 */

	private static final long serialVersionUID = 0x24301a12642c8465L;

	/*
	 * -------------------------------------------------------------------
	 * Constructors
	 * -------------------------------------------------------------------
	 */
	
	/**
	 * Default constructor. 
	 * <p>
	 * All fields are initialized to null.</p>
	 */		
	public InvalidAttributesException() {
		super();
	}

	/**
	 * Constructs an <code>InvalidAttributesException</code> instance using 
     * the supplied text of the message.
	 * <p>
	 * All fields are initialized to null.</p>
	 * 
	 * @param s				message about the problem
	 */ 
	public InvalidAttributesException(String s) {
		super(s);
	}
}


