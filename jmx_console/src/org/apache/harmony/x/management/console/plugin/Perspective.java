/* Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public static final String ID = "org.apache.harmony.x.management.console.plugin.perspective";
	
	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();

		layout.setEditorAreaVisible(false);
//		layout.setFixed(true);

		layout.addNewWizardShortcut("org.apache.harmony.x.management.console.plugin.wizard.new");
		
		IFolderLayout treeFolder = layout.createFolder("tree", IPageLayout.LEFT, 0.25f, editorArea);
		treeFolder.addView(MBeanTreeView.ID);
		
		layout.addView(AttributeView.ID, IPageLayout.TOP, 0.5f, editorArea);

		layout.addView(OperationView.ID, IPageLayout.TOP, 0.5f, editorArea);

		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, 0.25f, editorArea);
		outputfolder.addView(NotificationView.ID);
		outputfolder.addView(OutputView.ID);
	}
}
