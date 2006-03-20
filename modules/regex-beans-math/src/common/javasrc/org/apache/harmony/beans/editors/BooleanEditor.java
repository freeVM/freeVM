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
package org.apache.harmony.beans.editors;

import java.beans.PropertyEditorSupport;

/**
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.1.2.1 $
 */

public class BooleanEditor extends PropertyEditorSupport {
    
    /**
     * 
     * @param source
     */
    public BooleanEditor(Object source) {
        super(source);
    }

    /**
     */
    public BooleanEditor() {
        super();
    }
    
    public String getAsText() {
        return getValueAsString();
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new Boolean(text));
    }
    
    public String getJavaInitializationString() {
        return getValueAsString();
    }
    
    public String[] getTags() {
        return new String[] {"true", "false"};
    }
    
    public void setValue(Object value) {
        if(value instanceof Boolean) {
            super.setValue(value);
        }
    }
    
    private String getValueAsString() {
        String result = null;
        Object value = getValue();
        if(value != null) {
            Boolean bValue = (Boolean) value;
            result = bValue.toString();
        }
        return result;
    }
}
