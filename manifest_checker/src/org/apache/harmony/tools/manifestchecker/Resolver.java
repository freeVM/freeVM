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

import java.util.Dictionary;
import java.util.List;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.osgi.framework.BundleException;

public class Resolver {

	StateObjectFactory factory;

	Resolver() {
		super();
		factory = StateObjectFactory.defaultFactory;
	}

	State resolve(List<SimpleManifest> manifests) {

		State state = factory.createState(true);

		// Create bundle descriptions from our manifests
		BundleDescription[] descriptions = new BundleDescription[manifests
				.size()];
		for (int i = 0; i < descriptions.length; i++) {
			Dictionary<String, String> dict = manifests.get(i).toDictionary();
			try {
				descriptions[i] = factory.createBundleDescription(state, dict,
						Integer.toString(i), i);
			} catch (BundleException e) {
				handleBundleException(e, manifests.get(i));
				continue;
			}
			state.addBundle(descriptions[i]);
		}

		// This does the work...
		state.resolve();

		return state;
	}

	void reportResolutionProblems(State state) {
		boolean resolutionErrors = hasUnresolvedBundles(state);

		BundleDescription[] descriptions = state.getBundles();
		VersionConstraint[] rootcauses = state.getStateHelper()
				.getUnsatisfiedLeaves(descriptions);

		// There may be warnings of unresolved constraints, show errors and
		// warnings
		if (resolutionErrors || rootcauses.length != 0) {
			if (resolutionErrors) {
				System.err.println("ERROR: There were resolution errors");
			} else {
				System.err.println("WARNING: There were resolution warnings");
			}
			System.err.println();

			// Just being cute
			System.err.print("The following ");
			if (rootcauses.length == 1) {
				System.err.println("root cause was identified");
			} else {
				System.err.println(rootcauses.length
						+ " root causes were identified");
			}

			for (VersionConstraint constraint : rootcauses) {
				System.err.print("Could not resolve following constraint in ");
				System.err.println(constraint.getBundle().getName());
				System.err.println("\t" + constraint);
			}
		} else {
			System.out.println("There were no resolution problems.");
		}
	}

	boolean hasUnresolvedBundles(State state) {
		BundleDescription[] descriptions = state.getBundles();
		for (int i = 0; i < descriptions.length; i++) {
			if (!descriptions[i].isResolved()) {
				return true;
			}
		}
		return false;
	}

	private void handleBundleException(BundleException e,
			SimpleManifest manifest) {
		System.err.println("Problem found creating bundle description from "
				+ manifest);
		System.err.println("Error message was: " + e.getLocalizedMessage());
	}
}
