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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NumericEditor extends AbstractEditor {

	Text text;
	Button setButton;
	private final int BUTTON_WIDTH = 70; 

	public NumericEditor(Composite parent, String mbean_name,
			String attribute_name, String type, boolean isEditable) {
		super(parent, mbean_name, attribute_name, type, isEditable);
		
		text = new Text(this, SWT.BORDER);

		int textWidth = rightPartWidth - (isEditable ? BUTTON_WIDTH : 0); 
		
		text.setBounds(leftPartWidth, 0, textWidth, 25);
		text.setEditable(isEditable);
		
		if(isEditable) {
			setButton = new Button(this, SWT.NONE);
			setButton.setText("Set");
			setButton.setToolTipText("Set attribute value in the MBean");
			setButton.setBounds(leftPartWidth+textWidth, 0, BUTTON_WIDTH, 25);
			setButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				NumericEditor.this.commit();
			}
			});
		} else {
			text.setEditable(false);
		}
		
		showExpandCollapseButton(false);
		brief();
	}

	public void brief() {
	}

	public void full() {
	}

	public boolean isFocused() {
		return text.isFocusControl();
	}

	public String getValue() {
		return text.getText();
	}
	
	public void setValue(Object value) {
		text.setText(value+"");
	}
}
