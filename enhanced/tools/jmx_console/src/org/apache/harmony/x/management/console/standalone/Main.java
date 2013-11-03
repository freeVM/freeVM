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

package org.apache.harmony.x.management.console.standalone;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.management.Notification;
import javax.management.remote.JMXServiceURL;

import org.apache.harmony.x.management.console.controller.AttributeInfo;
import org.apache.harmony.x.management.console.controller.Controller;
import org.apache.harmony.x.management.console.controller.ControllerFactory;
import org.apache.harmony.x.management.console.controller.MBeanOperations;
import org.apache.harmony.x.management.console.controller.NotificationHandler;
import org.apache.harmony.x.management.console.controller.OperationInfo;
import org.apache.harmony.x.management.console.controller.VMMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * An SWT interface for the JMX Controller.
 * @author Victor A. Martynov
 * @version $Revision: 1.5 $
 */
public class Main {

	private static final String APPLICATION_NAME = "JMX Console";
	private static final String CONFIGURATION_FILE_NAME = "console.cfg";
	private static final long REFRESH_RATE = 1000L;

	private static Timer timer = new Timer();

	static Display display = null;

	Shell main_SHELL = null;

	/*
	 * Controller and other fields initialized by controller.
	 */
	private Controller controller = ControllerFactory.getController();

	private MBeanOperations mbo = null;

	private VMMonitor vmmon = null;

	private NotificationHandler notif_handler = null;

	private TabFolder main_TABFOLDER = null;

	private TabItem commonView_TABITEM = null;

	private TabItem VMView_TABITEM = null;

	Composite commonView_COMPOSITE = null;

	Composite VMView_COMPOSITE = null;

	private TabFolder edit_mbean_TABFOLDER = null;

	private Tree mbean_TREE = null;

	private TabItem attributes_TI = null;

	private TabItem operations_TI = null;

	private TabItem notifications_TI = null;

	private ScrolledComposite attrs_SCROLLCOMPOSITE = null;
	private Composite attrs_toolbar_COMPOSITE = null;
	private Composite attrs_COMPOSITE = null;

	private Composite attrs_table_COMPOSITE = null;

	private ScrolledComposite ops_SCROLLCOMPOSITE = null;

	private Composite ops_COMPOSITE = null;

	private Table notifications_TABLE = null;

	private Composite notifications_COMPOSITE = null;

	/*
	 * The buttons that lay on the notifications pane.
	 */
	private Button subscribe_BUTTON = null;

	private Button clear_BUTTON = null;

	private Button update_BUTTON = null;

	private Composite query_COMPOSITE = null;

	/*
	 * Icons and images. 
	 */
	static Image mainIcon = null;

	static Image errorIcon = null;

	static Image resultIcon = null;

	static Image connectIcon = null;

	/*
	 * Current MBEan
	 */
	private String mbean_name = null;

	private Composite classLoadingMXBean_COMPOSITE = null;

	private Composite compilationMXBean_COMPOSITE = null;

	private Composite garbageCollectorMXBean_COMPOSITE = null;

	private Composite memoryManagerMXBean_COMPOSITE = null;

	private Composite memoryMXBean_COMPOSITE = null;

	private Composite memoryPoolMXBean_COMPOSITE = null;

	private Composite operatingSystemMXBean_COMPOSITE = null;

	private Composite runtimeMXBean_COMPOSITE = null;

	private Composite threadMXBean_COMPOSITE = null;

	/*
	 * The Hashtable containing notifications sent by MBeans.
	 * key - MBean Name (String)
	 * value - Vector of notifications 
	 */
	private Hashtable notifications_H = new Hashtable();

