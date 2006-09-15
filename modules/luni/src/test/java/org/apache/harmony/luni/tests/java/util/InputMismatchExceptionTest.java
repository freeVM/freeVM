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

import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class InputMismatchExceptionTest extends TestCase {

    private static final String ERROR_MESSAGE = "for serialization test"; //$NON-NLS-1$

    /**
     * @tests java.util.InputMismatchException#InputMismatchException()
     */
    @SuppressWarnings("cast")
    public void test_Constructor() {
        InputMismatchException exception = new InputMismatchException();
        assertNotNull(exception);
        assertTrue(exception instanceof NoSuchElementException);
        assertTrue(exception instanceof Serializable);
    }

    /**
     * @tests java.util.InputMismatchException#InputMismatchException(String)
     */
    public void test_ConstructorLjava_lang_String() {
        InputMismatchException exception = new InputMismatchException(
                ERROR_MESSAGE);
        assertNotNull(exception);
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new InputMismatchException(ERROR_MESSAGE));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new InputMismatchException(
                ERROR_MESSAGE));
    }
}
