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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestFinder {

	public ManifestFinder() {
		super();
	}

	Set<SimpleManifest> gatherJarManifests(File bundleDir) {
		File[] jarFiles = bundleDir.listFiles(new FilenameFilter() {
			public boolean accept(File dirName, String fileName) {
				return fileName.endsWith(".jar");
			}
		});

		Set<SimpleManifest> manifests = new HashSet<SimpleManifest>(
				jarFiles.length);
		for (File file : jarFiles) {
			try {
				JarFile jar = new JarFile(file);
				Manifest manifest = jar.getManifest();
				if (manifest == null) {
					System.out.println("WARNING: No manifest found in " + file);
				} else {
					manifests.add(SimpleManifest.fromManifest(manifest));
				}
			} catch (IOException e) {
				System.err.println("ERROR: Problem getting manifest from "
						+ file + " : skipping...");
			}
		}
		return manifests;
	}

	Set<SimpleManifest> gatherExpandedManifests(File bundleDir) {
		File[] subdirs = bundleDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		Set<SimpleManifest> result = new HashSet<SimpleManifest>(subdirs.length);
		for (int i = 0; i < subdirs.length; i++) {
			File subdir = subdirs[i];
			File manifest = new File(subdir, "META-INF" + File.separator
					+ "MANIFEST.MF");
			if (manifest.exists()) {
				try {
					result.add(SimpleManifest.fromFile(manifest));
				} catch (IOException e) {
					System.err.println("Problem getting manifest from "
							+ manifest + " : skipping...");
				}
			} else {
				System.out
						.println("WARNING: No manifest found in expanded bundle "
								+ subdir);
			}
		}
		return result;
	}
}
