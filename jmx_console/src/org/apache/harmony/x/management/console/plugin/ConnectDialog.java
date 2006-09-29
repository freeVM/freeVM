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
 * @version $Revision: 1.5 $
 */
package org.apache.harmony.x.management.console.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author Victor A. Martynov
 * @version $Revision: 1.5 $
 */
public class ConnectDialog  {
	
	Composite dialog; 
	TabFolder connect_TABFOLDER = null;
	
	TabItem vm_TI = null;
	TabItem create_server_TI = null;
	TabItem url_TI = null;

	private Composite vm_COMPOSITE = null;
	private Composite create_server_COMPOSITE = null;
	private Composite url_COMPOSITE = null;
	
	private Label user_name_LABEL = null;
	private Label password_LABEL = null;
	
	Text jmx_url_TEXT = null;
	Text user_name_TEXT = null;
	Text password_TEXT = null;
	
	private Label jmx_url_LABEL = null;

	private Text default_domain_TEXT = null;
	private Label default_domain_LABEL = null;

	private Label JMXConnectorPort_LABEL = null;
	private Text JMXConnectorPort_TEXT = null;

	private static int WIDTH = 400;
	private static int HEIGHT = 250;

	private static int LW = 100; //Label width
	private static int H = 20; //Label width
	
	private static int TW = 200; //Text width
	private static int SP = 10; // Space between components
	
	private Button startJMXConnector_BUTTON = null;

	
	public ConnectDialog(Composite parent) {
		if(Conn.DEBUG) OutputView.print("ConnectWizardPage.createControl(Composite)");
		
		connect_TABFOLDER = new TabFolder(parent, SWT.NONE);
		connect_TABFOLDER.setBounds(0, 0, WIDTH, HEIGHT);

		url_TI = new TabItem(connect_TABFOLDER, SWT.NONE);
		 
		create_server_TI = new TabItem(connect_TABFOLDER, SWT.NONE);

		vm_TI = new TabItem(connect_TABFOLDER, SWT.NONE);

		vm_TI.setText("Local VM");
		
		create_server_TI.setText("New Server");

		url_TI.setText("Remote URL");

		vm_TI.setToolTipText("Connect to VM locally");
		create_server_TI.setToolTipText("Create new MBean server");

		url_TI.setToolTipText("Connect to MBean Server using full URL and user credentials");

		 vm_COMPOSITE = new Composite(connect_TABFOLDER, SWT.NONE);

		 create_server_COMPOSITE = new Composite(connect_TABFOLDER, SWT.NONE);

		 url_COMPOSITE = new Composite(connect_TABFOLDER, SWT.NONE);
		
		vm_TI.setControl(vm_COMPOSITE);
		create_server_TI.setControl(create_server_COMPOSITE);
		url_TI.setControl(url_COMPOSITE);
		
		fill_create_server_COMPOSITE();
		fill_url_COMPOSITE();
		fill_vm_COMPOSITE();
		
		if(Conn.DEBUG) OutputView.print("ConnectWizardPage.createControl(Composite) finished.");
	}

	/* ************************************************************************
	 * Private methods.
	 * ***********************************************************************/
	
	/*
	 * Filling URL composite.
	 *
	 */
	private void fill_url_COMPOSITE() {

		
		jmx_url_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		jmx_url_LABEL.setText("JMX URL:");
		jmx_url_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		jmx_url_LABEL.setBounds(SP, SP, LW, H);
		jmx_url_TEXT.setBounds(LW+2*SP, SP, TW, H);

		user_name_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		user_name_LABEL.setText("User Name:");
		user_name_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		user_name_LABEL.setBounds(SP, H+2*SP, LW, H);
		user_name_TEXT.setBounds(LW+2*SP, H+2*SP, TW, H);

		
		password_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		password_LABEL.setText("Password:");
		password_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		password_LABEL.setBounds(SP, 2*H+3*SP, LW, H);
		password_TEXT.setBounds(LW+2*SP, 2*H+3*SP, TW, H);
	}
		
