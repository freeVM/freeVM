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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The abstraction of a panel which can list entities and provide GUI components to offer and handle certain actions on the entities.<br>
 * The entities are listed in a listbox, 
 * @param <EntityType> type of the entities listed on and edited by this panel
 */
public class ListAndEditPanel< EntityType > extends JPanel implements ActionListener {

    /** The component to list he entities. */
    private final JList   entityListComponent = new JList();

    /** Add new entity button.             */
    private final JButton addButton           = new JButton();
    /** Edit selected entity button.       */
    private final JButton editButton          = new JButton();
    /** Remove selected entity button.     */
    private final JButton removeButton        = new JButton();

    /** Reference to the list whose elements are to be listed and edited, and where to put new entities. */
    private final List< ? > entityList;

    /**
     * Can be used to filter the input entity list, hide elements from displaying. 
     * @param <EntityType> type of the entities filtered by this filter
     */
    public interface Filter< EntityType > {
        /**
         * Tells whether to include an entity in the list-and-edit process
         * @param entity entity to be tested
         * @return true if the entity should be listed and edited; false if it should be and excluded and hid
         */
        boolean includeEntity( final EntityType entity );
    }

    /**
     * Creates a new ListAndEditPanel.<br>
     * Sets a BorderLayout for ourselves.
     * @param entityName name of the listed and edited entity (this will be displayed on the buttons)
     * @param entityList reference to the list whose elements are to be listed and edited, and where to put new entities
     */
    public ListAndEditPanel( final String entityName, final List< EntityType > entityList ) {
        this( entityName, entityList, null );
    }

    /**
     * Creates a new ListAndEditPanel.<br>
     * Sets a BorderLayout for ourselves.
     * @param entityName name of the listed and edited entity (this will be displayed on the buttons)
     * @param entityList reference to the list whose elements are to be listed and edited, and where to put new entities
     * @param entityFilter filter to be used when listing the entities
     */
    public ListAndEditPanel( final String entityName, final List< EntityType > entityList, final Filter< EntityType > entityFilter ) {
        super( new BorderLayout() );

        this.entityList = entityList;

        final DefaultListModel listModel = new DefaultListModel();
        for ( final EntityType entity : entityList )
            if ( entityFilter == null || entityFilter.includeEntity( entity ) )
                listModel.addElement( entity );

        entityListComponent.setModel( listModel );

        buildGUI( entityName );
    }

    /**
     * Builds the graphical user interface of the panel.
     * @param entityName name of the listed and edited entity (this will be displayed on the buttons)
     */
    private void buildGUI( final String entityName ) {
        final JPanel buttonsPanel = new JPanel();

        addButton   .setText          ( "Add " + entityName                );
        addButton   .setMnemonic      ( addButton   .getText().charAt( 0 ) );
        addButton   .addActionListener( this                               );
        buttonsPanel.add( addButton );

        editButton  .setText          ( "Edit " + entityName               );
        editButton  .setMnemonic      ( editButton  .getText().charAt( 0 ) );
        editButton  .addActionListener( this                               );
        buttonsPanel.add( editButton );

        removeButton.setText          ( "Remove " + entityName             );
        removeButton.setMnemonic      ( removeButton.getText().charAt( 0 ) );
        removeButton.addActionListener( this                               );
        buttonsPanel.add( removeButton );

        add( buttonsPanel, BorderLayout.NORTH );

        add( new JScrollPane( entityListComponent ), BorderLayout.CENTER );
    }

    /**
     * Handles the action events of the buttons for adding new, editing and removing entities.
     * @param ae details of the action event
     */
    public void actionPerformed( final ActionEvent ae ) {
    }

}
