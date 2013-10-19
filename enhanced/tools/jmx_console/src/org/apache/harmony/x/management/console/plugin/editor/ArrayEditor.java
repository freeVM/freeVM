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

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ArrayEditor extends AbstractEditor {

	private static final int BUTTON_WIDTH=70;

	Table table;
	TableColumn column;
	Button button;
	Text text;
	
	public ArrayEditor(Composite parent, String mbean_name,
			String attribute_name, String type, boolean isEditable) {
		super(parent, mbean_name, attribute_name, type, isEditable);

		int tableWidth = rightPartWidth - (isEditable ? BUTTON_WIDTH : 0);  

		table = new Table(this, SWT.MULTI);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		column = new TableColumn(table, SWT.LEFT);
		column.setWidth(tableWidth-25);

		if(isEditable) {
			button = new Button(this, SWT.NONE);
			button.setText("Set");
			button.setToolTipText("Set attribute value in the MBean");
			button.setBounds(leftPartWidth+tableWidth, 0, BUTTON_WIDTH, 25);
			button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				ArrayEditor.this.commit();
			}
			});
		} 
		
		showExpandCollapseButton(true);
		brief();
	}

	public void setValue(Object value) {
		Object []array = convertArray(value).toArray();
		table.removeAll();
		column.setText(convertType(type)+"["+array.length+"]");
		for(int i = 0; i < array.length; i++) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, array[i]+"");
		} 
	}

	public void brief() {
		int tableWidth = rightPartWidth - (isEditable ? BUTTON_WIDTH : 0);  
		table.setBounds(leftPartWidth, 0, tableWidth, 25);
	}

	public void full() {
		int tableWidth = rightPartWidth - (isEditable ? BUTTON_WIDTH : 0);  
		Point preferredSize = table.computeSize(tableWidth, SWT.DEFAULT);
		table.setBounds(leftPartWidth, 0, tableWidth, preferredSize.y);
	}

	public boolean isFocused() {
		return table.isFocusControl();
	}

	public String getValue() {
		return null;
	}
	
	private Vector convertArray(Object o) {
		
		Vector v = new Vector();
		
		if(o instanceof Object[]) {
			Object oo[] = (Object[]) o;
			for(int i = 0; i < oo.length; i++) {
				v.add(oo[i]);
			}
		} else if(o instanceof long[]) {
			long ol[] = (long[]) o;
			for(int i = 0; i < ol.length; i++) {
				v.add(new Long(ol[i]));
			}
		}
		return v;
	}

	private String convertType(String type) {
		if(       type.equals("[B")) {
			return "byte";
		} else if(type.equals("[C")) {
			return "char";
		} else if(type.equals("[D")) {
			return "double";
		} else if(type.equals("[F")) {
			return "float";
		} else if(type.equals("[I")) {
			return "int";
		} else if(type.equals("[J")) {
			return "long";
		} else if(type.equals("[S")) {
			return "short";
		} else if(type.equals("[Z")) {
			return "boolean";
		} else if(type.startsWith("[L")) {
			return type.substring(2, type.length()-1);
		} else {			
			return type;
		}
	}
}
