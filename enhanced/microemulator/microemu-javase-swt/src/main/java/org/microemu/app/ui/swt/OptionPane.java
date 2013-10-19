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

package org.microemu.app.ui.swt;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class OptionPane 
{
	
	public static final int ERROR_MESSAGE = 1;
	public static final int INFORMATION_MESSAGE = 2;
	
	
	private static WindowAdapter windowListener = new WindowAdapter()
	{
		public void windowClosing(WindowEvent ev) 
		{
			((Dialog) ev.getSource()).hide();
		}
	};
	

	public static String showInputDialog(Component parentComponent, String message)
	{
		final StringBuffer result = new StringBuffer();
		final Dialog dialog = new Dialog(getFrame(parentComponent), "Input");
		final TextField tfInput = new TextField(40);

		dialog.setModal(true);
		dialog.setLayout(new BorderLayout());
		dialog.add(new Label(message), BorderLayout.WEST);
		dialog.add(tfInput, BorderLayout.CENTER);
		Panel panel = new Panel();
		Button btOk = new Button("OK");
		panel.add(btOk);
		Button btCancel = new Button("Cancel");
		panel.add(btCancel);
		dialog.add(panel, BorderLayout.SOUTH); 
		dialog.pack();
		dialog.addWindowListener(windowListener);
		
		btOk.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent ev) 
			{
				result.append(tfInput.getText());
				dialog.hide();
			}
		});
		
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) 
			{
				dialog.hide();
			}
		}); 

		dialog.show();
				
		if (result.length() > 0) {		
			return result.toString();
		}
		
		return null;
	}
	
	
	public static void showMessageDialog(Component parentComponent, String message, String title, int messageType)
	{	
		final Dialog dialog = new Dialog(getFrame(parentComponent), title);

		dialog.setModal(true);
		dialog.setLayout(new BorderLayout());
		dialog.add(new Label(message), BorderLayout.CENTER);
		Panel panel = new Panel();
		Button btOk = new Button("OK");
		panel.add(btOk);
		dialog.add(panel, BorderLayout.SOUTH); 
		dialog.pack();
		dialog.addWindowListener(windowListener);
		
		btOk.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent ev) 
			{
				dialog.hide();
			}
		});		

		dialog.show();
	}
	
	
	private static Frame getFrame(Component parentComponent) 
	{
		Component tmp = parentComponent;
		while (true) {
			if (tmp instanceof Frame) {
				return (Frame) tmp;
			}
			tmp = tmp.getParent();
		}
	}
	
	
}
