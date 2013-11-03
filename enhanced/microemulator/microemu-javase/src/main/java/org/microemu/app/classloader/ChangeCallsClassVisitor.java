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

import org.microemu.app.util.MIDletThread;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlads
 *
 */
public class ChangeCallsClassVisitor extends ClassAdapter {

	InstrumentationConfig config;
	
	public ChangeCallsClassVisitor(ClassVisitor cv, InstrumentationConfig config) {
		super(cv);
		this.config = config;
	}

    public void visit(final int version, final int access, final String name, final String signature, String superName, final String[] interfaces) {
    	if  ((config.isEnhanceThreadCreation()) && (superName.equals("java/lang/Thread"))) {
    		superName = ChangeCallsMethodVisitor.codeName(MIDletThread.class);
    	}
    	super.visit(version, access, name, signature, superName, interfaces);
	}
    
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
		return  new ChangeCallsMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), config);
	}

}
