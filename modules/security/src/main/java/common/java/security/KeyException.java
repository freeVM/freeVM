/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security;

/**
 * This class is the superclass of all classes which represent problems with
 * keys.
 * 
 * 
 * @see Throwable
 * @see Error
 */
public class KeyException extends GeneralSecurityException {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -7483676942812432108L;

	/**
	 * Constructs a new instance of this class with its walkback and message
	 * filled in.
	 * 
	 * 
	 * @param msg
	 *            String The detail message for the exception.
	 */
    public KeyException(String msg) {
        super(msg);
    }

	/**
	 * Constructs a new instance of this class with its walkback filled in.
	 * 
	 */
    public KeyException() {
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public KeyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public KeyException(Throwable cause) {
        super(cause);
    }
}
