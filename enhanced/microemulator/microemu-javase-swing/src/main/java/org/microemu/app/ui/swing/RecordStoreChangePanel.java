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

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.microemu.app.Common;
import org.microemu.app.util.FileRecordStoreManager;

public class RecordStoreChangePanel extends SwingDialogPanel {

	private static final long serialVersionUID = 1L;

	private Common common;

	private JComboBox selectStoreCombo = new JComboBox(new String[] { "File record store", "Memory record store" });

	public RecordStoreChangePanel(Common common) {
		this.common = common;

		add(new JLabel("Record store type:"));
		add(selectStoreCombo);
	}

	protected void showNotify() {
		if (common.getRecordStoreManager() instanceof FileRecordStoreManager) {
			selectStoreCombo.setSelectedIndex(0);
		} else {
			selectStoreCombo.setSelectedIndex(1);
		}
	}

	public String getSelectedRecordStoreName() {
		return (String) selectStoreCombo.getSelectedItem();
	}

}
