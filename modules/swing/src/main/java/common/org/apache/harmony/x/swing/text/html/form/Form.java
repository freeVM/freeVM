/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
* @author Alexander T. Simbirtsev
* @version $Revision$
*/
package org.apache.harmony.x.swing.text.html.form;

import java.util.ArrayList;

import javax.swing.text.AttributeSet;

public class Form {
    private final ArrayList elements = new ArrayList();
    private final AttributeSet attributes;

    public Form(final AttributeSet attr) {
        attributes = attr.copyAttributes();
    }

    public int getElementsCount() {
        return elements.size();
    }

    public void addElement(final FormElement element) {
        elements.add(element);
    }

    public void removeElement(final FormElement element) {
        elements.remove(element);
    }

    public FormElement getElement(final int index) {
        return (0 <= index && index < elements.size()) ? (FormElement)elements.get(index) : null;
    }

    public AttributeSet getAttributes() {
        return attributes;
    }

}
