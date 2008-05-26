/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.ui.swing.logconsole;

/**
 * @author Michael Lifshits
 *
 */
import java.awt.Rectangle;

import javax.swing.JTextArea;
import javax.swing.JViewport;

public class LogTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;

	//private int maxLines = 20;

	private LogTextCaret caret;

	public LogTextArea(int rows, int columns, int maxLines) {
		super(rows, columns);
		caret = new LogTextCaret();
		setCaret(caret);
		//this.maxLines = maxLines;
		setEditable(false);
	}

	public void setText(String t) {
		super.setText(t);
		caret.setVisibilityAdjustment(true);
	}

	public void append(String str) {

		super.append(str);
		
		JViewport viewport = (JViewport) getParent();
		boolean scrollToBottom = Math.abs(viewport.getViewPosition().getY() - (getHeight() - viewport.getHeight())) < 100;

		caret.setVisibilityAdjustment(scrollToBottom);
		
		if (scrollToBottom) {
			setCaretPosition(getText().length());
		}

		//		if (getLineCount() > maxLines) {
		//			Document doc = getDocument();
		//			if (doc != null) {
		//				try {
		//					doc.remove(0, getLineStartOffset(getLineCount() - maxLines - 1));
		//				} catch (BadLocationException e) {
		//				}
		//			}
		//			if (!scrollToBottom) {
		//				Rectangle nloc = new Rectangle(0,30,10,10);
		//		        if (SwingUtilities.isEventDispatchThread()) {
		//		        	scrollRectToVisible(nloc);
		//		        } else {
		//		            SwingUtilities.invokeLater(new SafeScroller(nloc));
		//		        }
		//			}
		//		}
	}

	class SafeScroller implements Runnable {

		Rectangle r;

		SafeScroller(Rectangle r) {
			this.r = r;
		}

		public void run() {
			LogTextArea.this.scrollRectToVisible(r);
		}
	}

}