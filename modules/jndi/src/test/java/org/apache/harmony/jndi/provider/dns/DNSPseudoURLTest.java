/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Alexei Y. Zakharov
 * @version $Revision: 1.1.2.5 $
 */

package org.apache.harmony.jndi.provider.dns;

import junit.framework.TestCase;

/**
 * <code>org.apache.harmony.jndi.provider.dns.DNSPseudoURL</code> unit test.
 * @author Alexei Zakharov
 * @version $Revision: 1.1.2.5 $
 */
public class DNSPseudoURLTest extends TestCase {

    public void testConstructor() {
        DNSPseudoURL url = null;

        url = new DNSPseudoURL("dns://super.puper.server.ru:54/sub.domain.com");
        assertEquals("super.puper.server.ru", url.getHost());
        assertEquals(false, url.isHostIpGiven());
        assertEquals(54, url.getPort());
        assertEquals("sub.domain.com.", url.getDomain());

        url = new DNSPseudoURL("dns://123.456.678.1/example.com.");
        assertEquals("123.456.678.1", url.getHost());
        assertEquals(true, url.isHostIpGiven());
        assertEquals(53, url.getPort());
        assertEquals("example.com.", url.getDomain());
        
        try {
            url = new DNSPseudoURL("dns://a.com/domain.com/mama");
            fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            // correct behaviour
        }
    }
    
    public void testConstructorDefaults() {
        DNSPseudoURL url;

        url = new DNSPseudoURL("dns://www.mydomain.ru");
        assertEquals("www.mydomain.ru", url.getHost());
        assertEquals(false, url.isHostIpGiven());
        assertEquals(53, url.getPort());
        assertEquals(".", url.getDomain());

        url = new DNSPseudoURL("dns:/mydomain.org");
        assertEquals("localhost", url.getHost());
        assertEquals(false, url.isHostIpGiven());
        assertEquals(53, url.getPort());
        assertEquals("mydomain.org.", url.getDomain());

        url = new DNSPseudoURL("dns:");
        assertEquals("localhost", url.getHost());
        assertEquals(false, url.isHostIpGiven());
        assertEquals(53, url.getPort());
        assertEquals(".", url.getDomain());
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DNSPseudoURLTest.class);
    }

}
