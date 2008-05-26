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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.microemu.app.classloader.ClassPreprocessor;
import org.microemu.app.classloader.InstrumentationConfig;
import org.microemu.device.Device;
import org.microemu.log.Logger;

public class AppletProducer {

	public static void createHtml(File htmlOutputFile, Device device, String className, File midletOutputFile,
			File appletPackageOutputFile, File deviceOutputFile, String deviceDescriptorLocation) throws IOException {
		int width = device.getNormalImage().getWidth();
		int height = device.getNormalImage().getHeight();
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(htmlOutputFile);
			writer.write("");
			writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n\n");
			writer.write("<html>\n");
			writer.write("\t<head>\n");
			writer.write("\t\t<title>MicroEmulator</title>\n");
			writer.write("\t</head>\n");
			writer.write("\t<body>\n");
			writer.write("\t\t<applet code=\"org.microemu.applet.Main\"\n");
			writer.write("\t\t\t\twidth=\"" + width + "\" height=\"" + height + "\"\n");
			writer.write("\t\t\t\tarchive=\"" + appletPackageOutputFile.getName() + ",");
			if (deviceOutputFile != null) {
				writer.write(deviceOutputFile.getName() + ",");
			}
			writer.write(midletOutputFile.getName() + "\">\n");
			writer.write("\t\t\t<param name=\"midlet\" value=\"" + className + "\">\n");
			if (deviceDescriptorLocation != null) {
				writer.write("\t\t\t<param name=\"device\" value=\"" + deviceDescriptorLocation + "\">\n");
			}
			writer.write("\t\t</applet>\n");
			writer.write("\t</body>\n");
			writer.write("</html>\n");
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public static void createMidlet(String midletInput, File midletOutputFile) throws IOException {
		JarInputStream jis = null;
		JarInputStream ijis = null;
		JarOutputStream jos = null;
		InstrumentationConfig config = new InstrumentationConfig();
		config.setEnhanceThreadCreation(false);
		try {
			jis = new JarInputStream(new URL(midletInput).openStream());
			Manifest manifest = jis.getManifest();
			if (manifest == null) {
				jos = new JarOutputStream(new FileOutputStream(midletOutputFile));
			} else {
				jos = new JarOutputStream(new FileOutputStream(midletOutputFile), manifest);
			}
		
			byte[] inputBuffer = new byte[1024];
			JarEntry jarEntry;
			while ((jarEntry = jis.getNextJarEntry()) != null) {
				if (jarEntry.isDirectory() == false) {
					String name = jarEntry.getName();
					int size = 0;
					int read;
					int length = inputBuffer.length;
					while ((read = jis.read(inputBuffer, size, length)) > 0) {
						size += read;
						
						length = 1024;
						if (size + length > inputBuffer.length) {
							byte[] newInputBuffer = new byte[size + length];
							System.arraycopy(inputBuffer, 0, newInputBuffer, 0, inputBuffer.length);
							inputBuffer = newInputBuffer;
						}
					}
					
					byte[] outputBuffer = inputBuffer;
					int outputSize = size;
					if (name.endsWith(".class")) {					
				        outputBuffer = ClassPreprocessor.instrument(new ByteArrayInputStream(inputBuffer, 0, size), config);
				        outputSize = outputBuffer.length;
					}
					jos.putNextEntry(new JarEntry(name));
					jos.write(outputBuffer, 0, outputSize);
				}
			}
			
			URL url = AppletProducer.class.getResource("/microemu-injected.jar");
			if (url != null) {
				ijis = new JarInputStream(url.openStream());
				while ((jarEntry = ijis.getNextJarEntry()) != null) {
					if (jarEntry.getName().equals("org/microemu/Injected.class")) {
						jos.putNextEntry(new JarEntry(jarEntry.getName()));
						int read;
						while ((read = ijis.read(inputBuffer)) > 0) {
							jos.write(inputBuffer, 0, read);
						}
					}
				}
			} else {
				Logger.error("Cannot find microemu-injected.jar resource in classpath");
			}
		} finally {
			IOUtils.closeQuietly(jis);
			IOUtils.closeQuietly(ijis);
			IOUtils.closeQuietly(jos);
		}
	}
		
}