		/* 
		 * Filling server composite
		 */
		void fill_create_server_COMPOSITE() {

			GridLayout gridLayout = new GridLayout(2, false);

			gridLayout.horizontalSpacing = 20;
			gridLayout.verticalSpacing = 10;
			
			create_server_COMPOSITE.setLayout(gridLayout);
			
			default_domain_LABEL = new Label(create_server_COMPOSITE, SWT.NONE);
			default_domain_LABEL.setText("Default Domain: ");
			
			default_domain_TEXT = new Text(create_server_COMPOSITE, SWT.BORDER);
			default_domain_TEXT.setText("DefaultDomain");
			default_domain_TEXT.setToolTipText("The default domain of a newly created JMX server");

			new Label(create_server_COMPOSITE, SWT.SEPARATOR | SWT.HORIZONTAL);
			new Label(create_server_COMPOSITE, SWT.NONE); //Placeholder
			
			startJMXConnector_BUTTON = new Button(create_server_COMPOSITE, SWT.CHECK);
			startJMXConnector_BUTTON.setText("Start JMX Connector");
			startJMXConnector_BUTTON.setSelection(false);
			startJMXConnector_BUTTON.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					if(startJMXConnector_BUTTON.getSelection()) {
						JMXConnectorPort_LABEL.setEnabled(true);
						JMXConnectorPort_TEXT.setEnabled(true);
					} else {
						JMXConnectorPort_LABEL.setEnabled(false);
						JMXConnectorPort_TEXT.setEnabled(false);
						JMXConnectorPort_TEXT.setText("");
					}
				}
			});
			
			new Label(create_server_COMPOSITE, SWT.NONE); //Placeholder 
			
			JMXConnectorPort_LABEL = new Label(create_server_COMPOSITE, SWT.NONE);
			JMXConnectorPort_LABEL.setText("Port: ");
			
			JMXConnectorPort_TEXT = new Text(create_server_COMPOSITE, SWT.BORDER);
			JMXConnectorPort_TEXT.setText("");
			JMXConnectorPort_TEXT.setToolTipText("The port on which JMX connector will start");
			
			JMXConnectorPort_LABEL.setEnabled(false);
			JMXConnectorPort_TEXT.setEnabled(false);
		}

		private void fill_vm_COMPOSITE() {
			vm_COMPOSITE.setLayout(new FillLayout());
			new Label(vm_COMPOSITE, SWT.NONE).setText("Manages the local JVM");
		}
		
		void saveConnection() throws Exception {
			TabItem selectedTab = connect_TABFOLDER.getSelection()[0];

			Conn.resetController();

			/*
			 * VM tab selected.
			 */
			if(selectedTab.equals(vm_TI)) {
					Conn.setMode(Conn.VM_MODE);
					Conn.getController().connect();

			} else if (selectedTab.equals(create_server_TI)) { //New Server tab
				int port;
				
				Conn.setMode(Conn.NEW_SERVER_MODE);
				
				String portS = JMXConnectorPort_TEXT.getText();
				if(portS.equals("")) {
					portS = "0";
				}
					
				port = new Integer(portS).intValue();
				Conn.setPort(port); 
				Conn.setStartJMXConnector(startJMXConnector_BUTTON.getSelection());
				Conn.setDefaultDomain(default_domain_TEXT.getText());
				Conn.getController().connect(default_domain_TEXT.getText());

			} else if(selectedTab.equals(url_TI)) { //URL tab selected
				Conn.setMode(Conn.URL_MODE);
				Conn.setUrl(jmx_url_TEXT.getText());
				Conn.setUser(user_name_TEXT.getText());
				Conn.setPass(password_TEXT.getText());
				
				Conn.getController().connect(jmx_url_TEXT.getText(),
                                             user_name_TEXT.getText(), 
                                             password_TEXT.getText());
			}
		}
			
}
