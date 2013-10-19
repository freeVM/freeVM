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
 * @version $Revision: 1.6 $
 */

package org.apache.harmony.x.management.console.standalone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


/**
 * Visualization of a connect dialog.
 * 
 * @author Victor A. Martynov
 * @version $Revision: 1.6 $
 */
class ConnectDialog {
	
	private static int WIDTH = 400;
	private static int HEIGHT = 250;

	private static int MARGIN = 10;

	/**
	 * Button width.
	 */
	private static int BUTTON_WIDTH = 50;
	/**
	 * Button height.
	 */
	private static int BUTTON_HEIGHT = 30;
	
	private static int TEXT_WIDTH = 150;
	
	private static int TEXT_HEIGHT = 25;
	
	/**
	 * The Shell object whic is a container for all of the controls of connect dialog.
	 */
	private Shell connect_SHELL = null;
	
	/*
	 * The components corresponding to connect dialog.
	 */
	
	private TabFolder connect_TABFOLDER = null;
	
	private TabItem vm_TI = null;
	private TabItem create_server_TI = null;
	private TabItem url_TI = null;

	private Composite vm_COMPOSITE = null;
	private Composite create_server_COMPOSITE = null;
	private Composite url_COMPOSITE = null;
	
	private Label user_name_LABEL = null;
	private Label password_LABEL = null;
	
	private Text jmx_url_TEXT = null;
	private Text user_name_TEXT = null;
	private Text password_TEXT = null;
	
	private Label jmx_url_LABEL = null;
	private Button connect_BUTTON = null;
	private Button cancel_BUTTON = null;

	private Text default_domain_TEXT = null;
	private Label default_domain_LABEL = null;

	private Button startJMXConnector_BUTTON = null;

	private Label JMXConnectorPort_LABEL = null;
	private Text JMXConnectorPort_TEXT = null;
	
	Main main = null;
	Shell main_SHELL = null;
	
	/**
	 * Connect dialog. The connect dialog contains 3 tabs: VM, New and URL.
	 */
	ConnectDialog(Main main) {

		this.main = main;
		main_SHELL = main.main_SHELL;
		
		connect_SHELL = new Shell(main_SHELL, SWT.CLOSE|SWT.APPLICATION_MODAL);

		connect_SHELL.setText("New Connection...");
		connect_SHELL.setSize(WIDTH, HEIGHT);
		connect_SHELL.setImage(Main.connectIcon);

		connect_TABFOLDER = new TabFolder(connect_SHELL, SWT.NONE);
		connect_TABFOLDER.setBounds(0, 0, WIDTH, HEIGHT-(BUTTON_HEIGHT+5));

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
		
		connect_BUTTON = new Button(connect_SHELL, SWT.NONE);
		connect_BUTTON.setBounds(WIDTH/2-(BUTTON_WIDTH+5), HEIGHT-(BUTTON_HEIGHT+5),  BUTTON_WIDTH, BUTTON_HEIGHT);
		connect_BUTTON.setText("Connect");
		
		cancel_BUTTON = new Button(connect_SHELL, SWT.NONE);
		cancel_BUTTON.setBounds(WIDTH/2+5, HEIGHT-(BUTTON_HEIGHT+5),  BUTTON_WIDTH, BUTTON_HEIGHT);
		cancel_BUTTON.setText("Cancel");

		connect_BUTTON.addSelectionListener(new ConnectButtonSelectionListener());

		cancel_BUTTON.addSelectionListener(new CancelButtonSelectionListener());

		connect_SHELL.pack();
		connect_SHELL.open();
	}

