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
 * @author Alexei Y. Zakharov
 * @version $Revision: 1.1.2.4 $
 */

package org.apache.harmony.jndi.provider.dns;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;


/**
 * Factory to create DNS URL contexts.
 * @author Alexei Zakharov
 * @version $Revision: 1.1.2.4 $
 */
public class dnsURLContextFactory implements ObjectFactory {
    
    /**
     * Returns new instance of DNS URL context.
     * @param obj either <code>null</code>, URL in string form or array
     * of URL in string form  
     * @param name ignored
     * @param nameCtx ignored
     * @param environment is passed to the context being created
     * @return created DNS context, an instance of either
     *  <code>dnsURLContext</code> or <code>DNSContext</code> class
     * @throws IllegalArgumentException if bad <code>obj</code> is given
     * @throws NamingException if such exception was encountered 
     * @see ObjectFactory#getObjectInstance(Object, Name, Context, Hashtable)
     */
    @SuppressWarnings("unchecked")
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> environment) throws NamingException
    {
        if (obj == null) {
            return new dnsURLContext(environment);
        } else if (obj instanceof String) {
            Hashtable<Object, Object> newEnv = (Hashtable<Object, Object>) environment.clone();

            newEnv.put(Context.PROVIDER_URL, obj);
            return new DNSContext(newEnv);
        } else if (obj instanceof String[]) {
            Hashtable<Object, Object> newEnv = (Hashtable<Object, Object>) environment.clone();
            StringBuffer sb = new StringBuffer();
            String urlArr[] = (String[]) obj;

            for (int i = 0; i < urlArr.length; i++) {
                if (i != 0) {
                    sb.append(' ');
                }
                sb.append(urlArr[i]);
            }
            newEnv.put(Context.PROVIDER_URL, sb.toString());
            return new DNSContext(newEnv);
        } else {
            throw new IllegalArgumentException(
                    "obj should be either null, String or array of String");
        }
    }

}
