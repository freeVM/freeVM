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
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

/**
 * Used to attach to a VM in a separate thread, to allow for cancellation and
 * detect that the associated System process died before the connect occurred.
 */
class ConnectRunnable implements Runnable {

	private VirtualMachine virtualMachine = null;

	private ListeningConnector connector = null;

	private Map connectionMap = null;

	private Exception exception = null;

	/**
	 * Constructs a runnable to connect to a VM via the given connector with the
	 * given connection arguments.
	 * 
	 * @param connector
	 *            the VM connector.
	 * @param map
	 *            the map of arguments
	 */
	public ConnectRunnable(ListeningConnector connector, Map map) {
		this.connector = connector;
		this.connectionMap = map;
	}

	public void run() {
		try {
			virtualMachine = connector.accept(connectionMap);
		} catch (IOException e) {
			exception = e;
		} catch (IllegalConnectorArgumentsException e) {
			exception = e;
		}
	}

	/**
	 * Returns the VM that was attached to, or <code>null</code> if none.
	 * 
	 * @return the VM that was attached to, or <code>null</code> if none
	 */
	public VirtualMachine getVirtualMachine() {
		return virtualMachine;
	}

	/**
	 * Returns any exception that occurred while attaching, or <code>null</code>.
	 * 
	 * @return IOException or IllegalConnectorArgumentsException
	 */
	public Exception getException() {
		return exception;
	}
}

public class HyDebugVMRunner extends HyVMRunner {

