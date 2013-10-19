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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.service.resolver.State;

public class Main {

	public static void main(String[] args) {
		Main manifester = new Main();

		if (args.length == 0) {
			manifester.printUsage();
			System.exit(-1);
		}

		File hdkPath = new File(args[0]);
		if (!hdkPath.isDirectory()) {
			System.err.println("Cannot find HDK rooted at " + hdkPath);
			System.exit(-1);
		}

		System.out.println("Checking bundles in HDK rooted at " + hdkPath);

		/* Collect up the manifests we wish to check */
		ManifestFinder finder = new ManifestFinder();

		// Bundles in the boot dir
		File bootDir = new File(hdkPath, "jdk/jre/lib/boot");
		Set<SimpleManifest> jarBundles = finder.gatherJarManifests(bootDir);
		Set<SimpleManifest> expandedBundles = finder.gatherExpandedManifests(bootDir);

		// Bundles in the test dir
		File testDir = new File(hdkPath, "build/test");
		Set<SimpleManifest> testBundles = finder.gatherJarManifests(testDir);

		List<SimpleManifest> allManifests = new ArrayList<SimpleManifest>();
		allManifests.addAll(jarBundles);
		allManifests.addAll(expandedBundles);
		allManifests.addAll(testBundles);
		
		Resolver resolver = new Resolver();
		State state = resolver.resolve(allManifests);
		resolver.reportResolutionProblems(state);
		
		if (resolver.hasUnresolvedBundles(state)) {
			// Show callers that we were unhappy
			System.exit(-2);
		}
	}

	void printUsage() {
		System.err.println("USAGE: ");
		System.err.println("  java -jar manifestcheck.jar <hdk_dir>");
	}
}
