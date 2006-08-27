/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author  Vasily Zakharov
 * @version $Revision: 1.1.2.2 $
 */
package org.apache.harmony.jndi.provider.rmi.registry;

import java.rmi.registry.Registry;

import java.util.NoSuchElementException;

import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


/**
 * Enumeration of {@link NameClassPair} objects,
 * used by {@link RegistryContext#list(Name)} method.
 *
 * @author  Vasily Zakharov
 * @version $Revision: 1.1.2.2 $
 */
class NameClassPairEnumeration implements NamingEnumeration {

    /**
     * Binding names returned from {@link Registry#list()} method.
     */
    protected final String[] names;

    /**
     * Index of the next name to return.
     */
    protected int index = 0;

    /**
     * Creates this enumeration.
     *
     * @param   names
     *          Binding names returned from {@link Registry#list()} method.
     */
    public NameClassPairEnumeration(String[] names) {
        this.names = names;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasMore() {
        return (index < names.length);
    }

    /**
     * {@inheritDoc}
     */
    public Object next() throws NamingException, NoSuchElementException {
        if (!hasMore()) {
            throw new NoSuchElementException();
        }

        String name = names[index++];
        NameClassPair pair = new NameClassPair(name, Object.class.getName());
        pair.setNameInNamespace(name);
        return pair;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasMoreElements() {
        return hasMore();
    }

    /**
     * {@inheritDoc}
     */
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw (NoSuchElementException)
                    new NoSuchElementException().initCause(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        index = names.length;
    }
}
