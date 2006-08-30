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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


class ResultDialog {

	private Shell result_SHELL = null;

	private Button res_ok_BUTTON = null;
	private Label  invoke_LABEL = null;
	private Label result_LABEL = null;
	private Composite result_COMPOSITE = null;

	/**
	 * This method initializes result_SHELL.
	 * 
	 * @param parent - the parent Shell of this ResultDialog
	 * @param mbean   the name of the MBean which operation was invoked.
	 * @param op - the name of the operation inoked.
	 * @param params - the array of the values of the parameters of the operation.
 	 * @param result - the object returned from the invocation of the operation.
	 */
	ResultDialog(Composite parent, String mbean, String op, String[] params, Object result) {
		result_SHELL = new Shell(parent.getShell(), SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.RESIZE);
		result_SHELL.setText("Result: ");
		result_SHELL.setMinimumSize(500, 100);
		result_SHELL.setLayout(new FillLayout());
		result_SHELL.setImage(parent.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
		
		result_COMPOSITE = new Composite(result_SHELL, SWT.NONE);
		
		String s = "["+mbean+"]."+op+"(";
		for(int i = 0; i < params.length; i++) {
			if(i != 0) {
				s = s+", ";
			}
			s = s+params[i];
		}
		
		s = s+");";

		invoke_LABEL = new Label(result_COMPOSITE, SWT.NONE);
		invoke_LABEL.setText("Method: "+s);
		
		result_LABEL = new Label(result_COMPOSITE, SWT.NONE);
		result_LABEL.setText("Result: "+ result);

		res_ok_BUTTON = new Button(result_COMPOSITE, SWT.NONE);
		res_ok_BUTTON.setText("OK");

		res_ok_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				result_SHELL.close();
				result_SHELL.dispose();
			}
		});

		result_COMPOSITE.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				int w = result_COMPOSITE.getSize().x;
				int h = result_COMPOSITE.getSize().y;
				invoke_LABEL.setBounds(0, 0, w, h/3);
				result_LABEL.setBounds(0, h/3, w, h/3);
				res_ok_BUTTON.setBounds(w/2-50, h/3 > 24 ? h-24 : h*2/3 + 2, 100, (h/3-4) > 20? 20:(h/3-4)); 
			}
		});

		result_SHELL.pack();
		result_SHELL.open();
	}
}
