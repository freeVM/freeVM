/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.eclipse.jdt.launching.environments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.CompatibleEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzerDelegate;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

public class ExecutionEnvironmentAnalyzer implements
        IExecutionEnvironmentAnalyzerDelegate {

    /**
     * Analyzes the given vm install and returns a collection of compatible
     * Select execution environments, possibly empty.
     * 
     * For Select we may match on a subset of SE installs.
     */
    public CompatibleEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor)
            throws CoreException {

        // Get the Java version string from the VM
        if (!(vm instanceof IVMInstall2)) {
            Activator.getDefault().log("VM is not a v2 installation : " + vm.getName());
            return new CompatibleEnvironment[0];
        }
        IVMInstall2 vm2 = (IVMInstall2) vm;

        String javaVersion = vm2.getJavaVersion();
        if (javaVersion == null) {
            Activator.getDefault().log("VM does not report version string");
            return new CompatibleEnvironment[0];
        }

        // Select is a subset of SE runtimes that are 1.5 or above
        if (javaVersion.startsWith("1.7") || javaVersion.startsWith("1.6")
                || javaVersion.startsWith("1.5")) {

            IExecutionEnvironmentsManager manager = JavaRuntime
                    .getExecutionEnvironmentsManager();
            IExecutionEnvironment env = manager.getEnvironment("Harmony-Select-1.0");
            if (env != null) {
                CompatibleEnvironment[] result = new CompatibleEnvironment[1];
                // SE is not a perfect match
                // TODO: figure out how we detect a real Select runtime
                result[0] = new CompatibleEnvironment(env, false);
                return result;
            }
        }

        // We didn't match as a subset
        return new CompatibleEnvironment[0];
    }
}
