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
package org.apache.harmony.x.management.console.plugin;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.harmony.x.management.console.controller.AttributeInfo;
import org.apache.harmony.x.management.console.plugin.editor.AbstractEditor;
import org.apache.harmony.x.management.console.plugin.editor.EditorFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;


/**
 * <code>AttributeView</code> is responsible for visualizing of the attributes
 * of MBean selected in <code>MbeanTreeView</code>. Because the contents of 
 * this view depends on the selection in <code>MbeanTreeView</code> this class
 * implements <code>IMBeanTreeDependant</code> interface. The visualization of 
 * each attribute is defined according to its type. The type of attribute 
 * defines which successor of <code>AbstractEditor</code> will be responsible 
 * for its visuzlization.
 * 
 * 
 * @see IMBeanTreeDependant
 * @see org.apache.harmony.x.management.console.plugin.editor.AbstractEditor
 * @author Victor A. Martynov
 */
public class AttributeView extends ViewPart implements IMBeanTreeDependant {
	
	/**
	 * ID of this view in Eclipse runtime.
	 */
	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.attribute";
	
	/**
	 * The delay between two consecutive refreshes of the attribute values.
	 */
	public static final long REFRESH_RATE = 1000L;

	private ScrolledComposite attrs_SCROLLCOMPOSITE = null;
	private Composite attrs_table_COMPOSITE = null;
    private Composite parent = null;
	private boolean subscribed;
	
	/**
	 * Refreshes the values of the attribute.
	 */
	private Timer timer;
	
	/**
	 *  
	 */
	private Hashtable attributeHashtable = null;

	private MBeanTreeListener listener;
	
	/**
	 * Calls the default constructor of the superclass.
	 */
	public AttributeView() {
		super();
	}
	
	
	/**
	 * Associates attributes and their editors.
	 */
	public void createPartControl(final Composite parent) {

		this.parent = parent;
		Conn.addView(this);
		listener = new MBeanTreeListener(this);
		
		attrs_SCROLLCOMPOSITE = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		attrs_SCROLLCOMPOSITE.setExpandHorizontal(true);

		attrs_SCROLLCOMPOSITE.setExpandVertical(true);

		attrs_table_COMPOSITE = new Composite(attrs_SCROLLCOMPOSITE, SWT.NONE);
		attrs_table_COMPOSITE.setLayout(new GridLayout(1, false));
		attrs_SCROLLCOMPOSITE.setContent(attrs_table_COMPOSITE);

		redraw(Conn.getActiveNode());
		
		parent.layout(true);
	}

	
	/**
	 * Redraws the view. 
	 */
	public void redraw(final MBeanTreeNode activeNode) {
		
		if(Conn.DEBUG) {
			OutputView.print("AttributeView.redraw("+activeNode+")");
		}
		
		Tools.cleanComposite(attrs_table_COMPOSITE);
		
		attributeHashtable = new Hashtable(); // Cleaning old Hashtable
		
		if(Conn.getMode() == Conn.NOT_CONNECTED) {
			new Label(attrs_table_COMPOSITE, SWT.NONE).setText(Conn.getStatus());
			attrs_table_COMPOSITE.layout(true);
			return;
		} 

		if(activeNode.getName() == null) {
			new Label(attrs_table_COMPOSITE, SWT.NONE).setText("No attributes available.");
			attrs_table_COMPOSITE.layout(true);
			return;
		}

		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		timer = new Timer(true);
		timer.schedule(new TimerTask() {

			public void run() {
				if(parent.isDisposed()) {
					this.cancel();
					return;
				}
				parent.getDisplay().syncExec(new Runnable() {

					public void run() {
						refreshValues(activeNode);
					}
					
				});
			}
		}, REFRESH_RATE, REFRESH_RATE);
		
		List list = null;
		
		try {
			list = Conn.getMBeanOperations().listAttributes(activeNode.getName());
		} catch (Exception e) {
			Tools.fireErrorDialog(parent, e);
			return;
		}

		final AttributeInfo attrs[] = (AttributeInfo[]) list.toArray(new AttributeInfo[0]);

		if (attrs.length == 0) {
			new Label(attrs_table_COMPOSITE, SWT.NONE).setText("No attributes available.");
			attrs_table_COMPOSITE.layout(true);
			return;
		}
		
		for (int i = 0; i < attrs.length; i++) {
			String attr_name = attrs[i].getName();
			String attr_type = attrs[i].getType();

			
			AbstractEditor editor = EditorFactory.create(attrs_table_COMPOSITE, 
					activeNode.getName(), 
                    attr_name,
                    attr_type,
                    attrs[i].isWritable());

			attributeHashtable.put(attr_name, editor);

			String[] attr_key_type = new String[2];

			attr_key_type[0] = attr_name;
			attr_key_type[1] = attr_type;

			editor.setData(attr_key_type);
			editor.layout(true);
		}

		Point preferredSize = attrs_table_COMPOSITE.computeSize(parent.getSize().x, SWT.DEFAULT);
		attrs_SCROLLCOMPOSITE.setMinSize(preferredSize);
		refreshValues(activeNode);
		attrs_table_COMPOSITE.layout(true);
		attrs_SCROLLCOMPOSITE.layout(true);
		
		parent.layout(true);
	}
	
	/**
	 * Refreshes the values in all attribute editors of this view.
	 */
	private void refreshValues(MBeanTreeNode activeNode) {
		Enumeration enumeration = attributeHashtable.keys();
		
		while(enumeration.hasMoreElements()) {
			String attrName = (String) enumeration.nextElement();
			
			AbstractEditor editor = (AbstractEditor) attributeHashtable.get(attrName);
			
			if(editor.isDisposed()) {
				return;
			}
			
			if(editor.isFocused()) {
				continue;
			}
			
			try {
				editor.setValue(Conn.getMBeanOperations().getAttribute(activeNode.getName(), attrName));
				editor.layout(true);
			} catch (Exception e) {
				Tools.fireErrorDialog(parent, e); 
				OutputView.print(e);
				timer.cancel();
				return;
			}
		}
	}

	public void setFocus() {
		// This method was intentionally left empty.
	}
	
	public Composite getParent() {
		return parent;
	}

	public MBeanTreeListener getMBeanTreeListener() {
		return listener;
	}

	public boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean val) {
		subscribed = val;
	}
}