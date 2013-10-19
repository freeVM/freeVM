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

package org.microemu.android.device.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.android.lcdui.Command;
import javax.microedition.android.lcdui.CommandListener;

import org.microemu.device.ui.DisplayableUI;

public abstract class AndroidDisplayableUI implements DisplayableUI {
	
	private static Comparator<Command> commandsPriorityComparator = new Comparator<Command>() {

		public int compare(Command first, Command second) {
			if (first.getPriority() == second.getPriority()) {
				return 0;
			} else if (first.getPriority() < second.getPriority()) {
				return -1;
			} else {
				return 1;
			}
		}
		
	};
	
	private List<Command> commands = new ArrayList<Command>();
	
	private CommandListener commandListener = null;
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public CommandListener getCommandListener() {
		return commandListener;
	}
	
	//
	// DisplayableUI
	//

	public void addCommand(Command cmd) {
		commands.add(cmd);
		// TODO decide whether this is the best way for keeping sorted commands
		Collections.sort(commands, commandsPriorityComparator);
	}

	public void removeCommand(Command cmd) {
		commands.remove(cmd);
	}

	public void setCommandListener(CommandListener l) {
		this.commandListener = l;
	}

}
