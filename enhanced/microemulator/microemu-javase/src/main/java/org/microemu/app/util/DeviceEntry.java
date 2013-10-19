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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.microemu.EmulatorContext;
import org.microemu.app.Common;
import org.microemu.app.Config;
import org.microemu.log.Logger;

import com.barteo.emulator.device.Device;

public class DeviceEntry {
	
	private String name;

	private String fileName;

	private String descriptorLocation;

	private boolean defaultDevice;

	private boolean canRemove;

	/**
	 * @deprecated
	 */
	private String className;

	/**
	 * @deprecated
	 */
	private EmulatorContext emulatorContext;

	public DeviceEntry(String name, String fileName, String descriptorLocation, boolean defaultDevice) {
		this(name, fileName, descriptorLocation, defaultDevice, true);
	}

	public DeviceEntry(String name, String fileName, String descriptorLocation, boolean defaultDevice, boolean canRemove) {
		this.name = name;
		this.fileName = fileName;
		this.descriptorLocation = descriptorLocation;
		this.defaultDevice = defaultDevice;
		this.canRemove = canRemove;
	}

	/**
	 * @deprecated use new DeviceEntry(String name, String fileName, String descriptorLocation, boolean defaultDevice);
	 */
	public DeviceEntry(String name, String fileName, boolean defaultDevice, String className,
			EmulatorContext emulatorContext) {
		this(name, fileName, null, defaultDevice, true);

		this.className = className;
		this.emulatorContext = emulatorContext;
	}

	public boolean canRemove() {
		return canRemove;
	}

	public String getDescriptorLocation() {
		if (descriptorLocation == null) {
			URL[] urls = new URL[1];
			try {
				urls[0] = new File(Config.getConfigPath(), fileName).toURI().toURL();
				ClassLoader classLoader = Common.createExtensionsClassLoader(urls);
				Class deviceClass = Class.forName(className, true, classLoader);
				Device device = (Device) deviceClass.newInstance();

				com.barteo.emulator.EmulatorContext oldContext = new com.barteo.emulator.EmulatorContext(emulatorContext);

				device.init(oldContext);
				descriptorLocation = device.getDescriptorLocation();
			} catch (MalformedURLException ex) {
				Logger.error(ex);
			} catch (ClassNotFoundException ex) {
				Logger.error(ex);
			} catch (InstantiationException ex) {
				Logger.error(ex);
			} catch (IllegalAccessException ex) {
				Logger.error(ex);
			}

		}

		return descriptorLocation;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * @deprecated
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public boolean isDefaultDevice() {
		return defaultDevice;
	}

	public void setDefaultDevice(boolean b) {
		defaultDevice = b;
	}

	public boolean equals(DeviceEntry test) {
		if (test == null) {
			return false;
		}
		if (test.getDescriptorLocation().equals(getDescriptorLocation())) {
			return true;
		}

		return false;
	}

	public String toString() {
		if (defaultDevice) {
			return name + " (default)";
		} else {
			return name;
		}
	}

}