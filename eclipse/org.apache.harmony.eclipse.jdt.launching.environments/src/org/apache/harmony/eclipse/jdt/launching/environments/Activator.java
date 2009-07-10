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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.apache.harmony.eclipse.jdt.launching.environments";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Convenience method to write status information to the platform log.
     * 
     * @param msg
     *            information to be written to the platform log.
     */
    public void log(String msg) {
        log(msg, null);
    }

    /**
     * Convenience method to write problem information to the platform log.
     * 
     * @param msg
     *            additional information about the event
     * @param e
     *            exception encapsulating any non-fatal problem
     */
    public void log(String msg, Exception e) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, msg, e));
    }

    /**
     * Convenience method to write error information to the platform log.
     * 
     * @param msg
     *            additional information about the event
     * @param e
     *            exception encapsulating the error
     */
    public void logError(String msg, Exception e) {
        getLog().log(
                new Status(IStatus.INFO, PLUGIN_ID, IStatus.ERROR, msg, e));
    }

}
