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

package org.apache.harmony.tests.java.nio.channels.spi;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for tests.api.java.nio.channels.spi");
        //$JUnit-BEGIN$
        suite.addTestSuite(AbstractInterruptibleChannelTest.class);
        suite.addTestSuite(AbstractSelectorTest.class);
        suite.addTestSuite(AbstractSelectableChannelTest.class);
        suite.addTestSuite(SelectorProviderTest.class);
        suite.addTestSuite(AbstractSelectionKeyTest.class);
        //$JUnit-END$
        return suite;
    }

}
