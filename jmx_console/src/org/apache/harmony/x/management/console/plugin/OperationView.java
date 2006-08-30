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

package org.apache.harmony.x.management.console.plugin;

import java.util.List;

import org.apache.harmony.x.management.console.controller.OperationInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class OperationView extends ViewPart implements IMBeanTreeDependant {
	
	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.operation";

	private ScrolledComposite ops_SCROLLCOMPOSITE = null;

	private Composite ops_COMPOSITE = null;
	
	Composite parent;
	
	private MBeanTreeListener listener;
	private boolean subscribed;

	public OperationView () {
		super();
		OutputView.print("OperationView.<init>");
		listener = new MBeanTreeListener(this);
	}
	
	
	public void createPartControl(Composite parent) {
		/*
		 * The Composite containing operations.
		 */
		Conn.addView(this);
		this.parent = parent;
		redraw(null);
	}
	
	public void setFocus() {
		//This section was intentionally left empty
	}
	
	public void redraw(final MBeanTreeNode activeNode) {

		Tools.cleanComposite(parent);

		if(Conn.getMode() == Conn.NOT_CONNECTED) {
			new Label(parent, SWT.NONE).setText(Conn.getStatus());
			return;
		}

		List list = null;

		int min_width = 0;
		int min_height = 0;

		ops_SCROLLCOMPOSITE = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		ops_SCROLLCOMPOSITE.setMinSize(600, 600);
		ops_SCROLLCOMPOSITE.setExpandHorizontal(true);
		ops_SCROLLCOMPOSITE.setExpandVertical(true);

		ops_COMPOSITE = new Composite(ops_SCROLLCOMPOSITE, SWT.NONE);
		ops_COMPOSITE.setLayout(new GridLayout(1, false));

		ops_SCROLLCOMPOSITE.setContent(ops_COMPOSITE);

		if(activeNode == null || activeNode.getName() == null) {
			new Label(ops_COMPOSITE, SWT.NONE).setText("No operations available");
			ops_COMPOSITE.layout(true);
			ops_SCROLLCOMPOSITE.layout(true);
			parent.layout(true);
			return;
		}
		
		try {
			list = Conn.getMBeanOperations().listOperations(activeNode.getName());
		} catch (Exception e) {
			Tools.fireErrorDialog(parent, e);
			e.printStackTrace();
			return;
		}

		OperationInfo ops_info[] = (OperationInfo[]) list
				.toArray(new OperationInfo[0]);

		if (ops_info.length == 0) {
			new Label(ops_COMPOSITE, SWT.NONE)
					.setText("No operations available.");
		}

		for (int i = 0; i < ops_info.length; i++) {

			if (i != 0) {
				Label separator_LABEL = new Label(ops_COMPOSITE, SWT.SEPARATOR
						| SWT.HORIZONTAL);
				separator_LABEL.setSize(320, 10);
			}

			Composite op_row_COMPOSITE = new Composite(ops_COMPOSITE, SWT.NONE);
			op_row_COMPOSITE.setLayout(new GridLayout(3, false));

			Label return_type_method_LABEL = new Label(op_row_COMPOSITE,
					SWT.NONE);
			return_type_method_LABEL.setText(ops_info[i].getReturnType() + " "
					+ ops_info[i].getName() + "(");
			return_type_method_LABEL
					.setToolTipText("The name and the return value of operation");

			final String[] signature = ops_info[i].getSign();

			if (signature.length != 0) {
				new Label(op_row_COMPOSITE, SWT.NONE);
				new Label(op_row_COMPOSITE, SWT.NONE);
			}
			final Text vals_TEXT[] = new Text[signature.length];

			for (int j = 0; j < signature.length; j++) {

				Label type_LABEL = new Label(op_row_COMPOSITE, SWT.NONE);
				type_LABEL.setText("    " + signature[j] + ":");
				vals_TEXT[j] = new Text(op_row_COMPOSITE, SWT.BORDER);
				vals_TEXT[j].setToolTipText("Edit the parameter");

				if (j != (signature.length - 1)) {
					new Label(op_row_COMPOSITE, SWT.NONE).setText(", ");
				}
			}

			Label parenthesis_r_LABEL = new Label(op_row_COMPOSITE, SWT.NONE);
			parenthesis_r_LABEL.setAlignment(SWT.CENTER);
			parenthesis_r_LABEL.setText(")");

			Button invoke_operation_BUTTON = new Button(op_row_COMPOSITE,
					SWT.NONE);
			invoke_operation_BUTTON.setText("Invoke");
			invoke_operation_BUTTON
					.setToolTipText("Invokes the named operation");

			op_row_COMPOSITE.pack();

			int w = op_row_COMPOSITE.getSize().x;
			int h = op_row_COMPOSITE.getSize().y;

			min_width = (w > min_width) ? w : min_width;
			min_height += h;

			ops_SCROLLCOMPOSITE.setMinSize(min_width, min_height);

			final String operation = ops_info[i].getName();
			invoke_operation_BUTTON
					.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							String[] vals = new String[signature.length];

							for (int j = 0; j < signature.length; j++) {
								vals[j] = vals_TEXT[j].getText();
							}

							Object result = null;

							try {
								OutputView.print("<< Invoking operation >>");
								OutputView.print("MBean="
										+ activeNode.getName() + ", op="
										+ operation + ", vals=" + vals
										+ ", signature=" + signature);
								result = Conn.getMBeanOperations().execute(
										activeNode.getName(), operation,
										vals, signature);
							} catch (Exception exc) {
								Tools.fireErrorDialog(parent, exc);
								OutputView.print(exc);
								return;
							}

							new ResultDialog(parent, activeNode.getName(), operation, vals, result);
						}
					});
		}
	

	parent.layout(true);
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
