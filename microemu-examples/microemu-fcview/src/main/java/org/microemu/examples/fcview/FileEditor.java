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

package org.microemu.examples.fcview;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * @author vlads
 *
 */
public class FileEditor extends TextBox  implements CommandListener {

	static final Command saveCommand = new Command("Save", Command.OK, 1);
	
	static final Command backCommand = new Command("Back", Command.BACK, 5);
	
	private Displayable back;
	
	FileConnection file;
	
	public FileEditor(FileConnection fc, Displayable back) {
		super("Edit " + fc.getName(), null, 128, TextField.ANY);
		this.back = back;
		addCommand(saveCommand);
		addCommand(backCommand);
		setCommandListener(this);
		
		file = fc;
		load();
	}

	private void load() {
		DataInputStream is = null; 
		try {
			is = file.openDataInputStream();
			this.setString(is.readUTF());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					((InputStream)is).close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	private void save() {
		DataOutputStream os = null; 
		try {
			os = file.openDataOutputStream();
			os.writeUTF(this.getString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					((OutputStream)os).close();
				}
			} catch (IOException ignore) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == backCommand) {
			try {
				file.close();
			} catch (IOException ignore) {
			}
			FCViewMIDlet.setCurrentDisplayable(back);
		} else if (c == saveCommand) {
			save();
		}
		
	}
}
