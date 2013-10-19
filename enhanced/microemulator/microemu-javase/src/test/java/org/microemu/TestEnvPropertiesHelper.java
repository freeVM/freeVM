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

package org.microemu;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.microemu.app.Config;

public class TestEnvPropertiesHelper {

	static final Properties utProperties = loadProperties(); 
	
	static final String FILE_NAME = "tests.properties";
	
	/**
	 * File  ${home}/.microemulator/test.properties is used for configuration
	 * @return
	 */
	public static String getProperty(String key, String defaultValue) {
		return utProperties.getProperty(key, defaultValue);
	}

	private static Properties loadProperties() {
		Properties prop = new Properties();
		
		File meHomeRoot = Config.getConfigPath();
		if (meHomeRoot == null) {
			return prop;
		}
		
		File file = new File(meHomeRoot, FILE_NAME);
		if (!file.exists() || !file.canRead()) {
			return prop;
		}
		
		FileInputStream input;
        try {
            input = new FileInputStream(file);
            prop.load((InputStream) input);
        } catch (Exception e) {
            System.err.println("Error reading properties " + e.toString());
        }
		return prop;
	}
}
