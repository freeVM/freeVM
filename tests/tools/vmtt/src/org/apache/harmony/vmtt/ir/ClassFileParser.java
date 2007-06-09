/*
    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.apache.harmony.vmtt.ir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.harmony.vmtt.ir.constants.CPTags;

/**
 * @author Aleksey V. Golubitsky
 * @version $Revision: 1.3 $
 */

public abstract class ClassFileParser extends CPTags {
	
	protected ClassFile classFile = null;
	protected DataInputStream dis = null;
	
	public void setClassFile(ClassFile cf)
	throws NullPointerException {
	    if (cf == null) {
	        throw new NullPointerException();
	    }
	    classFile = cf;
	}
	
	public ClassFile getClassFile() {
	    return classFile;
	}
	
	public void setInputFile(File inputFile)
	throws NullPointerException, IOException, FileNotFoundException {
	    if (inputFile == null) {
	        throw new NullPointerException();
	    }
	    dis = new DataInputStream(new ByteArrayInputStream(readClassFile(inputFile)));
	}
	
	protected byte[] readClassFile(File f)
	throws FileNotFoundException, IOException {
		FileInputStream istream = new FileInputStream(f);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b = 0; 
		while ((b = istream.read()) != -1) {
			baos.write(b);
		}
		return baos.toByteArray();
	}
	
	public abstract void parse() throws ClassFileFormatException;

}