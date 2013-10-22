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

package org.apache.harmony.beans.editors;

import java.beans.PropertyEditorSupport;

public class ByteEditor extends PropertyEditorSupport {

    public ByteEditor(Object source) {
        super(source);
    }

    public ByteEditor() {
        super();
    }

    @Override
    public String getAsText() {
        return getValueAsText();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(new Byte(text));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe.toString());
        }
    }

    @Override
    public String getJavaInitializationString() {
        return getValueAsText();
    }

    @Override
    public String[] getTags() {
        return null;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Byte) {
            super.setValue(value);
        }
    }

    private String getValueAsText() {
        String result = null;
        Object value = getValue();
        if (value != null) {
            Byte bValue = (Byte) value;
            result = bValue.toString();
        }
        return result;
    }
}
