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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SwtInputDialog extends SwtDialog 
{
	private String title;
	private String message;
	private String value;
	

	public SwtInputDialog(Shell parentShell, String title, String message)
	{
		super(parentShell);
		
		this.title = title;
		this.message = message;
	}


	public String getValue() 
	{
		return value;
	}


	protected void configureShell(Shell shell) 
	{
		super.configureShell(shell);
		
		if (title != null) {
			shell.setText(title);
		}
	}


	protected Control createDialogArea(Composite composite) 
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);

		Label lbMessage = new Label(composite, SWT.NONE);
		lbMessage.setText(message);
		lbMessage.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Text txInput = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gridData = 
				new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = txInput.getLineHeight() * 15;
		txInput.setLayoutData(gridData);
		txInput.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				value = txInput.getText();
			}
		});
		
		return composite;
	}
	
}
