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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * This view is designed to hold debug information that is printed by the console.
 * 
 * @author Victor A. Martynov
 */
public class OutputView extends ViewPart {

	public static final String ID = "org.apache.harmony.x.management.console.plugin.view.output";
	public static final String MESSAGE_CONSOLE_NAME = "Message Console";
	
	private static Text text = null;
	private static Composite parent = null; 

	private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private static PrintStream ps = new PrintStream(baos);
	private static Vector messages = new Vector();
	

	public OutputView() {
		super();
	}

	public void createPartControl(Composite parent) {
			OutputView.parent = parent;
			parent.setLayout(new FillLayout());
			text = new Text(parent, SWT.MULTI | SWT.WRAP);
			String s = "";

			for(int i = 0; i < messages.size(); i++) {
				s += messages.get(i);
			}
			
			text.setText(s);
	}

	public void setFocus() {
		
	}

	/**
	 * This method stores the given string in an internal storage and
	 * shows it in OutputView.
	 * @param s The string that will be printed in output view.
	 */
	public static void print(String s) {
		
		messages.add(s+"\n");
		
		if(parent != null     && 
		   text != null       && 
		   !text.isDisposed() &&
		   !parent.isDisposed()) {
			
			text.append(s+"\n");
			parent.layout(true);
		}
	}

	/**
	 * This method stores stack trace of the given exception in an internal storage and
	 * shows it in OutputView.
	 * @param t The throwable/exception that will be printed in output view.
	 */
	public static void print(Throwable t) {
		t.printStackTrace(ps);
		String s = baos.toString()+"\n";
		messages.add(s);

		if(text != null && !text.isDisposed()) {
			text.append(s);
		}

		baos.reset();
	}
}
