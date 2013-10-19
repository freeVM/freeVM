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

package org.microemu.android.device.ui;

import javax.microedition.android.lcdui.TextBox;

import org.microemu.android.MicroEmulatorActivity;
import org.microemu.device.ui.TextBoxUI;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AndroidTextBoxUI extends AndroidDisplayableUI implements TextBoxUI {
	
	private MicroEmulatorActivity activity;
	
	private TextBox textBox;
	
	private LinearLayout view;
	
	private TextView titleView;
	
	private EditText editView;
	
	public AndroidTextBoxUI(final MicroEmulatorActivity activity, TextBox textBox) {
		this.activity = activity;		
		this.textBox = textBox;
		
		activity.post(new Runnable() {
			public void run() {
				AndroidTextBoxUI.this.view = new LinearLayout(activity);
				AndroidTextBoxUI.this.view.setOrientation(LinearLayout.VERTICAL);
				AndroidTextBoxUI.this.view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				
				titleView = new TextView(activity);
				titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				AndroidTextBoxUI.this.view.addView(titleView);
				
				editView = new EditText(activity);
				editView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				AndroidTextBoxUI.this.view.addView(editView);
				
				AndroidTextBoxUI.this.invalidate();
			}
		});		
	}

	//
	// DisplayableUI
	//
	
	public void hideNotify() {
	}

	public void showNotify() {
		activity.post(new Runnable() {
			public void run() {
				activity.setContentView(view);
				editView.requestFocus();
			}
		});
	}
	
	public void invalidate() {
		titleView.setText(textBox.getTitle());		
	}	

	//
	// TextBoxUI
	//
	
	public int getCaretPosition() {
		return editView.getSelectionStart();
	}
	
	public String getString() {
		return editView.getText().toString();
	}

	public void setString(String text) {
		editView.setText(text);
		editView.setSelection(text.length());
	}

}
