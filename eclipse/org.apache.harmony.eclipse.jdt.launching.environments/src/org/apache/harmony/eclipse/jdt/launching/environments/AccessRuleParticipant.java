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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

public class AccessRuleParticipant implements IAccessRuleParticipant {

    static String[] packages = null;

    public IAccessRule[][] getAccessRules(IExecutionEnvironment environment,
            IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {

        // Read list of packages
        if (packages == null) {
            try {
                packages = getPackages();
            } catch (IOException e) {
                return new IAccessRule[0][];
            }
        }
        // Set up rules for specific packages
        IAccessRule[] packageRules = new IAccessRule[packages.length + 1];

        // Allow named packages
        for (int i = 0; i < packages.length; i++) {
            Path path = new Path(packages[i].replace('.', IPath.SEPARATOR));
            packageRules[i] = JavaCore.newAccessRule(path, IAccessRule.K_ACCESSIBLE);
        }

        // Disallow everything else
        packageRules[packages.length] = JavaCore.newAccessRule(
                new Path("**/*"), IAccessRule.K_NON_ACCESSIBLE); //$NON-NLS-1$

        // These rules apply equally to all libraries
        IAccessRule[][] allRules = new IAccessRule[libraries.length][];
        for (int i = 0; i < allRules.length; i++) {
            allRules[i] = packageRules;
        }
        return allRules;
    }

    private String[] getPackages() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/Harmony-Select-1.0");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        ArrayList<String> pkgs = new ArrayList<String>(50);
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() != 0 && line.charAt(0) != '#') {
                pkgs.add(line + "/*");
            }
            line = br.readLine();
        }
        br.close();

        return pkgs.toArray(new String[pkgs.size()]);
    }
}
