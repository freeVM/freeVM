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
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.3.6.3 $
 */
package java.beans;

import java.util.EventListenerProxy;

/**
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.3.6.3 $
 */

public class VetoableChangeListenerProxy extends EventListenerProxy
        implements VetoableChangeListener {
    
    private String propertyName;

    /**
     * @com.intel.drl.spec_ref
     */
    public VetoableChangeListenerProxy(
            String propertyName, VetoableChangeListener listener) {
        super(listener);
        this.propertyName = propertyName;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {
        VetoableChangeListener listener =
                (VetoableChangeListener) getListener();
        if(listener != null) listener.vetoableChange(evt);
    }
}
