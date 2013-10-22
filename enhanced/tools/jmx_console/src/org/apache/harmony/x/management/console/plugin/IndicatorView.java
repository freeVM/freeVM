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
 * @version $Revision: 1.3 $
 */

package org.apache.harmony.x.management.console.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class IndicatorView extends ViewPart {

	public IndicatorView() {
		super();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		Button disconnectButton = new Button(parent, SWT.NONE);
		disconnectButton.setText("Disconnect");
		disconnectButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				
			}
		});
		
		Label statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText("Status: "+Conn.getStatus());
	}

	public void setFocus() {
	}

}
