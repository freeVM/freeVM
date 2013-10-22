/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Sergei A. Krivenko
 * @version $Revision: 1.3.4.3 $
 */
package java.beans.beancontext;

import java.beans.DesignMode;
import java.beans.Visibility;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Collection;

/**
 * @author Sergei A. Krivenko
 * @version $Revision: 1.3.4.3 $
 */

public interface BeanContext extends BeanContextChild, Collection, DesignMode, 
        Visibility {

     /** @todo: find out what it should be initialized to **/
    public static final Object globalHierarchyLock = new Object();
    
    /**
     * @com.intel.drl.spec_ref
     */
    public void addBeanContextMembershipListener(
            BeanContextMembershipListener bcml);
    
    /**
     * @com.intel.drl.spec_ref
     */
    public URL getResource(String name, BeanContextChild bcc) 
        throws IllegalArgumentException;

    /**
     * @com.intel.drl.spec_ref
     */
    public InputStream getResourceAsStream(String name, BeanContextChild bcc) 
        throws IllegalArgumentException;

    /**
     * @com.intel.drl.spec_ref
     */
    public Object instantiateChild(String beanName) 
        throws IOException, ClassNotFoundException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void removeBeanContextMembershipListener(
            BeanContextMembershipListener bcml);
}
