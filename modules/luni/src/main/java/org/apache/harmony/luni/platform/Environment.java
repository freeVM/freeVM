/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.luni.platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment
 */
public class Environment {

	private static Map<String, String> envMap = new HashMap<String, String>();

	static {
        byte[] bytes = getEnvBytes();
        if (bytes != null) {
            String[] envStrings = new String(bytes).split("\0");
            for (int i = 0; i < envStrings.length; i++) {
                int separator = envStrings[i].indexOf("=");
                envMap.put(envStrings[i].substring(0, separator), envStrings[i]
                        .substring(separator + 1));
            }
        }
	}

	/**
	 * Returns a Map of the current environment variables, containing key and
	 * value pairs.
	 * 
	 * @return a Map containing the environment variables and their values
	 */
	public static Map<String, String> getenv() {
		return envMap;
	}

	/**
	 * Returns a String containing the value of the specified name environment
	 * variable
	 * 
	 * @param name -
	 *            the environment variable to get the value of
	 * 
	 * @return the value of the environment variable specified
	 */
	public static String getenv(String name) {
		return envMap.get(name);
	}

	private static native byte[] getEnvBytes();
}
