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
 * @version $Revision: 1.4.4.3 $
 */
package java.beans.beancontext;

import java.util.EventObject;

/**
 * @author Sergei A. Krivenko
 * @version $Revision: 1.4.4.3 $
 */

public abstract class BeanContextEvent extends EventObject {

    /**
     * @serial
     */
    protected BeanContext propagatedFrom;
    
    /**
     * @com.intel.drl.spec_ref
     */
    protected BeanContextEvent(BeanContext bc) {   
        super(bc);
    }
    
    /**
     * @com.intel.drl.spec_ref
     */
    public BeanContext getBeanContext() {
        return (BeanContext) super.getSource();
    }
    
    /**
     * @com.intel.drl.spec_ref
     */
    public synchronized BeanContext getPropagatedFrom() {
        return this.propagatedFrom;
    }
    
    /**
     * @com.intel.drl.spec_ref
     */
    public synchronized boolean isPropagated() {
        return (this.propagatedFrom != null);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public synchronized void setPropagatedFrom(BeanContext bc) {
        this.propagatedFrom = bc;
    }
}
