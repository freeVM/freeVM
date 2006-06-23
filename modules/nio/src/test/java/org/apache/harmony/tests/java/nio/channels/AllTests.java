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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.apache.harmony.tests.java.nio.channels");
        //$JUnit-BEGIN$
        suite.addTestSuite(FileChannelLockingTest.class);
        suite.addTestSuite(FileChannelTest.class);
        suite.addTestSuite(SinkChannelTest.class);
        suite.addTestSuite(DatagramChannelTest.class);
        suite.addTestSuite(PipeTest.class);
        suite.addTestSuite(ChannelsTest.class);
        suite.addTestSuite(ServerSocketChannelTest.class);
        suite.addTestSuite(SocketChannelTest.class);
        suite.addTestSuite(SourceChannelTest.class);
        suite.addTestSuite(SelectionKeyTest.class);
        suite.addTestSuite(SelectorTest.class);
        suite.addTestSuite(AlreadyConnectedExceptionTest.class);
        suite.addTestSuite(SelectableChannelTest.class);
        //$JUnit-END$
        return suite;
    }

}
