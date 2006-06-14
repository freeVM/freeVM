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


package javax.naming;

/**
 * This is the <code>NamingException</code> used when an operation is requested
 * which is not supported.
 * <p>
 * Multithreaded access to an instance is only safe when client code locks the
 * object first.</p>
 * 
 */
public class OperationNotSupportedException extends NamingException {
	
    /*
     * This constant is used during deserialization to check the J2SE version
     * which created the serialized object.
     */
	static final long serialVersionUID = 5493232822427682064L;

    /**
     * Constructs a <code>OperationNotSupportedException</code> instance 
     * with all data initialized to null.
     */
    public OperationNotSupportedException() {
        super();
    }

    /**
     * Constructs a <code>OperationNotSupportedException</code> instance
     * with the specified message. 
     * 
     * @param arg0 The detail message for this exception. It may be null.
     */
    public OperationNotSupportedException(String arg0) {
        super(arg0);
    }

}


