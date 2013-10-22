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
 * @version $Revision: 1.1.2.1 $
 */
package org.apache.harmony.beans;

/**
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.1.2.1 $
 */

public class Argument {
    
    private Class type = null;
    private Object value = null;
    private Class[] interfaces = null;
    
    public Argument(Object value) {
        this.value = value;
        if(this.value != null) {
            this.type = value.getClass();
            this.interfaces = this.type.getInterfaces();
        }
    }
    
    public Argument(Class type, Object value) {
        this.type = type;
        this.value = value;
        this.interfaces = type.getInterfaces();
    }
    
    public Class getType() { return type; }
    
    public Object getValue() { return value; }
    
    public Class[] getInterfaces() { return interfaces; }
    
    public void setType(Class type) {
        this.type = type;
        this.interfaces = type.getInterfaces();
    }
    
    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }
}
