/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.test.func.api.java.beans.boundproperties.auxiliary;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

/**
 */
public class SimpleProxyListener extends PropertyChangeListenerProxy {
    public boolean invoked = false;

    public void propertyChange(PropertyChangeEvent arg0) {
        invoked = true;
        super.propertyChange(arg0);
    }

    public SimpleProxyListener(String arg0, PropertyChangeListener arg1) {
        super(arg0, arg1);
    }

    /**
     * @return Returns the invoked.
     */
    public boolean isInvoked() {
        return invoked;
    }
}