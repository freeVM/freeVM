/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Evgueni V. Brevnov, Roman S. Bushmanov
 * @version $Revision$
 */
package java.lang;

import junit.framework.TestCase;

/**
 * tested class: java.lang.Class
 * tested method: desiredAssertionStatus
 */
public class ClassTestDesiredAssertionStatus extends TestCase {

    /**
     * default assertion status must be false.
     */
    public void test1() {
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        scl.setDefaultAssertionStatus(true);
        scl.clearAssertionStatus();
        assertFalse(ClassTestDesiredAssertionStatus.class
            .desiredAssertionStatus());
    }
}