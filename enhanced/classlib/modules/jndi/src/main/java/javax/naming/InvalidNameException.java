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
 * An <code>InvalidNameException</code> is the <code>NamingException</code> 
 * used when a supplied name does not match the required format. 
 * <p>
 * Multithreaded access to a <code>InvalidNameException</code> instance is 
 * only safe when client code locks the object first.</p>
 * 
 */
public class InvalidNameException extends NamingException {

    /*
     * For serialization purposes, the following fields must appear in the
     * order given and specified serialVersionUID must be used.
     */
    static final long serialVersionUID = -8370672380823801105L; // J2SE 1.4.2

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs an <code>InvalidNameException</code> instance 
     * with all data initialized to null.
     */
    public InvalidNameException() {
        super();
    }

    /**
     * Constructs an <code>InvalidNameException</code> instance 
     * with a specified error message.
     * 
     * @param msg The detail message for the exception. It may be null.
     */
    public InvalidNameException(String msg) {
        super(msg);
    }

}


