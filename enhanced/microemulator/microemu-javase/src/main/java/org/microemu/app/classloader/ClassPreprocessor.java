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

package org.microemu.app.classloader;

import java.io.IOException;
import java.io.InputStream;

import org.microemu.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * @author vlads
 *
 */
public class ClassPreprocessor {

	public static byte[] instrument(final InputStream classInputStream, InstrumentationConfig config) {
		try {
			ClassReader cr = new ClassReader(classInputStream);
			ClassWriter cw = new ClassWriter(0);
			ClassVisitor cv = new ChangeCallsClassVisitor(cw, config);
			cr.accept(cv, 0);
			return cw.toByteArray();
		} catch (IOException e) {
			Logger.error("Error loading MIDlet class", e);
			return null;
		} 
    }
	
}