	private void createView(String title) {

		notif_handler = controller.getNotificationService();
		// Cleaning data from previous connection.
		if (main_TABFOLDER != null) {
			main_TABFOLDER.dispose();
			main_TABFOLDER = null;
		}

		main_TABFOLDER = new TabFolder(main_SHELL, SWT.NONE);

		commonView_COMPOSITE = new Composite(main_TABFOLDER, SWT.NONE);
		VMView_COMPOSITE = new Composite(main_TABFOLDER, SWT.NONE);

		commonView_COMPOSITE.setLayout(new FillLayout());
		VMView_COMPOSITE.setLayout(new FillLayout());

		commonView_TABITEM = new TabItem(main_TABFOLDER, SWT.NONE);
		commonView_TABITEM.setControl(commonView_COMPOSITE);
		commonView_TABITEM.setText("Common View");

		mbo = controller.getMBeanOperations();

		main_SHELL.setText(Main.APPLICATION_NAME + ": " + title);

		/**
		 * Creating tabfolder and tree.
		 */
		createMbean_TREE(commonView_COMPOSITE);
		createEdit_mbean_TABFOLDER(commonView_COMPOSITE);

		attrs_COMPOSITE = new Composite(edit_mbean_TABFOLDER, SWT.NONE);
		
		attrs_toolbar_COMPOSITE = new Composite(attrs_COMPOSITE, SWT.BORDER);
		attrs_SCROLLCOMPOSITE = new ScrolledComposite(attrs_COMPOSITE, SWT.H_SCROLL | SWT.V_SCROLL);
		
		edit_mbean_TABFOLDER.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent event) {
				int w = attrs_COMPOSITE.getSize().x;
				int h = attrs_COMPOSITE.getSize().y;
				
				attrs_toolbar_COMPOSITE.setBounds(0, 0, w, 40);
				attrs_SCROLLCOMPOSITE.setBounds(0, 40, w, h-40);
			}
			
		});
		
		
		Button set_values_BUTTON = new Button(attrs_toolbar_COMPOSITE, SWT.PUSH);
		set_values_BUTTON.setText("Set Values");
		set_values_BUTTON.setBounds(90, 5, 100, 30);

		Button update_values_BUTTON = new Button(attrs_toolbar_COMPOSITE, SWT.PUSH);
		update_values_BUTTON.setText("Update Values");
		update_values_BUTTON.setBounds(210, 5, 100, 30);
		

		attrs_SCROLLCOMPOSITE.setMinSize(600, 600);
		attrs_SCROLLCOMPOSITE.setExpandHorizontal(true);

		attrs_SCROLLCOMPOSITE.setExpandVertical(true);

		attrs_table_COMPOSITE = new Composite(attrs_SCROLLCOMPOSITE, SWT.NONE);
		attrs_table_COMPOSITE.setLayout(new GridLayout(2, false));

		attrs_SCROLLCOMPOSITE.setContent(attrs_table_COMPOSITE);

		/**
		 * The Composite containing operations.
		 */
		ops_SCROLLCOMPOSITE = new ScrolledComposite(edit_mbean_TABFOLDER,
				SWT.H_SCROLL | SWT.V_SCROLL);

		ops_SCROLLCOMPOSITE.setMinSize(600, 600);
		ops_SCROLLCOMPOSITE.setExpandHorizontal(true);
		ops_SCROLLCOMPOSITE.setExpandVertical(true);

		ops_COMPOSITE = new Composite(ops_SCROLLCOMPOSITE, SWT.NONE);
		ops_COMPOSITE.setLayout(new GridLayout(1, false));

		ops_SCROLLCOMPOSITE.setContent(ops_COMPOSITE);

		/**
		 * The Composite containing table of notifications caught and Subscribe/Unsubscribe of notifications.
		 */
		notifications_COMPOSITE = new Composite(edit_mbean_TABFOLDER, SWT.NONE);

		notifications_TABLE = new Table(notifications_COMPOSITE, SWT.BORDER);

		notifications_TABLE.setHeaderVisible(true);
		notifications_TABLE.setLinesVisible(true);

		notifications_TABLE.pack();

		subscribe_BUTTON = new Button(notifications_COMPOSITE, SWT.CHECK);
		subscribe_BUTTON.setText("Subscribe");
		subscribe_BUTTON
				.setToolTipText("Subscribe/Unsubscribe to notifications of current MBean");

		subscribe_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				TreeItem[] selected = mbean_TREE.getSelection();
				MBeanTreeNode node = (MBeanTreeNode) selected[0].getData();

				try {
					Button button = (Button) event.widget;

					if (button.getSelection()) {
						notif_handler.subscribe(mbean_name);
						notifications_H.put(mbean_name, new Vector());
						update_BUTTON.setEnabled(true);
						clear_BUTTON.setEnabled(true);
						node.setSubscribed(true);
					} else {
						notif_handler.unsubscribe(mbean_name);
						notifications_H.put(mbean_name, new Vector());
						update_BUTTON.setEnabled(false);
						clear_BUTTON.setEnabled(false);
						node.setSubscribed(false);
					}
				} catch (Exception exc) {
					subscribe_BUTTON.setSelection(false);
					fireErrorDialog(main_SHELL, exc);
					return;
				}
			}
		});

		clear_BUTTON = new Button(notifications_COMPOSITE, SWT.PUSH);
		update_BUTTON = new Button(notifications_COMPOSITE, SWT.PUSH);

		clear_BUTTON.setEnabled(false);
		update_BUTTON.setEnabled(false);

		clear_BUTTON.setText("Clear List");
		clear_BUTTON.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent arg0) {

				TreeItem[] selected = mbean_TREE.getSelection();
				MBeanTreeNode node = (MBeanTreeNode) selected[0].getData();

				notifications_TABLE.removeAll();
				node.getNotifications().removeAllElements();
			}
		});

		update_BUTTON.setText("Update");

		update_BUTTON.addSelectionListener(new SelectionAdapter() {

			public synchronized void widgetSelected(SelectionEvent arg0) {

				Vector new_notifications = null;

				try {
					new_notifications = notif_handler
							.getNotifications(mbean_name);
				} catch (Exception exc) {
					fireErrorDialog(main_SHELL, exc);
					return;
				}

				TreeItem[] selected = mbean_TREE.getSelection();
				MBeanTreeNode node = (MBeanTreeNode) selected[0].getData();
				node.getNotifications().addAll(new_notifications);
				refillNotificationTable(notifications_TABLE, node);
			}
		});

		final TableColumn source_TCOL = new TableColumn(notifications_TABLE,
				SWT.NONE);
		final TableColumn message_TCOL = new TableColumn(notifications_TABLE,
				SWT.NONE);
		final TableColumn sequence_number_TCOL = new TableColumn(
				notifications_TABLE, SWT.NONE);
		final TableColumn time_stamp_TCOL = new TableColumn(
				notifications_TABLE, SWT.NONE);
		final TableColumn type_TCOL = new TableColumn(notifications_TABLE,
				SWT.NONE);
		final TableColumn user_data_TCOL = new TableColumn(notifications_TABLE,
				SWT.NONE);

		source_TCOL.setText("Source");
		message_TCOL.setText("Message");
		sequence_number_TCOL.setText("Sequence Number");
		time_stamp_TCOL.setText("Time Stamp");
		type_TCOL.setText("Type");
		user_data_TCOL.setText("User Data");

		notifications_COMPOSITE.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				int w = notifications_COMPOSITE.getSize().x;
				int h = notifications_COMPOSITE.getSize().y;

				notifications_TABLE.setBounds(0, 0, w, h - 50);

				source_TCOL.setWidth(w / 6);
				message_TCOL.setWidth(w / 6);
				sequence_number_TCOL.setWidth(w / 6);
				time_stamp_TCOL.setWidth(w / 6);
				type_TCOL.setWidth(w / 6);
				user_data_TCOL.setWidth(w / 6);
				subscribe_BUTTON.setBounds(w - 300, h - 40, 70, 30);
				clear_BUTTON.setBounds(w - 150, h - 40, 70, 30);
				update_BUTTON.setBounds(w - 75, h - 40, 70, 30);
			}
		});

		attributes_TI.setControl(attrs_COMPOSITE);
		operations_TI.setControl(ops_SCROLLCOMPOSITE);
		notifications_TI.setControl(notifications_COMPOSITE);

		mbean_TREE.setVisible(true);

		/**
		 * Retrieving the domains.
		 */

		String[] domains = null;

		try {
			domains = mbo.getAllDomains();
		} catch (Exception e) {
			fireErrorDialog(main_SHELL, e);
			return;
		}

		for (int i = 0; i < domains.length; i++) {

			String[] mbeans = null;

			try {
				mbeans = mbo.getMBeansOfDomain(domains[i]);
			} catch (Exception e) {
				fireErrorDialog(main_SHELL, e);
				return;
			}

			TreeItem domain_TRI = new TreeItem(mbean_TREE, SWT.NULL);

			domain_TRI.setText(domains[i]);

			for (int j = 0; j < mbeans.length; j++) {
				TreeItem mbean_TRI = new TreeItem(domain_TRI, SWT.NULL);
				mbean_TRI.setText(mbeans[j]);
				mbean_TRI.setData(new MBeanTreeNode());
			}
		}

		/* OK! Lets fill VMView_COMPOSITE!*/

		try {
			vmmon = controller.getVMMonitor();

			VMView_TABITEM = new TabItem(main_TABFOLDER, SWT.NONE);
			VMView_TABITEM.setControl(VMView_COMPOSITE);
			VMView_TABITEM.setText("VM View");

			TabFolder VMView_TABFOLDER = new TabFolder(VMView_COMPOSITE,
					SWT.NONE);

			TabItem classLoadingMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem compilationMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem garbageCollectorMXBean_TABITEM = new TabItem(
					VMView_TABFOLDER, SWT.NONE);
			TabItem memoryManagerMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem memoryMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem memoryPoolMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem operatingSystemMXBean_TABITEM = new TabItem(
					VMView_TABFOLDER, SWT.NONE);
			TabItem runtimeMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem threadMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);

			classLoadingMXBean_TABITEM.setText("Class Loading");
			compilationMXBean_TABITEM.setText("Compilation");
			garbageCollectorMXBean_TABITEM.setText("Garbage Collector");
			memoryManagerMXBean_TABITEM.setText("Memory Manager");
			memoryMXBean_TABITEM.setText("Memory");
			memoryPoolMXBean_TABITEM.setText("Memory Pool");
			operatingSystemMXBean_TABITEM.setText("Operating System");
			runtimeMXBean_TABITEM.setText("Runtime");
			threadMXBean_TABITEM.setText("Threads");

			classLoadingMXBean_TABITEM.setToolTipText("Class Loading");
			compilationMXBean_TABITEM.setToolTipText("Compilation");
			garbageCollectorMXBean_TABITEM.setToolTipText("Garbage Collector");
			memoryManagerMXBean_TABITEM.setToolTipText("Memory Manager");
			memoryMXBean_TABITEM.setToolTipText("Memory");
			memoryPoolMXBean_TABITEM.setToolTipText("Memory Pool");
			operatingSystemMXBean_TABITEM.setToolTipText("Operating System");
			runtimeMXBean_TABITEM.setToolTipText("Runtime");
			threadMXBean_TABITEM.setToolTipText("Threads");

			classLoadingMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			compilationMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			garbageCollectorMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			memoryManagerMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			memoryMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);
			memoryPoolMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			operatingSystemMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			runtimeMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);
			threadMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);

			classLoadingMXBean_TABITEM.setControl(classLoadingMXBean_COMPOSITE);
			compilationMXBean_TABITEM.setControl(compilationMXBean_COMPOSITE);
			garbageCollectorMXBean_TABITEM
					.setControl(garbageCollectorMXBean_COMPOSITE);
			memoryManagerMXBean_TABITEM
					.setControl(memoryManagerMXBean_COMPOSITE);
			memoryMXBean_TABITEM.setControl(memoryMXBean_COMPOSITE);
			memoryPoolMXBean_TABITEM.setControl(memoryPoolMXBean_COMPOSITE);
			operatingSystemMXBean_TABITEM
					.setControl(operatingSystemMXBean_COMPOSITE);
			runtimeMXBean_TABITEM.setControl(runtimeMXBean_COMPOSITE);
			threadMXBean_TABITEM.setControl(threadMXBean_COMPOSITE);

			createVMView();

			//timer.schedule(new VMViewRefresher(), 0L, REFRESH_RATE);	TODO	
		} catch (Exception exc) {
			VMView_COMPOSITE.setEnabled(false);
		}

		/* Refresh all components */
		main_SHELL.layout();
	}

	void refillNotificationTable(Table notifications_TABLE, MBeanTreeNode node) {

		boolean subscribed = node.getSubscribed();
		subscribe_BUTTON.setSelection(subscribed);

		if(!subscribed) {
			clear_BUTTON.setEnabled(false);
			update_BUTTON.setEnabled(false);
			return;
		}
		else {
			clear_BUTTON.setEnabled(true);
			update_BUTTON.setEnabled(true);
		}

		notifications_TABLE.removeAll();

		Vector notifications = (Vector) node.getNotifications();

		if (notifications == null) {
			return;
		}

		Iterator iter = notifications.iterator();

		while (iter.hasNext()) {
			Notification notification = (Notification) iter.next();
			String[] fields = new String[6];
			fields[0] = notification.getSource() + "";
			fields[1] = notification.getMessage();
			fields[2] = notification.getSequenceNumber() + "";
			fields[3] = notification.getTimeStamp() + "";
			fields[4] = notification.getType();
			fields[5] = notification.getUserData() + "";

			TableItem ti = new TableItem(notifications_TABLE, SWT.NONE);
			ti.setText(fields);
		}
	}

	class VMViewRefresher extends TimerTask {
		public void run() {
			if (VMView_COMPOSITE.isDisposed()) {
				timer.cancel();
			} else {
				display.syncExec(new Runnable() {
					public void run() {
						classLoadingMXBean_COMPOSITE.setToolTipText("Rulezz!!");
					}
				});
			}
		}
	}

	void createVMView() {

		operatingSystemMXBean_COMPOSITE.setLayout(new GridLayout(1, false));
		classLoadingMXBean_COMPOSITE.setLayout(new GridLayout(1, false));

		Hashtable hashtable = null;
		Enumeration keys = null;
		/* Filling Operating System Info composite */

		try {
			hashtable = vmmon.getOSInfo();
		} catch (Exception e) {
			fireErrorDialog(main_SHELL, e);
			return;
		}

		keys = hashtable.keys();

		while (keys.hasMoreElements()) {
			Object o = keys.nextElement();
			Label label = new Label(operatingSystemMXBean_COMPOSITE, SWT.NONE);
			label.setText(o + ": " + hashtable.get(o));
		}

		/* Filling Class Loading composite */

		try {
			hashtable = vmmon.getClassLoadingInfo();
		} catch (Exception e) {
			fireErrorDialog(main_SHELL, e);
			return;
		}

		keys = hashtable.keys();

		while (keys.hasMoreElements()) {
			Object o = keys.nextElement();
			Label label = new Label(classLoadingMXBean_COMPOSITE, SWT.NONE);
			label.setText(o + ": " + hashtable.get(o));
		}
	}

	void showView() throws Exception {
		controller.connect();
		createView("Local VM");
	}

	void showView(String defaultDomain, boolean startRMIServer, int port)
			throws Exception {
		controller.connect(defaultDomain);
		String title = startRMIServer ? defaultDomain + ", port=" + port
				: defaultDomain;

		createView(title);
	}

	void showView(String url, String user, String pass) throws Exception {
		JMXServiceURL surl = null;
		controller.connect(url, user, pass);
		surl = new JMXServiceURL(url);
		createView(Main.APPLICATION_NAME + ": " + surl.getHost() + ":"
				+ surl.getPort());
	}

	/**
	 * TODO
	 * The query pane. This pane is intended to show command line to query MBeans and select
	 * a subset of them. 
	 */
	private void createQuery_COMPOSITE() { // TODO
		Label label = new Label(query_COMPOSITE, SWT.NONE);
		label.setText("Query: ");
		label.setBounds(0, 0, 100, 100);
		Text text = new Text(query_COMPOSITE, SWT.BORDER);
	}

	/**
	 * This method initializes edit_mbean_TABFOLDER	
	 *
	 */
	private void createEdit_mbean_TABFOLDER(Composite composite) {

		edit_mbean_TABFOLDER = new TabFolder(composite, SWT.NONE);

		attributes_TI = new TabItem(edit_mbean_TABFOLDER, SWT.NONE);
		operations_TI = new TabItem(edit_mbean_TABFOLDER, SWT.NONE);
		notifications_TI = new TabItem(edit_mbean_TABFOLDER, SWT.NONE);

		attributes_TI.setText("Attributes");
		operations_TI.setText("Operations");
		notifications_TI.setText("Notifications");

		attributes_TI.setToolTipText("Attributes Exposed For View and Edit");
		operations_TI
				.setToolTipText("Operations, That May Be Performed on MBean");
		notifications_TI
				.setToolTipText("Notifications emitted by the selected MBean");

		edit_mbean_TABFOLDER.setVisible(false);
	}

	/**
	 * This method initializes mbean_TREE	
	 *
	 */
	private void createMbean_TREE(Composite composite) {
		mbean_TREE = new Tree(composite, SWT.NONE);

		mbean_TREE.addSelectionListener(new SelectionAdapter() {
			/**
			 * 	The Event that happens when some some element of the MBean Tree is selected.
			 */
			public void widgetSelected(SelectionEvent event) {

				TreeItem treeItem = (TreeItem) event.item;
				MBeanTreeNode node = (MBeanTreeNode) treeItem.getData();

				edit_mbean_TABFOLDER.setVisible(false);

				if (node == null) {
					return;
				}


				TreeItem item = (TreeItem) event.item;
				mbean_name = item.getText();

				cleanComposite(ops_COMPOSITE);
				cleanComposite(attrs_table_COMPOSITE);

				edit_mbean_TABFOLDER.setVisible(true);

				List list = null;

				try {
					list = mbo.listOperations(mbean_name);
				} catch (Exception e) {
					fireErrorDialog(main_SHELL, e);
					return;
				}

				OperationInfo ops_info[] = (OperationInfo[]) list
						.toArray(new OperationInfo[0]);

				int min_width = 0;
				int min_height = 0;

				if (ops_info.length == 0) {
					new Label(ops_COMPOSITE, SWT.NONE)
							.setText("No operations available.");
				}

				for (int i = 0; i < ops_info.length; i++) {

					if (i != 0) {
						Label separator_LABEL = new Label(ops_COMPOSITE,
								SWT.SEPARATOR | SWT.HORIZONTAL);
						separator_LABEL.setSize(320, 10);
					}

					Composite op_row_COMPOSITE = new Composite(ops_COMPOSITE,
							SWT.NONE);
					op_row_COMPOSITE.setLayout(new GridLayout(3, false));

					Label return_type_method_LABEL = new Label(
							op_row_COMPOSITE, SWT.NONE);
					return_type_method_LABEL.setText(ops_info[i]
							.getReturnType()
							+ " " + ops_info[i].getName() + "(");
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

					Label parenthesis_r_LABEL = new Label(op_row_COMPOSITE,
							SWT.NONE);
					parenthesis_r_LABEL.setAlignment(SWT.CENTER);
					parenthesis_r_LABEL.setText(")");

					Button invoke_operation_BUTTON = new Button(
							op_row_COMPOSITE, SWT.NONE);
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
										result = mbo.execute(mbean_name,
												operation, vals, signature);
									} catch (Exception exc) {
										fireErrorDialog(main_SHELL, exc);
										return;
									}

									new ResultDialog(main_SHELL, mbean_name,
											operation, vals, result);
								}
							});
				}
				
				fillAttrsTable(attrs_table_COMPOSITE);

				ops_COMPOSITE.layout(true);


				refillNotificationTable(notifications_TABLE, node);

				edit_mbean_TABFOLDER.setVisible(true);
			}
		});

	}

	
	void fillAttrsTable(Composite tbl_COMPOSITE) {

		List list = null;

		try {
			list = mbo.listAttributes(mbean_name);
		} catch (Exception e) {
			fireErrorDialog(main_SHELL, e);
			return;
		}

		AttributeInfo attrs[] = (AttributeInfo[]) list
				.toArray(new AttributeInfo[0]);

		if (attrs.length == 0) {
			new Label(tbl_COMPOSITE, SWT.NONE)
					.setText("No attributes available.");
		}

		for (int i = 0; i < attrs.length; i++) {
			String attr_name = attrs[i].getName();
			String attr_type = attrs[i].getType();

			int style = SWT.BORDER | SWT.SINGLE;

			if (!attrs[i].isWritable()) {
				style = style | SWT.READ_ONLY;
			}

			int dp = attr_type.lastIndexOf("."); //Last Dot position 
			String short_attr_type = (dp == -1) ? attr_type : attr_type
					.substring(dp + 1);
			Label attr_name_LABEL = new Label(tbl_COMPOSITE, SWT.NONE);
			attr_name_LABEL.setText(attr_name + "[" + short_attr_type
					+ "]: ");
			attr_name_LABEL.setBounds(0, 25 * i, 190, 25);

			Text attr_edit_TEXT = new Text(tbl_COMPOSITE, style);
			attr_edit_TEXT.setBounds(190, 25 * i, 190, 25);

			if (attrs[i].isReadable()) {
				String val = attrs[i].getValue().toString();
				if (val.length() > 100) {
					val = val.substring(0, 100);
				}
				attr_edit_TEXT.setText(val);
			}

			String[] attr_key_type = new String[2];

			attr_key_type[0] = attr_name;
			attr_key_type[1] = attr_type;

			attr_edit_TEXT.setData(attr_key_type);

			attr_edit_TEXT.addKeyListener(new KeyListener() {

				public void keyPressed(KeyEvent ke) {
					if (ke.keyCode == 13) { // Enter pressed
						Text widget = (Text) ke.widget;
						String key_type[] = (String[]) widget.getData();

						try {

							mbo.setAttribute(mbean_name, key_type[0],
									widget.getText(), key_type[1]);
						} catch (Exception e) {
							fireErrorDialog(main_SHELL, e);
							return;
						}
					}
				}

				public void keyReleased(KeyEvent arg0) {
				}
			});

		}

		tbl_COMPOSITE.layout(true);
	}
	/**
	 * This method initializes main_SHELL
	 */
	void create_main_SHELL(Display display) {

		main_SHELL = new Shell(display);
		main_SHELL.setText(APPLICATION_NAME);
		main_SHELL.setImage(mainIcon);
		main_SHELL.setSize(1024, 768);
		main_SHELL.setMinimumSize(640, 480);

		main_SHELL.setLayout(new FillLayout());

		Menu main_M = new Menu(main_SHELL, SWT.BAR);
		main_SHELL.setMenuBar(main_M);

		Menu connection_M = new Menu(main_SHELL, SWT.DROP_DOWN);
		Menu window_M = new Menu(main_SHELL, SWT.DROP_DOWN);

		MenuItem connection_MI = new MenuItem(main_M, SWT.CASCADE);
		MenuItem window_MI = new MenuItem(main_M, SWT.CASCADE);
		MenuItem help_MI = new MenuItem(main_M, SWT.CASCADE);

		help_MI.setText("&Help");
		help_MI.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				new HelpDialog(main_SHELL);
			}

		});
		MenuItem new_connection_MI = new MenuItem(connection_M, SWT.CASCADE);
		new_connection_MI.setText("&New Connection...");
		new MenuItem(connection_M, SWT.SEPARATOR);

		new_connection_MI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				new ConnectDialog(Main.this);
			}
		});

		MenuItem exit_MI = new MenuItem(connection_M, SWT.CASCADE);
		exit_MI.setText("&Exit");
		exit_MI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				timer.cancel();
				timer = null;
				main_SHELL.close();
			}
		});

		connection_MI.setText("&Connection");
		connection_MI.setMenu(connection_M);

		window_MI.setText("&Window");
		window_MI.setMenu(window_M);
		main_SHELL.open();
	}

	/**
	 * Cause the error dialog to appear.
	 * @param message
	 */
	static void fireErrorDialog(Shell parent, Throwable exception) {
		new ErrorDialog(parent, exception);
	}

	private void cleanComposite(Composite composite) {
		Control[] controls = composite.getChildren();
		for (int i = 0; i < controls.length; i++) {
			controls[i].dispose();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		display = Display.getDefault();

		/*
		 * Initializing images.
		 */
		errorIcon = display.getSystemImage(SWT.ICON_WARNING);
		resultIcon = display.getSystemImage(SWT.ICON_INFORMATION);
		connectIcon = display.getSystemImage(SWT.ICON_QUESTION);

		InputStream is = Main.class
				.getResourceAsStream("/icons/9_16x16.PNG");
		ImageData imagedata = new ImageData(is);
		mainIcon = new Image(display, imagedata);

		Main thisClass = new Main();

		thisClass.create_main_SHELL(display);

		/*
		 * Initial Connection Dialog.
		 */
		new ConnectDialog(thisClass);

		while (!thisClass.main_SHELL.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		timer.cancel();
		timer = null;
		display.dispose();
	}

	private class MBeanTreeNode {
		boolean isSubscribed;

		Vector notifications;

		public MBeanTreeNode() {
			isSubscribed = false;
		}

		public void setSubscribed(boolean val) {
			isSubscribed = val;
			if (val) {
				notifications = new Vector();
			} else {
				notifications = null;
			}
		}

		public boolean getSubscribed() {
			return isSubscribed;
		}

		public Vector getNotifications() {
			return notifications;
		}
	}
}
