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
/**
* @author Alexander T. Simbirtsev
* @version $Revision$
*/
package org.apache.harmony.x.swing.text.html.form;

import javax.swing.text.AttributeSet;

public class FormFieldsetModel implements FormElement {
    private final Form form;
    private final AttributeSet attributes;
    private AttributeSet legendAttributes;
    private String legend;

    public FormFieldsetModel(final Form form, final AttributeSet attr) {
        this.form = form;
        attributes = attr.copyAttributes();
    }
    
    public Form getForm() {
        return form;
    }

    public AttributeSet getAttributes() {
        return attributes;
    }

    public int getElementType() {
        return FormAttributes.FIELDSET_TYPE_INDEX;
    }

    public void setLegend(final String legend) {
        this.legend = legend;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegendAttributes(final AttributeSet attr) {
        legendAttributes = attr.copyAttributes();
    }

    public AttributeSet getLegendAttributes() {
        return legendAttributes;
    }
}
