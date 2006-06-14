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
 * This is the <code>NamingException</code> used when communication with the 
 * naming or directory service fails.
 * <p>
 * Multithreaded access to an instance is only safe when client code locks the
 * object first.</p>
 * 
 */
public class CommunicationException extends NamingException {
    
	/*
     * This constant is used during deserialization to check the J2SE version
     * which created the serialized object.
     */
	static final long serialVersionUID = 3618507780299986611L; // J2SE 1.4.2	

    /**
     * Constructs a <code>CommunicationException</code> instance with all 
     * data initialized to null.
     */
    public CommunicationException() {
        super();
    }

    /**
     * Constructs a <code>CommunicationException</code> instance using the 
     * specified message.
     * 
     * @param s The detail message for the exception. It may be null.
     */
    public CommunicationException(String s) {
        super(s);
    }

}


