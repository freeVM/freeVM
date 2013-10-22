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


package javax.transaction;

import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * An exception which is thrown when a request is made which supplied a null transaction
 * context in a situation where an active transaction is required.
 * 
 */
public class TransactionRequiredException extends RemoteException implements Serializable {

	private static final long serialVersionUID = -1898806419937446439L;
	
	/**
	 * Creates a TransactionRequiredException with no error message.
	 *
	 */
	public TransactionRequiredException() {
		super();
	} // end method TransactionRequiredException()
	
	/**
	 * Creates a TransactionRequiredException with a specified error message.
	 * @param msg a String holding the error message
	 */
	public TransactionRequiredException(String msg) {
		super( msg );
	} // end method TransactionRequiredException(String msg)
	
} // end class TransactionRequiredException


