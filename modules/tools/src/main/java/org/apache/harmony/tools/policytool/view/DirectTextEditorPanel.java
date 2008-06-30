/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tools.policytool.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.harmony.tools.policytool.Consts;

/**
 * An editor panel which provides an interface for direct editing the policy text.
 */
public class DirectTextEditorPanel extends EditorPanel {
	
	/** Text area for direct editing the policy text. */
	private final JTextArea policyTextTextArea = new JTextArea();
	
	/**
	 * Creates a new DirectTextEditorPanel.<br>
	 * Sets a BorderLayout as the layout manager.
	 */
	public DirectTextEditorPanel() {
		super( new BorderLayout(), false );
		
		policyTextTextArea.setFont( new Font( "Courier New", Font.PLAIN, Consts.DIRECT_EDITING_FONT_SIZE ) );
		
		// We want to track changes of the document so we can ask confirmation on exit
		policyTextTextArea.getDocument().addDocumentListener( new DocumentListener() {
			public void changedUpdate( final DocumentEvent de ) {
			}
			public void insertUpdate ( final DocumentEvent de ) {
				setHasDirty( true );
			}
			public void removeUpdate ( final DocumentEvent de ) {
				setHasDirty( true );
			}
		} );
		
		add( new JScrollPane( policyTextTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ), BorderLayout.CENTER );
	}
	
	@Override
	public String getPanelTitle() {
		return "Direct editing";
	}
	
	@Override
	public void loadPolicyText( final String policyText ) {
		policyTextTextArea.setText( policyText );
	}
	
	@Override
	public String getPolicyText() {
		return policyTextTextArea.getText();
	}
	
}
