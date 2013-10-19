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
 * This is the <code>NamingException</code> used when a connection to a 
 * server cannot be established.
 * <p>
 * Multithreaded access to an instance is only safe when client code locks the
 * object first.</p>
 * 
 */
public class ServiceUnavailableException extends NamingException {
	
    /*
     * This constant is used during deserialization to check the J2SE version
     * which created the serialized object.
     */
	static final long serialVersionUID = -4996964726566773444L; // J2SE 1.4.2

    /**
     * Constructs a <code>ServiceUnavailableException</code> instance 
     * with all data initialised to null.
     */
    public ServiceUnavailableException() {
        super();
    }

    /**
     * Constructs a <code>ServiceUnavailableException</code> instance
     * with the specified message.
     * 
     * @param s The detail message for this exception. It may be null.
     */
    public ServiceUnavailableException(String s) {
        super(s);
    }

}


