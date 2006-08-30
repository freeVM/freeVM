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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class Tools {
	
	public static void cleanComposite(Composite composite) {

		if(composite == null) return;

		Control[] controls = composite.getChildren();
		for (int i = 0; i < controls.length; i++) {
			controls[i].dispose();
		}
	}
	
	public static void fireErrorDialog(Composite parent, Throwable exception) {
		if(parent != null && !parent.isDisposed()) {
			Shell shell = new Shell(parent.getDisplay());
			new ErrorDialog(shell, exception);
		}
	}

	public static void fireErrorDialog(Shell shell, Throwable exception) {
		if(shell != null && !shell.isDisposed()) {
			new ErrorDialog(shell, exception);
		}
	}
}
