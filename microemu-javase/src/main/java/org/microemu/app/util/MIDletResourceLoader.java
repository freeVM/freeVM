/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.util;

import java.io.InputStream;

import org.microemu.Injected;
import org.microemu.log.Logger;
import org.microemu.util.ThreadUtils;

/**
 * @author vlads
 *
 * Use MIDletResourceLoader to load resources.
 * To solve resource resource loading paterns commonly used in MIDlet and not aceptable in Java SE application
 * when System class is called to load resource 
 * 
 * j2me example:
 * 
 *  String.class.getResourceAsStream(resourceName)
 *
 */
public class MIDletResourceLoader {
	
	//TODO make this configurable
	
	public static boolean traceResourceLoading = false;
	
	/**
	 * @deprecated find better solution to share variable
	 */
	public static ClassLoader classLoader;
	
	private static final String FQCN = Injected.class.getName();
	
	public static InputStream getResourceAsStream(Class origClass, String resourceName)  {
		if (traceResourceLoading) {
			Logger.debug("Loading MIDlet resource", resourceName);
		}
		if (classLoader != origClass.getClassLoader()) {
			// showWarning
			String callLocation  = ThreadUtils.getCallLocation(FQCN);
			if (callLocation != null) {
				Logger.warn("attempt to load resource [" + resourceName + "] using System ClasslLoader from " + callLocation);
			}
		}
		resourceName = resolveName(origClass, resourceName);
			
		InputStream is = classLoader.getResourceAsStream(resourceName);
		if (is == null) {
			Logger.debug("Resource not found ", resourceName);
		}
		return is;
	}

    private static String resolveName(Class origClass, String name) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            while (origClass.isArray()) {
                origClass = origClass.getComponentType();
            }
            String baseName = origClass.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/') + "/" +name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }
}
