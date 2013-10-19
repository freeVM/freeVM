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

package org.apache.harmony.x.management.console.standalone;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * The visualization of Error Dialog.
 * @author Victor A. Martynov
 * @version $Revision: 1.3 $
 */
class ErrorDialog {

	private Shell error_SHELL = null;
	
	/**
	 * This method initializes error_SHELL	
	 *
	 */
	ErrorDialog(Shell parent, Throwable exception) {
		
		/*
		 * Printing the stack trace into byte array, in order to visualize it.
		 */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		exception.printStackTrace(ps);
		final String stackTrace = new String(baos.toByteArray());
		
		error_SHELL = new Shell(parent);
		error_SHELL.setText("Error");
		error_SHELL.setSize(500, 100);
		error_SHELL.setImage(Main.errorIcon);
		
		/*
		 * Border width.
		 */
		final int bw = error_SHELL.getBorderWidth();
		
		final Button ok_BUTTON = new Button(error_SHELL, SWT.NONE);
		
		ok_BUTTON.setBounds(new Rectangle(250, 50, 100, 25));
		ok_BUTTON.setText("OK");
		CLabel error_CLABEL = new CLabel(error_SHELL, SWT.NONE);
		error_CLABEL.setText(exception.getMessage());
		error_CLABEL.setBounds(new org.eclipse.swt.graphics.Rectangle(1, 5, 500, 30));

		ok_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				error_SHELL.close();
				error_SHELL.dispose();
				error_SHELL = null;
			}
		});

		final Text details_TEXT = new Text(error_SHELL, SWT.V_SCROLL | SWT.H_SCROLL);
		details_TEXT.setText(stackTrace);
		details_TEXT.setBounds(0,0,0,0);

		final Button openDetails_BUTTON = new Button(error_SHELL, SWT.NONE);
		final Button closeDetails_BUTTON = new Button(error_SHELL, SWT.NONE);
		
		closeDetails_BUTTON.setVisible(false);

		openDetails_BUTTON.setBounds(390-2*bw, 50, 100, 25);
		closeDetails_BUTTON.setBounds(0,0,0,0);

		openDetails_BUTTON.setText("Details >>");
		closeDetails_BUTTON.setText("Details <<");
		
		openDetails_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				
					details_TEXT.setBounds(bw, 100, 500-2*bw, 340);
				
					openDetails_BUTTON.setBounds(0,0,0,0);
					closeDetails_BUTTON.setBounds(390-2*bw, 450, 100, 25);
					
					openDetails_BUTTON.setVisible(false);
					closeDetails_BUTTON.setVisible(true);

					error_SHELL.setSize(500, 500);
					ok_BUTTON.setBounds(250, 450, 100, 25);
			}
		});

		closeDetails_BUTTON.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
					details_TEXT.setBounds(0,0,0,0);

					openDetails_BUTTON.setBounds(400-2*bw, 50, 100, 25);
					closeDetails_BUTTON.setBounds(0,0,0,0);
					
					openDetails_BUTTON.setVisible(true);
					closeDetails_BUTTON.setVisible(false);

					error_SHELL.setSize(500, 100);
					ok_BUTTON.setBounds(250, 50, 100, 25);
			}
		});

		error_SHELL.pack();
		error_SHELL.open();
	}
}	