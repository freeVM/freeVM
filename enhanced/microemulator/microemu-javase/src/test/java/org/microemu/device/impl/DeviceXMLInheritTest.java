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

package org.microemu.device.impl;

import org.microemu.device.impl.DeviceImpl;

import nanoxml.XMLElement;

import junit.framework.TestCase;

/**
 * @author vlads
 *
 */
public class DeviceXMLInheritTest extends TestCase {

	public void verifyXML(String parentXML, String childXML, String expectedXML) {
		XMLElement child = new XMLElement();
		child.parseString(childXML);
		XMLElement parent = new XMLElement();
		parent.parseString(parentXML);
		XMLElement expected = new XMLElement();
		expected.parseString(expectedXML);
		
		XMLElement result = DeviceImpl.inheritXML(parent, child, "/");
		
		//System.out.println("expected:" + expected.toString());
		//System.out.println(" result:" + result.toString());
		
		assertEquals(result.toString(), expected.toString());
	}
	
	public void testValueOverride() {
		verifyXML("<device><display><numcolors>2</numcolors><vp>vpv</vp></display></device>", 
				  "<device><display><numcolors>65536</numcolors><v>v1</v></display></device>", 
				  "<device><display><numcolors>65536</numcolors><vp>vpv</vp><v>v1</v></display></device>");
	}

	public void testValueOverrideByName() {
		verifyXML("<device><v>v1</v><v name=\"2\">v2</v><v name=\"3\">v3</v></device>", 
				  "<device><v>v1m</v><v name=\"3\">v3m</v></device>", 
				  "<device><v>v1m</v><v name=\"2\">v2</v><v name=\"3\">v3m</v></device>");

		verifyXML("<device><v>v1</v><v name=\"2\">v2</v><v name=\"3\">v3</v></device>", 
				  "<device><v name=\"3\">v3m</v><v>v1m</v></device>", 
				  "<device><v>v1m</v><v name=\"2\">v2</v><v name=\"3\">v3m</v></device>");
		
		verifyXML("<device><v>v1</v><v name=\"2\">v2</v><v name=\"3\">v3</v></device>", 
				  "<device><v>v1m</v><v name=\"3\" attrm=\"am\">v3m</v></device>", 
				  "<device><v>v1m</v><v name=\"2\">v2</v><v name=\"3\" attrm=\"am\">v3m</v></device>");
		
		verifyXML("<device><v attrm=\"am\">v1</v><v name=\"2\">v2</v><v name=\"3\">v3</v></device>", 
				  "<device><v>v1m</v><v name=\"3\">v3m</v></device>", 
				  "<device><v attrm=\"am\">v1m</v><v name=\"2\">v2</v><v name=\"3\">v3m</v></device>");
	}
	
	public void testValueOverrideByNameWithCase() {
		verifyXML("<device><v>v1</v><v name=\"a\">v2</v><v name=\"b\">v3</v></device>", 
				  "<device><v>v1m</v><v name=\"B\">v3m</v></device>", 
				  "<device><v>v1m</v><v name=\"a\">v2</v><v name=\"b\">v3</v><v name=\"B\">v3m</v></device>");

	}

	public void testValueOverrideNoName() {
		verifyXML("<device><v name=\"A\">v1</v></device>", 
				  "<device><v name=\"B\">v2</v></device>", 
				  "<device><v name=\"A\">v1</v><v name=\"B\">v2</v></device>");

	}
	
	public void testValueOverrideFonts() {
		verifyXML("<device><fonts>" +
				   "<font face=\"system\" style=\"plain\" size=\"small\">F1</font>" +
				   "<font face=\"system\" style=\"plain\" size=\"medium\">F2</font>" +
				   "</fonts></device>",
				   
				   "<device><fonts>" +
				   "<font face=\"system\" style=\"plain\" size=\"medium\">F2m</font>" +
				   "</fonts></device>",
				   
				   "<device><fonts>" +
				   "<font face=\"system\" style=\"plain\" size=\"small\">F1</font>" +
				   "<font face=\"system\" style=\"plain\" size=\"medium\">F2m</font>" +
				   "</fonts></device>");
	}
	
}
