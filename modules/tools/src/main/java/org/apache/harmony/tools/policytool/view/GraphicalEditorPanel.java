/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tools.policytool.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.tools.policytool.model.GrantEntry;
import org.apache.harmony.tools.policytool.model.KeystoreEntry;
import org.apache.harmony.tools.policytool.model.KeystorePasswordURLEntry;
import org.apache.harmony.tools.policytool.model.PolicyEntry;

/**
 * An editor panel which provides an interface for direct editing the policy text.
 */
public class GraphicalEditorPanel extends EditorPanel {

    /** Holds the invalid policy text or null if the loaded policy text is valid.        */
    private String                          invalidPolicyText;

    /** The list of the policy text's entries or null if invalid policy text was loaded. */
    private List< PolicyEntry >             policyEntryList = new ArrayList< PolicyEntry >();

    /** ListAndEditPanel for handling the grant entries.                                 */
    private ListAndEditPanel< PolicyEntry > grantEntryLAEPanel;
    /**
     * Creates a new GraphicalEditorPanel.<br>
     * Sets a BorderLayout as the layout manager.
     * @param mainFrame reference to the main frame
     */
    public GraphicalEditorPanel( final MainFrame mainFrame ) {
        super( mainFrame, "Graphical editing", new BorderLayout(), true );

        buildGUI();
    }

    /**
     * Builds the graphical user interface.<br>
     * Creates and adds a new LAE panel to this editor panel which will be responsible to handle the grant entries.
     */
    private void buildGUI() {
        if ( grantEntryLAEPanel != null )
            remove( grantEntryLAEPanel );

        grantEntryLAEPanel = new ListAndEditPanel< PolicyEntry >( "Policy entries:", "Policy Entry", policyEntryList,
            new ListAndEditPanel.Filter< PolicyEntry > () {
                public boolean includeEntity( final PolicyEntry entity ) {
                    return entity instanceof GrantEntry;
                }
            },
            new ListAndEditPanel.LAEFormDialogFactory< PolicyEntry > () {
                public LAEFormDialog createFactoryForAddOrEdit( final PolicyEntry selectedEntity ) {
                    return new GrantEntryEditFormDialog( mainFrame, GraphicalEditorPanel.this, (GrantEntry) selectedEntity, policyEntryList );
                }
            }
        );

        add( grantEntryLAEPanel, BorderLayout.CENTER );
    }

    @Override
    public boolean loadPolicyText( final String policyText ) {
        this.invalidPolicyText = policyText;

        policyEntryList = new ArrayList< PolicyEntry >();

        //TODO: uncomment when loadPolicyText() is implemented 
        //buildGUI();

        return true;
    }

    @Override
    public String getPolicyText() {
        return invalidPolicyText;
    }

    /**
     * Shows the keystore entry edit dialog.<br>
     * This dialog handles both the keystore entry and the keystore password URL entries.
     */
    public void showKeystoreEntryEditDialog() {
        KeystoreEntry            keystoreEntry            = null;
        KeystorePasswordURLEntry keystorePasswordURLEntry = null;

        for ( final PolicyEntry policyEntry : policyEntryList ) {
            if ( keystoreEntry == null )
                if ( policyEntry instanceof KeystoreEntry )
                    keystoreEntry = (KeystoreEntry) policyEntry;
            if ( keystorePasswordURLEntry == null )
                if ( policyEntry instanceof KeystorePasswordURLEntry )
                    keystorePasswordURLEntry = (KeystorePasswordURLEntry) policyEntry;

            if ( keystoreEntry != null && keystorePasswordURLEntry != null )
                break;
        }

        new KeystoreEntryEditFormDialog( mainFrame, this, keystoreEntry, keystorePasswordURLEntry, policyEntryList ).setVisible( true );
    }

}
