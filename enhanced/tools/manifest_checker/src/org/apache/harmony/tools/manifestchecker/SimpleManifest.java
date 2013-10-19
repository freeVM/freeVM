/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tools.manifestchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

class SimpleManifest {

	static SimpleManifest fromFile(File manifestFile) throws IOException {
		FileInputStream fis = new FileInputStream(manifestFile);
		Manifest manifest = new Manifest(fis);
		return fromManifest(manifest);
	}

	static SimpleManifest fromManifest(Manifest manifest) {
		return new SimpleManifest(manifest.getMainAttributes());
	}

	Attributes attributes;

	SimpleManifest(Attributes attributes) {
		super();
		this.attributes = attributes;
	}

	String getBSN() {
		String bsnValue = attributes.getValue("Bundle-SymbolicName");
		return trimAttributes(bsnValue);
	}

	String[] getExportedPackages() {
		String exportValue = attributes.getValue("Export-Package");
		if (exportValue == null) {
			return null;
		}
		String[] exportStrings = exportValue.split(",");
		return trimAllAttributes(exportStrings);
	}

	String[] getImportedPackages() {
		String importValue = attributes.getValue("Import-Package");
		if (importValue == null) {
			return null;
		}
		String[] importStrings = importValue.split(",");
		return trimAllAttributes(importStrings);
	}

	public Dictionary<String, String> toDictionary() {
		Dictionary<String, String> result = new Hashtable<String, String>(attributes.size());
		Iterator<Entry<Object, Object>> entryItr = attributes.entrySet().iterator();
		while (entryItr.hasNext()) {
			Entry<Object, Object> entry = (Entry<Object, Object>) entryItr.next();
			result.put((String) entry.getKey().toString(), (String) entry.getValue());
		}
		return result;
	}

	public String toString() {
		return "Manifest for " + getBSN();
	}

	private String[] trimAllAttributes(String[] values) {
		String[] trimmedImports = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			trimmedImports[i] = trimAttributes(values[i]);
		}
		return trimmedImports;
	}

	private String trimAttributes(String value) {
		if (value == null) {
			return null;
		}
		int semi = value.indexOf(';');
		if (semi == -1) {
			return value;
		}
		return value.substring(0, semi);
	}
}
