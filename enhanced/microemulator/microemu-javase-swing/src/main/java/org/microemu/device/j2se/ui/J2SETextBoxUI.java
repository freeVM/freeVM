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

package org.microemu.device.j2se.ui;

import javax.microedition.lcdui.TextBox;

import org.microemu.device.impl.ui.DisplayableImplUI;
import org.microemu.device.ui.TextBoxUI;

public class J2SETextBoxUI extends DisplayableImplUI implements TextBoxUI {

	private String text;

	public J2SETextBoxUI(TextBox textBox) {
	}

	public int getCaretPosition() {
		// TODO not yet used
		return -1;
	}

	public String getString() {
		// TODO not yet used
		return text;
	}

	public void setString(String text) {
		// TODO not yet used
		this.text = text;
	}

}
