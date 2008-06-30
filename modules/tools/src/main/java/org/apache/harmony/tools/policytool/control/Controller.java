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

package org.apache.harmony.tools.policytool.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.harmony.tools.policytool.view.EditorPanel;
import org.apache.harmony.tools.policytool.view.MainFrame.MenuItemEnum;

/**
 * The controller handles the user actions, drives the GUI and
 * connects it to the model.
 */
public class Controller implements ChangeListener, ActionListener{

    /** Reference to the main frame component.    */
    private final Component     mainFrame;
    /** Array of the editor panels.               */
    private final EditorPanel[] editorPanels;
    /** Reference to the active editor panel.     */
    private EditorPanel         activeEditorPanel;

    /** Reference to the keystore edit menu item. */
    private JMenuItem           keystoreEditMenuItem;

    /** The currently edited policy file.         */
    private File editedPolicyFile;

    /**
     * Creates a new Controller.
     * @param mainFrame reference to the main frame component
     * @param editorPanels array of the editor panels
     * @param policyFileName policy file name to be loaded initially
     */
    public Controller( final Component mainFrame, final EditorPanel[] editorPanels, final String policyFileName ) {
        this.mainFrame    = mainFrame;
        this.editorPanels = editorPanels;
        activeEditorPanel = editorPanels[ 0 ];

        PolicyFileHandler.setDialogParentComponent( mainFrame );

        editedPolicyFile = new File( policyFileName );
        activeEditorPanel.loadPolicyText( PolicyFileHandler.loadPoilcyFile( editedPolicyFile ) );
    }

    /**
     * Returns the array of editor panels.
     * @return the array of editor panels
     */
    public EditorPanel[] getEditorPanels() {
        return editorPanels;
    }

    /**
     * Sets the keystore edit menu item.
     * @param keystoreEditMenuItem the keystore edit menu item
     */
    public void setKeystoreEditMenuItem( final JMenuItem keystoreEditMenuItem ) {
        this.keystoreEditMenuItem = keystoreEditMenuItem;
        keystoreEditMenuItem.setEnabled( activeEditorPanel.supportsGraphicalKeystoreEdit() );
    }

    /**
     * Exits from the program.<br>
     * There might be unsaved changes in which case confirmation will be asked.
     */
    public void exit() {
        boolean exitOk = false;

        if ( activeEditorPanel.getHasDirty() ) {

            switch ( JOptionPane.showConfirmDialog( mainFrame, "There are unsaved changes. Save before exit?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE ) ) {

            case JOptionPane.YES_OPTION:
                final JFileChooser fileChooser = new JFileChooser();

                if ( editedPolicyFile == null ) {
                    if ( fileChooser.showSaveDialog( mainFrame ) == JFileChooser.APPROVE_OPTION )
                        editedPolicyFile = fileChooser.getSelectedFile();
                }
                if ( editedPolicyFile != null ) {
                    if ( !PolicyFileHandler.savePolicyFile( editedPolicyFile, activeEditorPanel.getPolicyText() ) ) {
                        switch ( JOptionPane.showConfirmDialog( mainFrame, "Saving failed. Do you still want to exit?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) ) {
                        case JOptionPane.YES_OPTION:
                            exitOk = true;
                            break;

                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            // We chose not to exit. exitOk = false
                            break;
                        }
                    } else {// Changes saved successfully
                        activeEditorPanel.setHasDirty( false );
                        exitOk = true;
                    }
                }
                break;

            case JOptionPane.NO_OPTION:
                exitOk = true;
                break;

            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
                // We chose not to exit. exitOk = false
                break;

            }

        } else
            exitOk = true;

        if ( exitOk )
            System.exit( 0 );
    }

    /**
     * Handles change events of the editors tabbed pane.
     * @param ce details of the change event
     */
    public void stateChanged( final ChangeEvent ce ) {
        final EditorPanel newActiveEditorPanel = (EditorPanel) ( (JTabbedPane) ce.getSource() ).getSelectedComponent();

        newActiveEditorPanel.loadPolicyText( activeEditorPanel.getPolicyText() );
        newActiveEditorPanel.setHasDirty   ( activeEditorPanel.getHasDirty  () );
        activeEditorPanel = newActiveEditorPanel;

        keystoreEditMenuItem.setEnabled( activeEditorPanel.supportsGraphicalKeystoreEdit() );
    }

    /**
     * Handles the action events of the menu items.
     * @param ae details of the action event
     */
    public void actionPerformed( final ActionEvent ae ) {
        // The action command is the ordinal of the menu item enum.
        final MenuItemEnum menuItemEnum = MenuItemEnum.values()[ Integer.parseInt( ae.getActionCommand() ) ];

        final JFileChooser fileChooser = new JFileChooser();
        switch ( menuItemEnum ) {

        case NEW :
            break;

        case OPEN :
            if ( fileChooser.showOpenDialog( mainFrame ) == JFileChooser.APPROVE_OPTION ) {
                editedPolicyFile = fileChooser.getSelectedFile();
                activeEditorPanel.loadPolicyText( PolicyFileHandler.loadPoilcyFile( editedPolicyFile ) );
            }
            break;

        case SAVE :
            if ( editedPolicyFile == null ) {
                if ( fileChooser.showSaveDialog( mainFrame ) == JFileChooser.APPROVE_OPTION )
                    editedPolicyFile = fileChooser.getSelectedFile();
            }
            if ( editedPolicyFile != null )
                if ( PolicyFileHandler.savePolicyFile( editedPolicyFile, activeEditorPanel.getPolicyText() ) )
                    activeEditorPanel.setHasDirty( false );						
            break;

        case SAVE_AS :
            if ( fileChooser.showSaveDialog( mainFrame ) == JFileChooser.APPROVE_OPTION ) {
                editedPolicyFile = fileChooser.getSelectedFile();
                if ( PolicyFileHandler.savePolicyFile( editedPolicyFile, activeEditorPanel.getPolicyText() ) )
                    activeEditorPanel.setHasDirty( false );						
            }
            break;

        case VIEW_WARNING_LOG :
            break;

        case EXIT :
            exit();
            break;

        case EDIT :
            break;

        }

    }

}
