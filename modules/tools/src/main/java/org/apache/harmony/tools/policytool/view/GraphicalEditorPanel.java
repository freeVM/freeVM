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
import java.util.List;

import org.apache.harmony.tools.policytool.model.PolicyEntry;

/**
 * An editor panel which provides an interface for direct editing the policy text.
 */
public class GraphicalEditorPanel extends EditorPanel {
	
	/** Holds the invalid policy text or null if the loaded policy text is valid.        */
	private String invalidPolicyText;
	
	/** The list of the policy text's entries or null if invalid policy text was loaded. */
	private List< PolicyEntry > policyEntryList;
	
	/**
	 * Creates a new GraphicalEditorPanel.<br>
	 * Sets a BorderLayout as the layout manager.
	 */
	public GraphicalEditorPanel() {
		super( new BorderLayout(), true );
	}
	
	@Override
	public String getPanelTitle() {
		return "Graphical editing";
	}
	
	@Override
	public void loadPolicyText( final String policyText ) {
		this.invalidPolicyText = policyText;
	}
	
	@Override
	public String getPolicyText() {
		return invalidPolicyText;
	}
	
}
