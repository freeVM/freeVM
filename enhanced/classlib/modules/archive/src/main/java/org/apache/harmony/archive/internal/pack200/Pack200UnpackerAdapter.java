/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.archive.internal.pack200;

// NOTE: Do not use generics in this code; it needs to run on JVMs < 1.5
// NOTE: Do not extract strings as messages; this code is still a
// work-in-progress
// NOTE: Also, don't get rid of 'else' statements for the hell of it ...
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200.Unpacker;

/**
 * This class provides the binding between the standard Pack200 interface and
 * the internal interface for (un)packing. As this uses generics for the
 * SortedMap, this class must be compiled and run on a Java 1.5 system. However,
 * Java 1.5 is not necessary to use the internal libraries for unpacking.
 */
public class Pack200UnpackerAdapter extends Pack200Adapter implements Unpacker {
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.jar.Pack200.Unpacker#unpack(java.io.InputStream,
	 *      java.util.jar.JarOutputStream)
	 */
	public void unpack(InputStream in, JarOutputStream out) throws IOException {
		if (in == null || out == null)
			throw new IllegalArgumentException(
					"Must specify both input and output streams");
		completed(0);
		try {
			while (in.available() > 0) {
				Segment s = Segment.parse(in);
				s.writeJar(out, in);
				out.flush();
			}
		} catch (Pack200Exception e) {
			throw new IOException("Failed to unpack Jar:" + String.valueOf(e));
		}
		completed(1);
		in.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.jar.Pack200.Unpacker#unpack(java.io.File,
	 *      java.util.jar.JarOutputStream)
	 */
	public void unpack(File file, JarOutputStream out) throws IOException {
		if (file == null || out == null)
			throw new IllegalArgumentException(
					"Must specify both input and output streams");
		int size = (int) file.length();
		int bufferSize = (size > 0 && size < DEFAULT_BUFFER_SIZE ? size
				: DEFAULT_BUFFER_SIZE);
		InputStream in = new BufferedInputStream(new FileInputStream(file),
				bufferSize);
		unpack(in, out);
	}
}
