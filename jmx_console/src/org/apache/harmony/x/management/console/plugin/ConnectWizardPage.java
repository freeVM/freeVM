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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class ConnectWizardPage extends WizardPage {

	ConnectDialog cd;
	
	public ConnectWizardPage() {
		super("Default Page Name");
		if(Conn.DEBUG) OutputView.print("ConnectWizardPage.<init>()");
	}
	
	protected ConnectWizardPage(String pageName) {
		super(pageName);
		if(Conn.DEBUG) OutputView.print("ConnectWizardPage.<init>(String)");
	}

	public void createControl(Composite parent) {

		if(Conn.DEBUG) OutputView.print("ConnectWizardPage.createControl("+parent+")");

		cd = new ConnectDialog(parent);
		setControl(parent);
	}
	
	void saveConnection() throws Exception {
		cd.saveConnection();
	}
}
