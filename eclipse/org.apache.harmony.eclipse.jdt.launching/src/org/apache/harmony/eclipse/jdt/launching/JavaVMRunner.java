/* Copyright 2000, 2005 The Apache Software Foundation or its licensors, as applicable
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

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public abstract class JavaVMRunner extends AbstractVMRunner {

	protected IVMInstall vmInstance;

	public JavaVMRunner(IVMInstall vmInstance) {
		super();
		this.vmInstance = vmInstance;
	}

	protected String renderDebugTarget(String classToRun, int host) {
		String format = HyLauncherMessages
				.getString("javaVMRunner.format.dbgTarget"); //$NON-NLS-1$
		return MessageFormat.format(format, new String[] { classToRun,
				String.valueOf(host) });
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format = HyLauncherMessages
				.getString("javaVMRunner.format.processLabel"); //$NON-NLS-1$
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { commandLine[0],
				timestamp });
	}

	protected static String renderCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < commandLine.length; i++) {
			buf.append(' ');
			char[] characters = commandLine[i].toCharArray();
			StringBuffer command = new StringBuffer();
			boolean containsSpace = false;
			for (int j = 0; j < characters.length; j++) {
				char character = characters[j];
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command);
				buf.append('\"');
			} else {
				buf.append(command);
			}
		}
		return buf.toString();
	}

	protected void addArguments(String[] args, List v) {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			v.add(args[i]);
		}
	}

	protected String getJDKLocation() {
		File location = vmInstance.getInstallLocation();
		return location.getAbsolutePath();
	}

	/**
	 * Returns the working directory to use for the launched VM, or
	 * <code>null</code> if the working directory is to be inherited from the
	 * current process.
	 * 
	 * @return the working directory to use
	 * @exception CoreException
	 *                if the working directory specified by the configuration
	 *                does not exist or is not a directory
	 */
	protected File getWorkingDir(VMRunnerConfiguration config)
			throws CoreException {
		String path = config.getWorkingDirectory();
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort(
					MessageFormat
							.format(
									HyLauncherMessages
											.getString("javaVMRunner.Specified_working_directory_does_not_exist_or_is_not_a_directory__{0}_1"), new String[] { path }), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		return dir;
	}

	/**
	 * @see VMRunner#getPluginIdentifier()
	 */
	protected String getPluginIdentifier() {
		return HyLaunchingPlugin.getUniqueIdentifier();
	}

	/**
	 * Returns the default process attribute map for java processes.
	 * 
	 * @return default process attribute map for java processes
	 */
	protected Map getDefaultProcessMap() {
		Map map = new HashMap();
		map.put(IProcess.ATTR_PROCESS_TYPE, "harmony"); //$NON-NLS-1$
		return map;
	}

	protected void addBootClassPathArguments(List arguments,
			VMRunnerConfiguration config) {
		Map map = config.getVMSpecificAttributesMap();
		if (map == null) {
			return;
		}
		String[] prependBootCP = (String[]) map
				.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
		String[] bootCP = (String[]) map
				.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
		String[] appendBootCP = (String[]) map
				.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
		if (prependBootCP == null && bootCP == null && appendBootCP == null) {
			// use old single attribute instead of new attributes if not
			// specified
			bootCP = config.getBootClassPath();
		}
		if (prependBootCP != null) {
			arguments
					.add("-Xbootclasspath/p:" + convertClassPath(prependBootCP)); //$NON-NLS-1$
		}
		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			} else {
				// empty
				arguments.add("-Xbootclasspath:"); //$NON-NLS-1$	
			}
		}
		if (appendBootCP != null) {
			arguments
					.add("-Xbootclasspath/a:" + convertClassPath(appendBootCP)); //$NON-NLS-1$
		}
	}

	protected String convertClassPath(String[] cp) {
		if (cp.length == 0) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer buf = new StringBuffer();
		int pathCount = 0;
		for (int i = 0; i < cp.length; i++) {
			if (pathCount > 0) {
				buf.append(File.pathSeparator);
			}
			buf.append(cp[i]);
			pathCount++;
		}
		return buf.toString();
	}
}