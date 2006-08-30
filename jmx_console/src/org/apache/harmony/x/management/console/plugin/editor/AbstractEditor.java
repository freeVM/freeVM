/* Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.harmony.x.management.console.plugin.Conn;
import org.apache.harmony.x.management.console.plugin.Tools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Represents single attribute editor.
 * Each attribute editor consists of 2 parts: the left  and the right part. 
 * Left part holds name/type of attribute and optionally expand/collapse button.
 * Right part contains the representation of this attribute according to its type.
 * It means that in the case of text based attributes (i.e. String, ObjectName)
 * the right part of editor will contain text box and optionally Submit button.
 * On the other hand if the attribute is of array type or TabularData the right 
 * part of editor will contain the table. 
 * 
 * @author Victor A. Martynov
 */
public abstract class AbstractEditor extends Composite {
	
	protected static final int NAME_WIDTH_PERCENTAGE = 30;
	
	protected String mbean_name;
	protected String attribute_name;
	protected String type;
	protected Composite parent;
	boolean collapsed;
	protected boolean isEditable;
	
	protected int rightPartWidth; 
	protected int leftPartWidth; 
	private Button expandCollapseButton;

	protected Composite row;
	
	/**
	 * Each editor should have the main element which obtains focus when the 
	 * attribute is edited. If the focus was given to main UI element of the 
	 * editor, the refresh should stop to allow editing. Each editor 
	 * implements this method in its own way.
	 * 
	 * @return true if the main element of editor got focus, false otherwise.
	 */
	public abstract boolean isFocused(); 
	
	/**
	 * Each editor implementation should convert its contents into a String and return
	 * it here. 
	 * 
	 * @return The value of the attribute as a String.
	 */
	public abstract String getValue();

	/**
	 * This method sets the value of this editor to the given value. It is a 
	 * responsibility of the editor implementations to convert 
	 * <code>value</code> into something suitable for this type of editor.
	 * 
	 * @param value The value of attribute.
	 */
	public abstract void setValue(Object value);
	
	/**
	 * Brief view has the height of 25 pixels. 
	 */
	public abstract void brief();
	
	/**
	 * Full view has the height returned by <code>getFullHeight</code> method. 
	 */
	public abstract void full();

	protected AbstractEditor(final Composite parent, 
			                 final String mbean_name, 
			                 final String attribute_name, 
			                 final String type, 
			                 boolean isEditable) {
		super(parent, SWT.NONE);
		
		this.mbean_name = mbean_name;
		this.attribute_name = attribute_name;
		this.type = type;
		this.parent = parent;
		this.isEditable = isEditable;
		collapsed = true;
		rightPartWidth = parent.getSize().x*(100-NAME_WIDTH_PERCENTAGE)/100 - 25; 
		leftPartWidth = parent.getSize().x*NAME_WIDTH_PERCENTAGE/100;

		rightPartWidth = (rightPartWidth <= 0) ? 600 : rightPartWidth;
		leftPartWidth = (leftPartWidth <= 0) ? 300 : leftPartWidth;
		
		setAttributeNameAndType();
		setSize(leftPartWidth+rightPartWidth, 25);
	}

	
	protected void showExpandCollapseButton(boolean visible) {
		expandCollapseButton.setVisible(visible);
	}
	
	
	protected void commit() {
		try {
			Conn.getMBeanOperations().setAttribute(mbean_name, attribute_name, getValue(), type);
		} catch (Exception e) {
			Tools.fireErrorDialog(getParent(), e);
		}
	}
	
	/* ************************************************************************
	 * Private Methods
	 *************************************************************************/
	
	/**
	 * 
	 */
	private void setAttributeNameAndType() {

		int dotPosition = type.lastIndexOf("."); //Last Dot position 
		String short_attr_type = (dotPosition == -1) 
		                         ? type 
		                         : type.substring(dotPosition + 1);
		
		Label name_type_LABEL = new Label(this, SWT.NONE);
		name_type_LABEL.setText(attribute_name + "[" + short_attr_type+ "]: ");
		name_type_LABEL.setToolTipText(attribute_name + "[" + type+ "]");
		name_type_LABEL.setBounds(0,0, leftPartWidth-25, 25);

		expandCollapseButton = new Button(this, SWT.NONE);
		expandCollapseButton.setVisible(false);
		
		expandCollapseButton.setText("+");
		expandCollapseButton.setToolTipText("Expand");
		
		expandCollapseButton.setBounds(leftPartWidth-25+5, 5, 15, 15);
		expandCollapseButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				if(collapsed) {
					expandCollapseButton.setText("-");
					expandCollapseButton.setToolTipText("Collapse");
					full();
					collapsed = false;
				} else {
					expandCollapseButton.setText("+");
					expandCollapseButton.setToolTipText("Expand");
					brief();
					collapsed = true;
				}
				
				layout(true);

				Composite attributeTable = getParent();
				attributeTable.layout(true);
				Point preferredSize = attributeTable.computeSize(getParent().getSize().x, SWT.DEFAULT);
				ScrolledComposite sc = (ScrolledComposite)getParent().getParent();
				sc.setMinSize(preferredSize);
				sc.layout(true);
			}
		});
	}
}
