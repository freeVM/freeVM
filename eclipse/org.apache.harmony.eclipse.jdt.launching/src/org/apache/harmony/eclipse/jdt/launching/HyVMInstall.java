/* Copyright 2005, 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.eclipse.jdt.launching;

import java.util.List;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.LibraryLocation;

public class HyVMInstall extends AbstractVMInstall {

	/*
	 * Constructor for a VM install.
	 */
	HyVMInstall(IVMInstallType type, String id) {
		super(type, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.launching.IVMInstall#getVMRunner(java.lang.String)
	 */
	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			return new HyVMRunner(this);
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			return new HyDebugVMRunner(this);
		}
		return null;
	}

	public void setLibraryLocations(LibraryLocation[] locations) {
		// If the libs are being explicitly set, then don't monkey with them
		if (locations != null) {
			super.setLibraryLocations(locations);
			return;
		}

		if (locations == null) {
			super.setLibraryLocations(locations);
			return;
		}

		// 'null' means use the default locations, which for us depends
		// upon the argument list.

		// find the vm subdir and VMI name
		String subdir = "default"; //$NON-NLS-1$
		String vminame = "harmonyvm"; //$NON-NLS-1$
		String[] args = getVMArguments();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("-vmdir:")) { //$NON-NLS-1$
					subdir = args[i].substring("-vmdir:".length()); //$NON-NLS-1$
				}
				if (args[i].startsWith("-vm:")) { //$NON-NLS-1$
					vminame = args[i].substring("-vm:".length()); //$NON-NLS-1$
				}
			}
		}

		// Build a library location for the kernel classes
		LibraryLocation[] kernel = kernelLocation(subdir, vminame);
		LibraryLocation[] stdDefaults = getVMInstallType()
				.getDefaultLibraryLocations(getInstallLocation());

		// Ensure we don't duplicate the kernel
		boolean found = false;
		for (int i = 0; i < stdDefaults.length; i++) {
			LibraryLocation location = stdDefaults[i];
			if (location.getSystemLibraryPath().equals(
					kernel[0].getSystemLibraryPath())) {
				found = true;
				break;
			}
		}

		if (found) {
			super.setLibraryLocations(null);
		} else {
			LibraryLocation[] allLibs = new LibraryLocation[kernel.length + stdDefaults.length];
			System.arraycopy(kernel, 0, allLibs, 0, kernel.length);
			System.arraycopy(stdDefaults, 0, allLibs, kernel.length, stdDefaults.length);
			super.setLibraryLocations(allLibs);
		}
	}

	private LibraryLocation[] kernelLocation(String subdir, String vmname) {
		List kernelLibraries = ((HyVMInstallType) getVMInstallType()).getKernelLibraries(
				getInstallLocation(), subdir, vmname);

		LibraryLocation[] kernelLibrariesLocation = new LibraryLocation[kernelLibraries.size()];
		for (int i = 0; i < kernelLibraries.size(); i++) {
			kernelLibrariesLocation[i] = (LibraryLocation) kernelLibraries.get(i);
		}
		
		return kernelLibrariesLocation;
	}

	/*
	 * (non-Javadoc) The setVMArgs does not cause an event change, so we have to
	 * subclass here to (potentially) change our library path based on changes
	 * to the -vmdir argument.
	 * 
	 * @see org.eclipse.jdt.launching.IVMInstall#setVMArgs(java.lang.String)
	 */
	public void setVMArgs(String vmArgs) {
		super.setVMArgs(vmArgs);
		setLibraryLocations(getLibraryLocations());
	}
}
