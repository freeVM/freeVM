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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * The visualization of Error Dialog.
 * @author Victor A. Martynov
 * @version $Revision: 1.3 $
 */
class ErrorDialog {

	public static final int BUTTON_WIDTH = 100;
	public static final int BUTTON_HEIGHT = 25;
	
	private Shell error_SHELL = null;
	private Label error_LABEL = null;
	private Button ok_BUTTON;
	private Text details_TEXT;
	private Button openDetails_BUTTON;
	private boolean isDetailsOpen = false;
	
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
		
		error_SHELL = new Shell(parent, SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.RESIZE);
		error_SHELL.setText("Error");
		error_SHELL.setMinimumSize(500, 100);
		error_SHELL.setImage(parent.getDisplay().getSystemImage(SWT.ICON_ERROR));
		error_SHELL.setLayout(new GridLayout(2, false));
		
		error_LABEL = new Label(error_SHELL, SWT.NONE);
		error_LABEL.setText(exception.getMessage());
		new Label(error_SHELL, SWT.NONE);

		ok_BUTTON = new Button(error_SHELL, SWT.NONE);
		ok_BUTTON.setBounds(250, 0, 100, 25);
		ok_BUTTON.setText("    OK    ");

		ok_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				error_SHELL.close();
				error_SHELL.dispose();
				error_SHELL = null;
			}
		});

		openDetails_BUTTON = new Button(error_SHELL, SWT.NONE);

		openDetails_BUTTON.setBounds(390, 0, 100, 25);

		openDetails_BUTTON.setText("Details >>");
		
		openDetails_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				
				if(isDetailsOpen) {
					isDetailsOpen = false;
					openDetails_BUTTON.setText("Details >>");
					details_TEXT.setText("");
				} else {
					isDetailsOpen = true;
					openDetails_BUTTON.setText("Details <<");
					details_TEXT.setText(stackTrace);
				}
				details_TEXT.setVisible(isDetailsOpen);
				error_SHELL.pack();
			}
		});

		details_TEXT = new Text(error_SHELL, SWT.V_SCROLL | SWT.H_SCROLL);
		details_TEXT.setText("");
		details_TEXT.setVisible(false);

		error_SHELL.pack();
		error_SHELL.open();
	}
}	
