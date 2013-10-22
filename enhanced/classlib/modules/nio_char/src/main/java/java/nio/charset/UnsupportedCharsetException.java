/* Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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

package java.nio.charset;


/**
 * Thrown when an unsupported charset name is encountered.
 * 
 */
public class UnsupportedCharsetException extends IllegalArgumentException {

	/*
	 * -------------------------------------------------------------------
	 * Constants
	 * -------------------------------------------------------------------
	 */

	/*
	 * This constant is used during deserialization to check the J2SE version
	 * which created the serialized object.
	 */
	private static final long serialVersionUID = 1490765524727386367L; // J2SE 1.4.2

	/*
	 * -------------------------------------------------------------------
	 * Instance variables
	 * -------------------------------------------------------------------
	 */

	// the unsupported charset name
	private String charsetName;

	/*
	 * -------------------------------------------------------------------
	 * Constructors
	 * -------------------------------------------------------------------
	 */

	/**
	 * Constructs an instance of this exception with the supplied charset name.
	 * 
	 * @param charset
	 *            the encountered unsupported charset name
	 */
	public UnsupportedCharsetException(String charset) {
		super("The unsupported charset name is \"" + charset + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
		this.charsetName = charset;
	}

	/*
	 * -------------------------------------------------------------------
	 * Methods
	 * -------------------------------------------------------------------
	 */

	/**
	 * Gets the encountered unsupported charset name.
	 * 
	 * @return the encountered unsupported charset name
	 */
	public String getCharsetName() {
		return this.charsetName;
	}

}
