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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.microemu.app.ui.Message;
import org.microemu.app.ui.MessageListener;

/**
 * @author vlads
 *
 */
public class SwingErrorMessageDialogPanel extends SwingDialogPanel implements MessageListener {

	private static final long serialVersionUID = 1L;

	private Frame parent;
	
	private JLabel iconLabel;
	
	private JLabel textLabel;
	
	private JTextArea stackTraceArea;
	
	private JScrollPane stackTracePane;

	/**
	 * @param parent
	 */
	public SwingErrorMessageDialogPanel(Frame parent) {
		this.parent = parent;
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.ipadx = 10;
		c.ipady = 10;
		c.gridx = 0;
		c.gridy = 0;
		iconLabel = new JLabel();
		add(iconLabel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		textLabel = new JLabel();
		add(textLabel, c);
		
		stackTraceArea = new JTextArea();
		stackTraceArea.setEditable(false);
		stackTracePane = new JScrollPane(stackTraceArea);
		stackTracePane.setPreferredSize(new Dimension(250, 250));
		
	}
	
	/* (non-Javadoc)
	 * @see org.microemu.app.ui.MessageListener#showMessage(int, java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	public void showMessage(int level, String title, String text, Throwable throwable) {
		switch (level) {
		case Message.ERROR:
			iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
			break;
		case Message.WARN:
			iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
			break;
		default:
			iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
		}

		textLabel.setText(text);
		
		if (throwable != null) {
			StringWriter writer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(writer));
			stackTraceArea.setText(writer.toString());
			stackTraceArea.setCaretPosition(0);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			c.weightx = 1;
			c.weighty = 1;
			add(stackTracePane, c);
		}
		
		SwingDialogWindow.show(parent, title, this, false);
		
		if (throwable != null) {
			remove(stackTracePane);
		}
	}

}
