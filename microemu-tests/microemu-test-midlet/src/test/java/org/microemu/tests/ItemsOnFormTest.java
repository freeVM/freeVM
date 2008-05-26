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

package org.microemu.tests;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import junit.framework.TestCase;

public class ItemsOnFormTest  extends TestCase {

	public ItemsOnFormTest() {
		
	}
	
	public ItemsOnFormTest(String name) {
		super(name);
	}
	
	public void testAddItems() {
		int step= 0;
		Item[] items= new Item[1];
		items[step]= new StringItem(null, "one");
		
		ItemsOnForm f = new ItemsOnForm(items);
		for (int i = 1 ; i < 15; i++) {
			f.commandAction(ItemsOnForm.addCommand, f);
		}
	}
}
