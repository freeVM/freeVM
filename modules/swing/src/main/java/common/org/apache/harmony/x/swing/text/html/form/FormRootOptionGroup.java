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

import java.util.Stack;

import javax.swing.text.SimpleAttributeSet;

import org.apache.harmony.x.swing.text.html.form.FormSelectModel;

public class FormRootOptionGroup extends FormOptionGroup {
    private final Stack groupStack = new Stack();
    
    private final FormSelectModel selectModel;

    public FormRootOptionGroup(final FormSelectModel selectModel) {
        super(SimpleAttributeSet.EMPTY);
        this.selectModel = selectModel;
    }

    public int getDepth() {
        return -1;
    }
    
    public boolean isEnabled() {
        return selectModel.isEnabled();
    }
    
    public String getTitle() {
        return selectModel.getTitle();
    }
    
    public void pushGroup(final FormOptionGroup group) {
        groupStack.push(group);
    }
    
    public void popGroup() {
        groupStack.pop();
    }
    
    public FormOptionGroup getCurrentGroup() {
        return !groupStack.isEmpty() ? (FormOptionGroup)groupStack.lastElement() : this;
    }
}
