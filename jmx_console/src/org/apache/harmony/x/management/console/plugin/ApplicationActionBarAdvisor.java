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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction newWindowAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(IWorkbenchWindow window) {
    	exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
        
        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);
    }

    protected void fillMenuBar(IMenuManager menuBar) {
    	MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        menuBar.add(fileMenu);
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);
        
        fileMenu.add(new Separator());
        helpMenu.add(new Separator());
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));   
        //toolbar.add(openViewAction);
        //toolbar.add(messagePopupAction);
    }

}
