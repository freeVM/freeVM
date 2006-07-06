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

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.security.Permission;

import junit.framework.TestCase;

public class SelectorProviderTest extends TestCase {

    /**
     * @tests SelectorProvider#provider() using security manager
     */
    public void test_provider_security() {        
        SecurityManager originalSecuirtyManager = System.getSecurityManager();
        System.setSecurityManager(new MockSelectorProviderSecurityManager());
        try {
            new MockSelectorProvider();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(originalSecuirtyManager);
        }
    }

    /**
     * @tests SelectorProvider#provider() using security manager
     */
    public void test_provider_security_twice() {
        SelectorProvider.provider();
        SecurityManager originalSecuirtyManager = System.getSecurityManager();
        System.setSecurityManager(new MockSelectorProviderSecurityManager());
        try {
            // should not throw SecurityException since it has been initialized
            // in the begining of this method.
            SelectorProvider testProvider = SelectorProvider.provider();
            assertNotNull(testProvider);
        } finally {
            System.setSecurityManager(originalSecuirtyManager);
        }
    }

    private static class MockSelectorProviderSecurityManager extends
            SecurityManager {

        public MockSelectorProviderSecurityManager() {
            super();
        }

        public void checkPermission(Permission perm) {
            if (perm instanceof RuntimePermission) {
                if ("selectorProvider".equals(perm.getName())) {
                    throw new SecurityException();
                }
            }
        }

        public void checkPermission(Permission perm, Object context) {
            if (perm instanceof RuntimePermission) {
                if ("selectorProvider".equals(perm.getName())) {
                    throw new SecurityException();
                }
            }
        }
    }

    private class MockSelectorProvider extends SelectorProvider {

        public MockSelectorProvider() {
            super();
        }

        public DatagramChannel openDatagramChannel() throws IOException {
            return null;
        }

        public Pipe openPipe() throws IOException {
            return null;
        }

        public AbstractSelector openSelector() throws IOException {
            return MockAbstractSelector.openSelector();
        }

        public ServerSocketChannel openServerSocketChannel() throws IOException {
            return null;
        }

        public SocketChannel openSocketChannel() throws IOException {
            return null;
        }
    }
}