	public HyDebugVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	protected void checkErrorMessage(IProcess process) throws CoreException {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		if (streamsProxy != null) {
			String errorMessage = streamsProxy.getErrorStreamMonitor()
					.getContents();
			if (errorMessage.length() == 0) {
				errorMessage = streamsProxy.getOutputStreamMonitor()
						.getContents();
			}
			if (errorMessage.length() != 0) {
				abort(errorMessage, null,
						IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
			}
		}
	}

	/**
	 * @see org.eclipse.jdt.launching.IVMRunner#run(VMRunnerConfiguration,
	 *      ILaunch, IProgressMonitor)
	 */
	public void run(VMRunnerConfiguration config, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor
				.beginTask(
						HyLauncherMessages
								.getString("HyDebugVMRunner.Launching_virtual_machine..._1"), 5); //$NON-NLS-1$
		subMonitor.subTask(HyLauncherMessages
				.getString("HyDebugVMRunner.Finding_free_socket..._2")); //$NON-NLS-1$

		int port = SocketUtil.findFreePort();
		if (port == -1) {
			abort(
					HyLauncherMessages
							.getString("HyDebugVMRunner.Could_not_find_a_free_socket_for_the_debugger_1"), null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE); //$NON-NLS-1$
		}

		subMonitor.worked(1);

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		subMonitor.subTask(HyLauncherMessages
				.getString("HyDebugVMRunner.Constructing_command_line..._3")); //$NON-NLS-1$

		String location = getJDKLocation();
		String program = constructProgramString(location, config);

		List arguments = new ArrayList(12);

		arguments.add(program);

		addArguments(config.getVMArguments(), arguments);

		addBootClassPathArguments(arguments, config);

		String[] cp = config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		arguments.add("-Xdebug"); //$NON-NLS-1$
		arguments
				.add("-Xrunjdwp:transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$

		arguments.add(config.getClassToLaunch());
		addArguments(config.getProgramArguments(), arguments);
		String[] cmdLine = new String[arguments.size()];
		arguments.toArray(cmdLine);

		String[] envp = config.getEnvironment();

		if (monitor.isCanceled()) {
			return;
		}

		subMonitor.worked(1);
		subMonitor.subTask(HyLauncherMessages
				.getString("HyDebugVMRunner.Starting_virtual_machine..._4")); //$NON-NLS-1$

		ListeningConnector connector = getConnector();
		if (connector == null) {
			abort(
					HyLauncherMessages
							.getString("HyDebugVMRunner.Couldn__t_find_an_appropriate_debug_connector_2"), null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); //$NON-NLS-1$
		}
		Map map = connector.defaultArguments();

		specifyArguments(map, port);
		Process p = null;
		try {
			try {
				// check for cancellation
				if (monitor.isCanceled()) {
					return;
				}

				connector.startListening(map);

				File workingDir = getWorkingDir(config);
				p = exec(cmdLine, workingDir, envp);
				if (p == null) {
					return;
				}

				// check for cancellation
				if (monitor.isCanceled()) {
					p.destroy();
					return;
				}

				IProcess process = newProcess(launch, p,
						renderProcessLabel(cmdLine), getDefaultProcessMap());
				process.setAttribute(IProcess.ATTR_CMDLINE,
						renderCommandLine(cmdLine));
				// Log the current launch command to the platform log
				logLaunchCmd(cmdLine, true);
				
				if (HyLaunchingPlugin.getDefault().isDebugging()
						&& (Platform
								.getDebugOption(HyLaunchingPlugin.DEBUG_LAUNCHING)
								.equalsIgnoreCase("true"))) { //$NON-NLS-1$
					traceLaunchCmd(cmdLine, envp, true);
				}
				subMonitor.worked(1);
				subMonitor
						.subTask(HyLauncherMessages
								.getString("HyDebugVMRunner.Establishing_debug_connection..._6")); //$NON-NLS-1$
				boolean retry = false;
				do {
					try {
						ConnectRunnable runnable = new ConnectRunnable(
								connector, map);
						Thread connectThread = new Thread(runnable,
								"Listening Connector"); //$NON-NLS-1$
						connectThread.start();
						while (connectThread.isAlive()) {
							if (monitor.isCanceled()) {
								connector.stopListening(map);
								p.destroy();
								return;
							}
							try {
								p.exitValue();
								// process has terminated - stop waiting for a
								// connection
								try {
									connector.stopListening(map);
								} catch (IOException e) {
									// expected
								}
								checkErrorMessage(process);
							} catch (IllegalThreadStateException e) {
								// expected while process is alive
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// Ignore
							}
						}

						Exception ex = runnable.getException();
						if (ex instanceof IllegalConnectorArgumentsException) {
							throw (IllegalConnectorArgumentsException) ex;
						}
						if (ex instanceof InterruptedIOException) {
							throw (InterruptedIOException) ex;
						}
						if (ex instanceof IOException) {
							throw (IOException) ex;
						}

						VirtualMachine vm = runnable.getVirtualMachine();
						if (vm != null) {
							JDIDebugModel.newDebugTarget(launch, vm,
									renderDebugTarget(
											config.getClassToLaunch(), port),
									process, true, false);
							subMonitor.worked(1);
							subMonitor.done();
						}
						return;
					} catch (InterruptedIOException e) {
						checkErrorMessage(process);

						// timeout, consult status handler if there is one
						IStatus status = new Status(
								IStatus.ERROR,
								HyLaunchingPlugin.getUniqueIdentifier(),
								IJavaLaunchConfigurationConstants.ERR_VM_CONNECT_TIMEOUT,
								"", e); //$NON-NLS-1$
						IStatusHandler handler = DebugPlugin.getDefault()
								.getStatusHandler(status);

						retry = false;
						if (handler == null) {
							// if there is no handler, throw the exception
							throw new CoreException(status);
						}
						Object result = handler.handleStatus(status, this);
						if (result instanceof Boolean) {
							retry = ((Boolean) result).booleanValue();
						}
					}
				} while (retry);
			} finally {
				connector.stopListening(map);
			}
		} catch (IOException e) {
			abort(
					HyLauncherMessages
							.getString("HyDebugVMRunner.Couldn__t_connect_to_VM_4"), e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED); //$NON-NLS-1$
		} catch (IllegalConnectorArgumentsException e) {
			abort(
					HyLauncherMessages
							.getString("HyDebugVMRunner.Couldn__t_connect_to_VM_5"), e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED); //$NON-NLS-1$
		}
		if (p != null) {
			p.destroy();
		}
	}

	protected ListeningConnector getConnector() {
		List connectors = Bootstrap.virtualMachineManager()
				.listeningConnectors();
		for (int i = 0; i < connectors.size(); i++) {
			ListeningConnector c = (ListeningConnector) connectors.get(i);
			if ("com.sun.jdi.SocketListen".equals(c.name())) //$NON-NLS-1$
				return c;
		}
		return null;
	}

	protected void specifyArguments(Map map, int portNumber) {
		Connector.IntegerArgument port = (Connector.IntegerArgument) map
				.get("port"); //$NON-NLS-1$
		port.setValue(portNumber);

		Connector.IntegerArgument timeoutArg = (Connector.IntegerArgument) map
				.get("timeout"); //$NON-NLS-1$
		if (timeoutArg != null) {
			int timeout = JavaRuntime.getPreferences().getInt(
					JavaRuntime.PREF_CONNECT_TIMEOUT);
			timeoutArg.setValue(timeout);
		}
	}
}