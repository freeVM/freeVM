/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @author Victor A. Martynov
 * @version $Revision: 1.3 $
 */
package org.apache.harmony.x.management.console.plugin.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BooleanEditor extends AbstractEditor {

	Button button;
	
	public BooleanEditor(Composite parent, 
                   String mbean_name, 
                   String attribute_name, 
                   String type, 
                   boolean isEditable) {
		
		super(parent, mbean_name, attribute_name, type, isEditable);

		button = new Button(this, SWT.CHECK);
		button.setBounds(leftPartWidth, 0, rightPartWidth, 25);
		
		if(isEditable) {
			button.setEnabled(true);
			button.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					BooleanEditor.this.commit();
				}
			});
		} else {
			button.setEnabled(false);
		}
		
		button.setToolTipText("Set the value");

		brief();
	}
	
	public void setValue(Object value) {
		String s = value+"";
		button.setSelection(new Boolean(s).booleanValue());
	}
	
	public String getValue() {
		return button.getSelection()+"";
	}

	public void brief() {
	}

	public void full() {
	}
	
	public void addMouseListener(MouseListener listener) {
		button.addMouseListener(listener);
	}

	public boolean isFocused() {
		return button.isFocusControl();
	}
}
