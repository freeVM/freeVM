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

package org.apache.harmony.luni.tests.java.util;

import java.util.FormatterClosedException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class FormatterClosedExceptionTest extends TestCase {

	/**
	 * @tests java.util.FormatterClosedException#FormatterClosedException
	 */
	public void test_formatterClosedException() {
		FormatterClosedException formatterClosedException = new FormatterClosedException();
		assertTrue(null != formatterClosedException);
	}

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new FormatterClosedException());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new FormatterClosedException());
    }
}
