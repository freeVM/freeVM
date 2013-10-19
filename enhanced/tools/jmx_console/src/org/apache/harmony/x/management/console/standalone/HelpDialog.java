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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author Victor A. Martynov
 * @version $Revision: 1.3 $
 */
class HelpDialog {

	private Shell help_SHELL = null;
	
	/**
	 * This method initializes error_SHELL	
	 *
	 */
	HelpDialog(Shell parent) {
		
		help_SHELL = new Shell(parent);
		help_SHELL.setText("Help");
		help_SHELL.setSize(500, 500);
		help_SHELL.setImage(Main.resultIcon);
		
		Text help_TEXT = new Text(help_SHELL, SWT.BORDER);
		help_TEXT.setBounds(0, 0, 500, 470);
		help_TEXT.setText("Help Text");
		
		Button ok_BUTTON = new Button(help_SHELL, SWT.NONE);
		
		ok_BUTTON.setBounds(new Rectangle(200, 470, 100, 25));
		ok_BUTTON.setText("OK");

		ok_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				help_SHELL.close();
				help_SHELL.dispose();
				help_SHELL = null;
			}
		});

		help_SHELL.pack();
		help_SHELL.open();
	}
}	