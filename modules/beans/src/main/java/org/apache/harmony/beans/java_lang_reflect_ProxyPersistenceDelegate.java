/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.beans;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class java_lang_reflect_ProxyPersistenceDelegate extends
        PersistenceDelegate {

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {
        assert oldInstance instanceof Proxy : oldInstance;

        Class[] interfaces = oldInstance.getClass().getInterfaces();
        InvocationHandler handler = Proxy.getInvocationHandler(oldInstance);

        return new Expression(oldInstance, Proxy.class, "newProxyInstance", //$NON-NLS-1$
                new Object[] { oldInstance.getClass().getClassLoader(),
                        interfaces, handler });
    }

    @Override
    protected void initialize(Class<?> type, Object oldInstance,
            Object newInstance, Encoder out) {
        // check for consistency
        assert oldInstance instanceof Proxy : oldInstance;
        assert newInstance instanceof Proxy : newInstance;
        assert newInstance == oldInstance;
    }

    @Override
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        assert oldInstance instanceof Proxy : oldInstance;

        return oldInstance == newInstance;
    }

}
