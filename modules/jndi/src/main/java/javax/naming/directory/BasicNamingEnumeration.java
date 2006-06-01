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

package javax.naming.directory;

import java.util.Enumeration;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * a simple implementation of NamingEnumeration
 * 
 */
class BasicNamingEnumeration<T> implements NamingEnumeration<T> {

    /*
     * -----------------------------------
     * Fields
     * -----------------------------------
     */

    private Enumeration<T> enumeration;

    /*
   	 * -----------------------------------
     * Constructors
     * -----------------------------------
     */

    /**
     * default constructor
     * @param e			wrapped enumeration
     */
    public BasicNamingEnumeration(Enumeration<T> e) {
        this.enumeration = e;
    }

	/*
	 * -----------------------------------
	 * Methods of interface NamingEnumeration
	 * -----------------------------------
	 */

    public T next() throws NamingException {
        return enumeration.nextElement();
    }

    public boolean hasMore() throws NamingException {
        return enumeration.hasMoreElements();
    }

    public void close() throws NamingException {
    	// Does nothing.
    }

    public boolean hasMoreElements() {
        return enumeration.hasMoreElements();
    }

    public T nextElement() {
        return enumeration.nextElement();
    }

}


