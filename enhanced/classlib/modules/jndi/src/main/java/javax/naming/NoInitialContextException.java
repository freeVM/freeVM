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
 * A <code>NoInitialContextException</code> is the exception thrown by the
 * naming classes when an initial context cannot be created. See the
 * specification of the <code>Context</code> interface and the 
 * <code>InitialContext</code> class regarding how initial context 
 * implementations are selected.
 * <p>
 * Any interaction with an <code>InitialContext</code> object may cause a
 * <code>NoInitialContextException</code> to be thrown. The 
 * <code>InitialContext</code> implementation may choose to defer getting 
 * the initial context until any of its methods are invoked.</p>
 * <p>
 * Multithreaded access to a <code>NoInitialContextException</code> instance 
 * is only safe when client code locks the object first.</p>
 * 
 */
public class NoInitialContextException extends NamingException {

    /*
     * For serialization purposes, the following fields must appear in the
     * order given and specified serialVersionUID must be used.
     */
    static final long serialVersionUID = -3413733186901258623L; // J2SE 1.4.2

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs an <code>NoInitialContextException</code> instance 
     * with all data initialized to null.
     */
    public NoInitialContextException() {
        super();
    }

    /**
     * Constructs an <code>NoInitialContextException</code> instance with the
     * specified error message.
     *  
     * @param msg The detail message for this exception. It may be null.
     */
    public NoInitialContextException(String msg) {
        super(msg);
    }

}


