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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.management.Notification;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;


public class NotificationView extends ViewPart implements IMBeanTreeDependant {

	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.notification";

	/*
	 * The Hashtable containing notifications sent by MBeans.
	 * key - MBean Name (String)
	 * value - Vector of notifications 
	 */
	private Hashtable notifications_H = new Hashtable();

	private Table notifications_TABLE = null;

	private Composite notifications_COMPOSITE = null;

	private Button subscribe_BUTTON = null;

	private Button clear_BUTTON = null;

	private Button update_BUTTON = null;
	
	Composite parent = null;
	
	private MBeanTreeNode node = null;
	
	private boolean subscribed;
	
	public void createPartControl(Composite parent) {

		Conn.addView(this);

		this.parent = parent;
		/*
		 * The Composite containing table of notifications caught and Subscribe/Unsubscribe of notifications.
		 */
		notifications_COMPOSITE = new Composite(parent, SWT.NONE);

		notifications_TABLE = new Table(notifications_COMPOSITE, SWT.BORDER);

		notifications_TABLE.setHeaderVisible(true);
		notifications_TABLE.setLinesVisible(true);

		notifications_TABLE.pack();

		subscribe_BUTTON = new Button(notifications_COMPOSITE, SWT.CHECK);
		subscribe_BUTTON.setText("Subscribe");
		subscribe_BUTTON
				.setToolTipText("Subscribe/Unsubscribe to notifications of current MBean");
		final Composite fParent = parent; 
		subscribe_BUTTON.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {


				try {
					Button button = (Button) event.widget;

					if (button.getSelection()) {
						Conn.getNotificationHandler().subscribe(node.getName());
						notifications_H.put(node.getName(), new Vector());
						update_BUTTON.setEnabled(true);
						clear_BUTTON.setEnabled(true);
						node.setSubscribed(true);
					} else {
						Conn.getNotificationHandler().unsubscribe(node.getName());
						notifications_H.put(node.getName(), new Vector());
						update_BUTTON.setEnabled(false);
						clear_BUTTON.setEnabled(false);
						node.setSubscribed(false);
					}
				} catch (Exception exc) {
					subscribe_BUTTON.setSelection(false);
					Tools.fireErrorDialog(fParent, exc);
					exc.printStackTrace();
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

				notifications_TABLE.removeAll();
				node.getNotifications().removeAllElements();
			}
		});

		update_BUTTON.setText("Update");

		update_BUTTON.addSelectionListener(new SelectionAdapter() {

			public synchronized void widgetSelected(SelectionEvent arg0) {

				Vector new_notifications = null;

				try {
					new_notifications = 
						Conn.getNotificationHandler().getNotifications(node.getName());
				} catch (Exception exc) {
					Tools.fireErrorDialog(fParent, exc);
					exc.printStackTrace();
					return;
				}

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

	}

	public void setFocus() {
		
	}
	
	private void refillNotificationTable(Table notifications_TABLE, MBeanTreeNode node) {

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
	
	public void redraw(MBeanTreeNode node) {

		if (node == null) {
			return;
		}
		
		refillNotificationTable(notifications_TABLE, node);
	}

	public Composite getParent() {
		return parent;
	}

	public MBeanTreeListener getMBeanTreeListener() {
		return new MBeanTreeListener(this);
	}

	public boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean val) {
		subscribed = val;
	}
}
