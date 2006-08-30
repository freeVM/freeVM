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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ConnectWizard extends Wizard implements INewWizard {

	private static final boolean DEBUG = true;
	ConnectWizardPage page = new ConnectWizardPage("New Connection");
	
	public ConnectWizard() {
		super();
		this.setWindowTitle("Connect Wizard Title");
	}
	
	public boolean performFinish() {
		OutputView.print("performFinish");
		try {
			page.saveConnection();

		MBeanTreeView mbeanTreeView = Conn.getMBeanTreeView();
		
		if(mbeanTreeView != null) {
			mbeanTreeView.fill();
		}

		Conn.printConnection();
		}
		catch(Throwable t) {
			Tools.fireErrorDialog(getShell(), t);
			OutputView.print(t);
			return false;
		}
		
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		OutputView.print("init");
	}
	
	public void addPages() {
		if(DEBUG) OutputView.print("ConnectWizard.addPages");
		addPage(page);
		if(DEBUG) OutputView.print("ConnectWizard.addPages completed.");
	}
	
	public boolean needsPreviousAndNextButtons() {
		return false;
	}
}