	/**
	 * The Listener that is invoked when 'Cancel' button is pressed.
	 */
	private class CancelButtonSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			connect_SHELL.close();
		}
	}
	
	/**
	 * The Listener that is invoked when 'Connect' button is pressed.
	 */
	private class ConnectButtonSelectionListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent event) {
			
			TabItem selectedTab = connect_TABFOLDER.getSelection()[0];

			/*
			 * VM tab selected.
			 */
			if(selectedTab.equals(vm_TI)) {
				try {
					main.showView();
				}
				catch (Exception e) {
					Main.fireErrorDialog(connect_SHELL, e);
				}
				connect_SHELL.close();
				connect_SHELL.dispose();
			}
			else if (selectedTab.equals(create_server_TI)) {
				int port;
				try {
					String portS = JMXConnectorPort_TEXT.getText();
					if(portS.equals("")) {
						portS = "0";
					}
					
					port = new Integer(portS).intValue();
					main.showView(default_domain_TEXT.getText(), 
							      startJMXConnector_BUTTON.getSelection(), 
							      port);
				}
				catch(Exception exc) {
					Main.fireErrorDialog(connect_SHELL, exc);
					return;
				}
				
				connect_SHELL.close();
				connect_SHELL.dispose();
			}
			else if(selectedTab.equals(url_TI)) {
				try {
					main.showView(jmx_url_TEXT.getText(), 
							              user_name_TEXT.getText(),
							              password_TEXT.getText());
					connect_SHELL.close();
					connect_SHELL.dispose();
				}
				catch (Exception e) {
					Main.fireErrorDialog(connect_SHELL, e);
					return;
				}
			}
			else {
				throw new RuntimeException("Strange TabItem Selected: "+selectedTab);
			}
		}
	}
	/**
	 * Filling URL composite.
	 *
	 */
	void fill_url_COMPOSITE() {
		jmx_url_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		jmx_url_LABEL.setBounds(MARGIN, MARGIN, TEXT_WIDTH, TEXT_HEIGHT);
		jmx_url_LABEL.setText("JMX URL:");
		jmx_url_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		jmx_url_TEXT.setBounds(2*MARGIN+TEXT_WIDTH, MARGIN, TEXT_WIDTH, TEXT_HEIGHT);

		user_name_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		user_name_LABEL.setBounds(MARGIN, 2*MARGIN+TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		user_name_LABEL.setText("User Name:");
		user_name_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		user_name_TEXT.setBounds(2*MARGIN+TEXT_WIDTH, 2*MARGIN+TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);

		password_LABEL = new Label(url_COMPOSITE, SWT.NONE);
		password_LABEL.setBounds(MARGIN, 3*MARGIN+2*TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
		password_LABEL.setText("Password:");
		password_TEXT = new Text(url_COMPOSITE, SWT.BORDER);
		password_TEXT.setBounds(2*MARGIN+TEXT_WIDTH, 3*MARGIN+2*TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
	}
	
	/**
	 * 
	 */
	void fill_create_server_COMPOSITE() {

		default_domain_LABEL = new Label(create_server_COMPOSITE, SWT.NONE);
		default_domain_LABEL.setText("Default Domain: ");
		default_domain_LABEL.setBounds(5, 5, 150, 25);
		
		default_domain_TEXT = new Text(create_server_COMPOSITE, SWT.BORDER);
		default_domain_TEXT.setText("DefaultDomain");
		default_domain_TEXT.setToolTipText("The default domain of a newly created JMX server");
		default_domain_TEXT.setBounds(155, 5, 150, 25);
		
		startJMXConnector_BUTTON = new Button(create_server_COMPOSITE, SWT.CHECK);
		startJMXConnector_BUTTON.setText("Start JMX Connector");
		startJMXConnector_BUTTON.setToolTipText("Indicates whether the newly created server should be exposed for management");
		startJMXConnector_BUTTON.setBounds(5, 65, 150, 25);
		
		startJMXConnector_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(startJMXConnector_BUTTON.getSelection()) {
					JMXConnectorPort_LABEL.setEnabled(true);
					JMXConnectorPort_TEXT.setEnabled(true);
				}
				else {
					JMXConnectorPort_LABEL.setEnabled(false);
					JMXConnectorPort_TEXT.setText("");
					JMXConnectorPort_TEXT.setEnabled(false);
				}	
			}
		});
		
		JMXConnectorPort_LABEL = new Label(create_server_COMPOSITE, SWT.NONE);
		JMXConnectorPort_LABEL.setText("Port: ");
		JMXConnectorPort_LABEL.setBounds(0, 100, 150, 25);
		
		JMXConnectorPort_TEXT = new Text(create_server_COMPOSITE, SWT.BORDER);
		JMXConnectorPort_TEXT.setText("");
		JMXConnectorPort_TEXT.setToolTipText("The port on which JMX connector will start");
		JMXConnectorPort_TEXT.setBounds(150, 100, 150, 25);
		
		JMXConnectorPort_LABEL.setEnabled(false);
		JMXConnectorPort_TEXT.setEnabled(false);
	}
}
