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

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Label;

public class VMView extends ViewPart {

	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.vm";
	
	private Composite VMView_COMPOSITE = null;

	private Composite classLoadingMXBean_COMPOSITE = null;

	private Composite compilationMXBean_COMPOSITE = null;

	private Composite garbageCollectorMXBean_COMPOSITE = null;

	private Composite memoryManagerMXBean_COMPOSITE = null;

	private Composite memoryMXBean_COMPOSITE = null;

	private Composite memoryPoolMXBean_COMPOSITE = null;

	private Composite operatingSystemMXBean_COMPOSITE = null;

	private Composite runtimeMXBean_COMPOSITE = null;

	private Composite threadMXBean_COMPOSITE = null;
	
	public void createPartControl(Composite parent) {
		try {
			VMView_COMPOSITE = new Composite(parent, SWT.NONE);
			VMView_COMPOSITE.setLayout(new FillLayout());

			TabFolder VMView_TABFOLDER = new TabFolder(VMView_COMPOSITE,
					SWT.NONE);

			TabItem classLoadingMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem compilationMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem garbageCollectorMXBean_TABITEM = new TabItem(
					VMView_TABFOLDER, SWT.NONE);
			TabItem memoryManagerMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem memoryMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem memoryPoolMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem operatingSystemMXBean_TABITEM = new TabItem(
					VMView_TABFOLDER, SWT.NONE);
			TabItem runtimeMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);
			TabItem threadMXBean_TABITEM = new TabItem(VMView_TABFOLDER,
					SWT.NONE);

			classLoadingMXBean_TABITEM.setText("Class Loading");
			compilationMXBean_TABITEM.setText("Compilation");
			garbageCollectorMXBean_TABITEM.setText("Garbage Collector");
			memoryManagerMXBean_TABITEM.setText("Memory Manager");
			memoryMXBean_TABITEM.setText("Memory");
			memoryPoolMXBean_TABITEM.setText("Memory Pool");
			operatingSystemMXBean_TABITEM.setText("Operating System");
			runtimeMXBean_TABITEM.setText("Runtime");
			threadMXBean_TABITEM.setText("Threads");

			classLoadingMXBean_TABITEM.setToolTipText("Class Loading");
			compilationMXBean_TABITEM.setToolTipText("Compilation");
			garbageCollectorMXBean_TABITEM.setToolTipText("Garbage Collector");
			memoryManagerMXBean_TABITEM.setToolTipText("Memory Manager");
			memoryMXBean_TABITEM.setToolTipText("Memory");
			memoryPoolMXBean_TABITEM.setToolTipText("Memory Pool");
			operatingSystemMXBean_TABITEM.setToolTipText("Operating System");
			runtimeMXBean_TABITEM.setToolTipText("Runtime");
			threadMXBean_TABITEM.setToolTipText("Threads");

			classLoadingMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			compilationMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			garbageCollectorMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			memoryManagerMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			memoryMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);
			memoryPoolMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			operatingSystemMXBean_COMPOSITE = new Composite(VMView_TABFOLDER,
					SWT.NONE);
			runtimeMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);
			threadMXBean_COMPOSITE = new Composite(VMView_TABFOLDER, SWT.NONE);

			classLoadingMXBean_TABITEM.setControl(classLoadingMXBean_COMPOSITE);
			compilationMXBean_TABITEM.setControl(compilationMXBean_COMPOSITE);
			garbageCollectorMXBean_TABITEM
					.setControl(garbageCollectorMXBean_COMPOSITE);
			memoryManagerMXBean_TABITEM
					.setControl(memoryManagerMXBean_COMPOSITE);
			memoryMXBean_TABITEM.setControl(memoryMXBean_COMPOSITE);
			memoryPoolMXBean_TABITEM.setControl(memoryPoolMXBean_COMPOSITE);
			operatingSystemMXBean_TABITEM
					.setControl(operatingSystemMXBean_COMPOSITE);
			runtimeMXBean_TABITEM.setControl(runtimeMXBean_COMPOSITE);
			threadMXBean_TABITEM.setControl(threadMXBean_COMPOSITE);

			createVMView();

			//timer.schedule(new VMViewRefresher(), 0L, REFRESH_RATE);	TODO	
		} catch (Exception exc) {
			exc.printStackTrace();
			parent.setLayout(new FillLayout());
			new Label(parent, SWT.NONE).setText("No VM view available.");
		}		

	}

	public void setFocus() {

	}
	
	/* ************************************************************************
	 * Private methods.
	 *************************************************************************/
	private void createVMView() {

		operatingSystemMXBean_COMPOSITE.setLayout(new GridLayout(1, false));
		classLoadingMXBean_COMPOSITE.setLayout(new GridLayout(1, false));

		Hashtable hashtable = null;
		Enumeration keys = null;
		/* Filling Operating System Info composite */

		try {
			hashtable = Conn.getVMMonitor().getOSInfo();
		} catch (Exception e) {
//			fireErrorDialog(main_SHELL, e); TODO
			e.printStackTrace();
			return;
		}

		keys = hashtable.keys();

		while (keys.hasMoreElements()) {
			Object o = keys.nextElement();
			Label label = new Label(operatingSystemMXBean_COMPOSITE, SWT.NONE);
			label.setText(o + ": " + hashtable.get(o));
		}

		/* Filling Class Loading composite */

		try {
			hashtable = Conn.getVMMonitor().getClassLoadingInfo();
		} catch (Exception e) {
//			fireErrorDialog(main_SHELL, e); TODO
			e.printStackTrace();
			return;
		}

		keys = hashtable.keys();

		while (keys.hasMoreElements()) {
			Object o = keys.nextElement();
			Label label = new Label(classLoadingMXBean_COMPOSITE, SWT.NONE);
			label.setText(o + ": " + hashtable.get(o));
		}
	}


}
