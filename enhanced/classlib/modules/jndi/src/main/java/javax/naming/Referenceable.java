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
 * An object which is not in a naming service, but can be referenced, implements
 * the <code>Referenceable</code> interface.
 * The <code>getReference()</code> method is implemented to provide details of 
 * how to find the object.
 * 
 * @see Reference
 * 
 */
public interface Referenceable {

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Get the <code>Reference</code> object associated with this <code>
     * Referenceable</code> object.
     * 
     * @return                  the <code>Reference</code> object associated 
     *                          with this <code>Referenceable</code> object
     * @throws NamingException  if a naming error occurs
     */
    Reference getReference() throws NamingException;
}


