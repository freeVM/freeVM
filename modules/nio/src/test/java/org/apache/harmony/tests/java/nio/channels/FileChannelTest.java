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

package org.apache.harmony.tests.java.nio.channels;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

public class FileChannelTest extends TestCase {

	/**
	 * @tests java.nio.channels.FileChannel#isOpen()
	 */
	public void test_close() throws Exception {
		// Regression for HARMONY-40
		File logFile = File.createTempFile("out", "tmp");
		logFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(logFile, true);
		FileChannel channel = out.getChannel();
		out.write(1);
		out.close();
		assertFalse("Assert 0: Channel is still open", channel.isOpen());
	}
}
