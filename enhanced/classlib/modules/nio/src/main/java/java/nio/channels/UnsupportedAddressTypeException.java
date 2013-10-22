/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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

package java.nio.channels;


/**
 * Thrown when connecting or binding to an unsupported address type.
 * 
 */
public class UnsupportedAddressTypeException extends IllegalArgumentException {

	private static final long serialVersionUID = -2964323842829700493L;

	/**
	 * Default constructor.
	 * 
	 */
	public UnsupportedAddressTypeException() {
		super();
	}
}
