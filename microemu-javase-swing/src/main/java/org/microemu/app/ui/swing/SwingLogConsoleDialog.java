/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.ui.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.microemu.app.Config;
import org.microemu.app.ui.swing.logconsole.LogTextArea;
import org.microemu.log.Logger;
import org.microemu.log.LoggerAppender;
import org.microemu.log.LoggingEvent;
import org.microemu.log.QueueAppender;
import org.microemu.log.StdOutAppender;

public class SwingLogConsoleDialog extends JFrame implements LoggerAppender {

	private static final long serialVersionUID = 1L;

	private static final boolean tests = false;

	private LogTextArea logArea;

	private Vector logLinesQueue = new Vector();

	private int testEventCounter = 0;

	private class SwingLogUpdater implements Runnable {

		private String getNextLine() {
			synchronized (logLinesQueue) {
				if (logLinesQueue.isEmpty()) {
					return null;
				}
				String line = (String) logLinesQueue.firstElement();
				logLinesQueue.removeElementAt(0);
				return line;
			}
		}

		public void run() {
			String line;
			while ((line = getNextLine()) != null) {
				logArea.append(line);
			}
		}
	}

	public SwingLogConsoleDialog(Frame owner, QueueAppender logQueueAppender) {
		super("Log console");

		setIconImage(owner.getIconImage());

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Log");

		JMenuItem menuClear = new JMenuItem("Clear");
		menuClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingLogConsoleDialog.this.logArea.setText("");
			}
		});
		menu.add(menuClear);
		
		menu.addSeparator();
		
		final JCheckBoxMenuItem menuRecordLocation = new JCheckBoxMenuItem("Show record location");
		menuRecordLocation.setState(Logger.isLocationEnabled());
		menuRecordLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.setLocationEnabled(menuRecordLocation.getState());
				Config.setLogConsoleLocationEnabled(menuRecordLocation.getState());
			}
		});
		menu.add(menuRecordLocation);		

		final JCheckBoxMenuItem menuStdOut = new JCheckBoxMenuItem("Write to standard output");
		menuStdOut.setState(StdOutAppender.enabled);
		menuStdOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StdOutAppender.enabled = menuStdOut.getState();
			}
		});
		menu.add(menuStdOut);

		menuBar.add(menu);

		if (tests) {
			JMenu testMenu = new JMenu("Tests");
			JMenuItem testLog = new JMenuItem("Log 10 events");
			testLog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (int i = 0; i < 10; i++) {
						log(testEventCounter++ + " " + new Date() + "\n\t data tests.......\n");
					}
				}
			});
			testMenu.add(testLog);
			menuBar.add(testMenu);
		}

		setJMenuBar(menuBar);

		this.logArea = new LogTextArea(20, 40, 1000);
		JScrollPane scrollPane = new JScrollPane(this.logArea);
		scrollPane.setAutoscrolls(false);

		getContentPane().add(scrollPane);

		LoggingEvent event = null;
		while ((event = logQueueAppender.poll()) != null) {
			append(event);
		}

		Logger.removeAppender(logQueueAppender);
		Logger.addAppender(this);
	}

	public void log(String message) {
		boolean createUpdater = false;
		synchronized (logLinesQueue) {
			if (logLinesQueue.isEmpty()) {
				createUpdater = true;
			}
			logLinesQueue.addElement(message);
		}
		if (createUpdater) {
			SwingUtilities.invokeLater(new SwingLogUpdater());
		}
	}

	private String formatLocation(StackTraceElement ste) {
		if (ste == null) {
			return "";
		}
		return ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber()
				+ ")";
	}

	private String formatEventTime(long eventTime) {
		DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS ");
		return format.format(new Date(eventTime));
	}

	public void append(LoggingEvent event) {
		StringBuffer bug = new StringBuffer(formatEventTime(event.getEventTime()));
		if (event.getLevel() == LoggingEvent.ERROR) {
			bug.append("Error:");
		}
		bug.append(event.getMessage());
		if (event.hasData()) {
			bug.append(" [").append(event.getFormatedData()).append("]");
		}
    	String location = formatLocation(event.getLocation());
    	if (location.length() > 0) {
    		bug.append("\n\t  ");
    	}
		bug.append(location);
		if (event.getThrowable() != null) {
			OutputStream out = new ByteArrayOutputStream();
			PrintStream stream = new PrintStream(out);
			event.getThrowable().printStackTrace(stream);
			stream.flush();
			bug.append(out.toString());
		}
		bug.append("\n");
		log(bug.toString());
	}

}
