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

package org.microemu.android.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AndroidClassVisitor extends ClassAdapter {
	
	boolean isMidlet;
	
	public class AndroidMethodVisitor extends MethodAdapter {

		public AndroidMethodVisitor(MethodVisitor mv) {
			super(mv);
		}
		
	    public void visitFieldInsn(final int opcode, String owner, final String name, String desc) {
			mv.visitFieldInsn(opcode, fixPackage(owner), name, fixPackage(desc));
		}	

		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			if (isMidlet && opcode == Opcodes.INVOKEVIRTUAL) {
				if ((name.equals("getResourceAsStream")) && (owner.equals("java/lang/Class"))) {							
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, fixPackage("org/microemu/MIDletBridge"), name, fixPackage("(Ljava/lang/Class;Ljava/lang/String;)Ljava/io/InputStream;"));
					return;
				}
			}
			
			mv.visitMethodInsn(opcode, fixPackage(owner), name, fixPackage(desc));
		}

		public void visitTypeInsn(final int opcode, String desc) {
			super.visitTypeInsn(opcode, fixPackage(desc));
		}
		
	}

	public AndroidClassVisitor(ClassVisitor cv, boolean isMidlet) {
		super(cv);
		
		this.isMidlet = isMidlet;
	}

    public void visit(final int version, final int access, String name, final String signature, String superName, final String[] interfaces) {
    	for (int i = 0; i < interfaces.length; i++) {
    		interfaces[i] = fixPackage(interfaces[i]);
    	}
    	super.visit(version, access, fixPackage(name), signature, fixPackage(superName), interfaces);
	}

	public FieldVisitor visitField(final int access, final String name, String desc, final String signature, final Object value) {
		return super.visitField(access, name, fixPackage(desc), signature, value);
	}

	public MethodVisitor visitMethod(final int access, final String name, String desc, final String signature, final String[] exceptions) {
		return new AndroidMethodVisitor(super.visitMethod(access, name, fixPackage(desc), signature, exceptions));
	}
	
	public static String fixPackage(String name) {
		int index = name.indexOf("javax/microedition/lcdui/");
		if (index != -1) {
			name = name.replaceAll("javax/microedition/lcdui/", "javax/microedition/android/lcdui/");
		}
		
		return name;
	}		
	
}
