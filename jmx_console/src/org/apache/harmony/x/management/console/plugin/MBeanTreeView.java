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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class MBeanTreeView extends ViewPart {

	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.mbeantree";
	
	private Tree mbean_TREE = null;

	private Composite parent = null;

	/*
	 * Icons and images. 
	 */
	static Image mainIcon = null;

	static Image errorIcon = null;

	static Image resultIcon = null;

	static Image connectIcon = null;
	
	static MBeanTreeView instance; 
	private TreeItem root;
	
	public MBeanTreeView() {
		super();
		instance = this;
		OutputView.print("MBeanTreeView.<init>");
	}
	
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		this.parent=parent;
		
		OutputView.print("MBeanTreeView.createPartControl: started");
		Conn.setMBeanTreeView(this);
		parent.setLayout(new FillLayout());
		
		parent.addDisposeListener( new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				OutputView.print("MBeanTreeView was disposed.");
				Conn.setMBeanTreeView(null);
			}
		});

		mbean_TREE = new Tree(parent, SWT.NONE);
		mbean_TREE.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				
				if(Conn.DEBUG) {
					OutputView.print("Event on MBean Tree: "+event);
				}
				
				TreeItem treeItem = (TreeItem) event.item;
				MBeanTreeNode node = (MBeanTreeNode) treeItem.getData();

				if(Conn.DEBUG) {
					OutputView.print("selected node: "+node);
				}
				
				if (node == null) {
					return;
				}
				
				Conn.setActiveNode(node);

				updateDependViewList();
			}
		});
		
		root = new TreeItem(mbean_TREE, SWT.NONE);
		
		fill();
	}

	void fill() {

		//Tools.cleanComposite(parent);

		root.removeAll();
		
		mbean_TREE.setVisible(true);
		
		root.setText(Conn.getStatus());

		if(Conn.getMode() == Conn.NOT_CONNECTED) {
			return;
		}

		/*
		 * Retrieving the domains.
		 */
		String[] domains = null;

		try {
			domains = Conn.getMBeanOperations().getAllDomains();
		} catch (Exception e) {
			Tools.fireErrorDialog(parent, e);
			e.printStackTrace();
			return;
		}

		
		for (int i = 0; i < domains.length; i++) {

			String[] mbeans = null;

			try {
				mbeans = Conn.getMBeanOperations().getMBeansOfDomain(domains[i]);
			} catch (Exception e) {
				Tools.fireErrorDialog(parent, e);
				e.printStackTrace();
				return;
			}

			TreeItem domain_TRI = new TreeItem(root, SWT.NULL);

			domain_TRI.setText(domains[i]);

			for (int j = 0; j < mbeans.length; j++) {
				TreeItem mbean_TRI = new TreeItem(domain_TRI, SWT.NULL);
				mbean_TRI.setText(mbeans[j]);
				MBeanTreeNode node = new MBeanTreeNode(mbeans[j]);
				mbean_TRI.setData(node);
			}
		}

		/* Refresh all components */
		parent.layout();
	}
	/*
	 */
	public void setFocus() {
	//	treeViewer.getControl().setFocus();
	}

	private void updateDependViewList() {
		IMBeanTreeDependant views[] = Conn.getViews();

		if(Conn.DEBUG) {
			OutputView.print("UpdateDependViewList: views length="+views.length);
		}

		for(int i=0; i < views.length; i++) {
			
			Composite parent = views[i].getParent();
			if(parent.isDisposed()) {
				mbean_TREE.removeSelectionListener(views[i].getMBeanTreeListener());
				Conn.removeView(views[i]);
			} else if(!views[i].getSubscribed()){
				mbean_TREE.addSelectionListener(views[i].getMBeanTreeListener());
				views[i].setSubscribed(true);
			}
		}
	}
}