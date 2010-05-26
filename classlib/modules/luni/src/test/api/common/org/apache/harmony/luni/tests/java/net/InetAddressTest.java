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

package org.apache.harmony.luni.tests.java.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import tests.support.Support_Configuration;
import tests.support.Support_Excludes;

public class InetAddressTest extends junit.framework.TestCase {
    
    private static boolean someoneDone[] = new boolean[2];

    protected static boolean threadedTestSucceeded;

    protected static String threadedTestErrorString;

    /**
     * This class is used to test inet_ntoa, gethostbyaddr and gethostbyname
     * functions in the VM to make sure they're threadsafe. getByName will cause
     * the gethostbyname function to be called. getHostName will cause the
     * gethostbyaddr to be called. getHostAddress will cause inet_ntoa to be
     * called.
     */
    static class threadsafeTestThread extends Thread {
        private String lookupName;

        private InetAddress testAddress;

        private int testType;

        /*
         * REP_NUM can be adjusted if desired. Since this error is
         * non-deterministic it may not always occur. Setting REP_NUM higher,
         * increases the chances of an error being detected, but causes the test
         * to take longer. Because the Java threads spend a lot of time
         * performing operations other than running the native code that may not
         * be threadsafe, it is quite likely that several thousand iterations
         * will elapse before the first error is detected.
         */
        private static final int REP_NUM = 20000;

        public threadsafeTestThread(String name, String lookupName,
                InetAddress testAddress, int type) {
            super(name);
            this.lookupName = lookupName;
            this.testAddress = testAddress;
            testType = type;
        }

        public void run() {
            try {
                String correctName = testAddress.getHostName();
                String correctAddress = testAddress.getHostAddress();
                long startTime = System.currentTimeMillis();

                synchronized (someoneDone) {
                }

                for (int i = 0; i < REP_NUM; i++) {
                    if (someoneDone[testType]) {
                        break;
                    } else if ((i % 25) == 0
                            && System.currentTimeMillis() - startTime > 240000) {
                        System.out
                                .println("Exiting due to time limitation after "
                                        + i + " iterations");
                        break;
                    }

                    InetAddress ia = InetAddress.getByName(lookupName);
                    String hostName = ia.getHostName();
                    String hostAddress = ia.getHostAddress();

                    // Intentionally not looking for exact name match so that 
                    // the test works across different platforms that may or 
                    // may not include a domain suffix on the hostname
                    if (!hostName.startsWith(correctName)) {
                        threadedTestSucceeded = false;
                        threadedTestErrorString = (testType == 0 ? "gethostbyname"
                                : "gethostbyaddr")
                                + ": getHostName() returned "
                                + hostName
                                + " instead of " + correctName;
                        break;
                    }
                    // IP addresses should match exactly
                    if (!correctAddress.equals(hostAddress)) {
                        threadedTestSucceeded = false;
                        threadedTestErrorString = (testType == 0 ? "gethostbyname"
                                : "gethostbyaddr")
                                + ": getHostName() returned "
                                + hostAddress
                                + " instead of " + correctAddress;
                        break;
                    }

                }
                someoneDone[testType] = true;
            } catch (Exception e) {
                threadedTestSucceeded = false;
                threadedTestErrorString = e.toString();
            }
        }
    }
    
    /**
     * @tests java.net.InetAddress#getByName(String)
     */
    public void test_getByNameUnknownHostException() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Related to HARMONY-5784

        // loop a few times to flex the negative cache paths
        for (int i = 0; i < 5; i++) {
            try {
                InetAddress.getByName("unknown.unknown.bad");
                fail("An UnknownHostException should have been thrown");
            } catch (UnknownHostException e) {
                assertEquals("unknown.unknown.bad", e.getMessage());
            }
        }
    }

    /**
     * @tests java.net.InetAddress#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method boolean java.net.InetAddress.equals(java.lang.Object)
        InetAddress ia1 = InetAddress
                .getByName(Support_Configuration.InetTestAddress);
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        assertTrue("Equals returned incorrect result - " + ia1 + " != "
                + ia2, ia1.equals(ia2));
    }

    /**
     * @tests java.net.InetAddress#getAddress()
     */
    public void test_getAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method byte [] java.net.InetAddress.getAddress()
        try {
            InetAddress ia = InetAddress
                    .getByName(Support_Configuration.InetTestIP);
            byte[] caddr = Support_Configuration.InetTestCaddr;
            byte[] addr = ia.getAddress();
            for (int i = 0; i < addr.length; i++)
                assertTrue("Incorrect address returned", caddr[i] == addr[i]);
        } catch (java.net.UnknownHostException e) {
        }
        
        byte[] origBytes = new byte[] { 0, 1, 2, 3 };
        InetAddress address = InetAddress.getByAddress(origBytes);
        origBytes[0] = -1;
        byte[] newBytes = address.getAddress();
        assertSame((byte) 0, newBytes[0]);
    }

    /**
     * @tests java.net.InetAddress#getAllByName(java.lang.String)
     */
    @SuppressWarnings("nls")
    public void test_getAllByNameLjava_lang_String() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.net.InetAddress []
        // java.net.InetAddress.getAllByName(java.lang.String)
        InetAddress[] all = InetAddress
                .getAllByName(Support_Configuration.SpecialInetTestAddress);
        assertNotNull(all);
        // Number of aliases depends on individual test machine
        assertTrue(all.length >= 1);
        for (InetAddress alias : all) {
            // Check that each alias has the same hostname. Intentionally not
            // checking for exact string match.
            assertTrue(alias.getHostName().startsWith(
                    Support_Configuration.SpecialInetTestAddress));
        }// end for all aliases
        
        // check the getByName if there is a security manager.
        SecurityManager oldman = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            boolean exception = false;
            try {
                InetAddress.getByName("3d.com");
            } catch (SecurityException ex) {
                exception = true;
            }
            assertTrue("expected SecurityException", exception);
        } finally {
            System.setSecurityManager(oldman);
        }
        
        //Regression for HARMONY-56
        InetAddress[] ia = InetAddress.getAllByName(null);
        assertEquals("Assert 0: No loopback address", 1, ia.length);
        assertTrue("Assert 1: getAllByName(null) not loopback",
                ia[0].isLoopbackAddress());
        
        ia = InetAddress.getAllByName("");
        assertEquals("Assert 2: No loopback address", 1, ia.length);
        assertTrue("Assert 3: getAllByName(\"\") not loopback",
                ia[0].isLoopbackAddress());
        
        // Check that getting addresses by dotted string distinguish
        // IPv4 and IPv6 subtypes.
        InetAddress[] list = InetAddress.getAllByName("192.168.0.1");
        for (InetAddress addr : list) {
            assertFalse("Expected subclass returned",
                    addr.getClass().equals(InetAddress.class));
        }
    }

    /**
     * @tests java.net.InetAddress#getByName(java.lang.String)
     */
    public void test_getByNameLjava_lang_String() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.net.InetAddress
        // java.net.InetAddress.getByName(java.lang.String)
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        
        // Intentionally not testing for exact string match
        /* FIXME: comment the assertion below because it is platform/configuration dependent
         * Please refer to HARMONY-1664 (https://issues.apache.org/jira/browse/HARMONY-1664)
         * for details
         */
//        assertTrue(
//      "Expected " + Support_Configuration.InetTestAddress + "*",
//          ia2.getHostName().startsWith(Support_Configuration.InetTestAddress));

        // TODO : Test to ensure all the address formats are recognized
        InetAddress i = InetAddress.getByName("1.2.3");
        assertEquals("1.2.0.3",i.getHostAddress());
        i = InetAddress.getByName("1.2");
        assertEquals("1.0.0.2",i.getHostAddress());
        i = InetAddress.getByName(String.valueOf(0xffffffffL));
        assertEquals("255.255.255.255",i.getHostAddress());
        String s = "222.222.222.222....";
        i = InetAddress.getByName(s);
        assertEquals("222.222.222.222",i.getHostAddress());
    }

    /**
     * @tests java.net.InetAddress#getHostAddress()
     */
    public void test_getHostAddress() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.lang.String
        // java.net.InetAddress.getHostAddress()
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestAddress);
        assertTrue("getHostAddress returned incorrect result: "
                + ia2.getHostAddress() + " != "
                + Support_Configuration.InetTestIP, ia2.getHostAddress()
                .equals(Support_Configuration.InetTestIP));
    }

    /**
     * @tests java.net.InetAddress#getHostName()
     */
    public void test_getHostName() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.lang.String java.net.InetAddress.getHostName()
        InetAddress ia = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        
        // Intentionally not testing for exact string match
        /* FIXME: comment the assertion below because it is platform/configuration dependent
         * Please refer to HARMONY-1664 (https://issues.apache.org/jira/browse/HARMONY-1664)
         * for details
         */
//        assertTrue(
//      "Expected " + Support_Configuration.InetTestAddress + "*",
//      ia.getHostName().startsWith(Support_Configuration.InetTestAddress));

        // Test for any of the host lookups, where the default SecurityManager
        // is installed.

        SecurityManager oldman = System.getSecurityManager();
        try {
            String exp = Support_Configuration.InetTestIP;
            System.setSecurityManager(new MockSecurityManager());
            ia = InetAddress.getByName(exp);
            String ans = ia.getHostName();
        /* FIXME: comment the assertion below because it is platform/configuration dependent
         * Please refer to HARMONY-1664 (https://issues.apache.org/jira/browse/HARMONY-1664)
         * for details
         */
        //    assertEquals(Support_Configuration.InetTestIP, ans);
        } finally {
            System.setSecurityManager(oldman);
        }

        // Make sure there is no caching
        String originalPropertyValue = System
                .getProperty("networkaddress.cache.ttl");
        System.setProperty("networkaddress.cache.ttl", "0");

        // Test for threadsafety
        try {
            InetAddress lookup1 = InetAddress
                    .getByName(Support_Configuration.InetTestAddress);
            assertTrue(lookup1 + " expected "
                    + Support_Configuration.InetTestIP,
                    Support_Configuration.InetTestIP.equals(lookup1
                            .getHostAddress()));
            InetAddress lookup2 = InetAddress
                    .getByName(Support_Configuration.InetTestAddress2);
            assertTrue(lookup2 + " expected "
                    + Support_Configuration.InetTestIP2,
                    Support_Configuration.InetTestIP2.equals(lookup2
                            .getHostAddress()));
            threadsafeTestThread thread1 = new threadsafeTestThread("1",
                    lookup1.getHostName(), lookup1, 0);
            threadsafeTestThread thread2 = new threadsafeTestThread("2",
                    lookup2.getHostName(), lookup2, 0);
            threadsafeTestThread thread3 = new threadsafeTestThread("3",
                    lookup1.getHostAddress(), lookup1, 1);
            threadsafeTestThread thread4 = new threadsafeTestThread("4",
                    lookup2.getHostAddress(), lookup2, 1);

            // initialize the flags
            threadedTestSucceeded = true;
            synchronized (someoneDone) {
                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
            }
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            /* FIXME: comment the assertion below because it is platform/configuration dependent
             * Please refer to HARMONY-1664 (https://issues.apache.org/jira/browse/HARMONY-1664)
             * for details
             */
//            assertTrue(threadedTestErrorString, threadedTestSucceeded);
        } finally {
            // restore the old value of the property
            if (originalPropertyValue == null)
                // setting the property to -1 has the same effect as having the
                // property be null
                System.setProperty("networkaddress.cache.ttl", "-1");
            else
                System.setProperty("networkaddress.cache.ttl",
                        originalPropertyValue);
        }
    }

    /**
     * @tests java.net.InetAddress#getLocalHost()
     */
    public void test_getLocalHost() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.net.InetAddress
        // java.net.InetAddress.getLocalHost()

        // We don't know the host name or ip of the machine
        // running the test, so we can't build our own address
        DatagramSocket dg = new DatagramSocket(0, InetAddress
                .getLocalHost());
        assertTrue("Incorrect host returned", InetAddress.getLocalHost()
                .equals(dg.getLocalAddress()));
        dg.close();
    }

    /**
     * @tests java.net.InetAddress#getLocalHost()
     */
    public void test_getLocalHost_extended() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        class Inet_SecurityManager extends SecurityManager {
            @Override
            public void checkConnect(String host, int port) {
                super.checkConnect(host, port);
                throw new SecurityException();
            }
        }

        // Bogus, but we don't know the host name or ip of the machine
        // running the test, so we can't build our own address
        DatagramSocket dg = new DatagramSocket(0, InetAddress.getLocalHost());
        assertEquals("Incorrect host returned", InetAddress.getLocalHost(), dg
                .getLocalAddress());
        dg.close();

        SecurityManager oldman = System.getSecurityManager();
        try {
            System.setSecurityManager(new Inet_SecurityManager());
            assertTrue("Host address should be a loop back address",
                    InetAddress.getLocalHost().isLoopbackAddress());
        } finally {
            System.setSecurityManager(oldman);
        }
    }
    
    /**
     * @tests java.net.InetAddress#hashCode()
     */
    public void test_hashCode() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method int java.net.InetAddress.hashCode()
        try {
            InetAddress host = InetAddress
                    .getByName(Support_Configuration.InetTestAddress);
            int hashcode = host.hashCode();
            assertTrue("Incorrect hash returned: " + hashcode + " from host: "
                    + host, hashcode == Support_Configuration.InetTestHashcode);
        } catch (java.net.UnknownHostException e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.net.InetAddress#isMulticastAddress()
     */
    public void test_isMulticastAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertTrue(ia2.isMulticastAddress());
        ia2 = InetAddress.getByName("localhost");
        assertFalse(ia2.isMulticastAddress());
    }

    /**
     * @tests java.net.InetAddress#isAnyLocalAddress()
     */
    public void test_isAnyLocalAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertFalse(ia2.isAnyLocalAddress());
        ia2 = InetAddress.getByName("localhost");
        assertFalse(ia2.isAnyLocalAddress());
    }
    
    /**
     * @tests java.net.InetAddress#isLinkLocalAddress()
     */
    public void test_isLinkLocalAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertFalse(ia2.isLinkLocalAddress());
        ia2 = InetAddress.getByName("localhost");
        assertFalse(ia2.isLinkLocalAddress());
    }
    
    /**
     * @tests java.net.InetAddress#isLoopbackAddress()
     */
    public void test_isLoopbackAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertFalse(ia2.isLoopbackAddress());
        ia2 = InetAddress.getByName("localhost");
        assertTrue(ia2.isLoopbackAddress());
        ia2 = InetAddress.getByName("127.0.0.2");
        assertTrue(ia2.isLoopbackAddress());
    }
    
    /**
     * @tests java.net.InetAddress#isLoopbackAddress()
     */
    public void test_isSiteLocalAddress() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertFalse(ia2.isSiteLocalAddress());
        ia2 = InetAddress.getByName("localhost");
        assertFalse(ia2.isSiteLocalAddress());
        ia2 = InetAddress.getByName("127.0.0.2");
        assertFalse(ia2.isSiteLocalAddress());
        ia2 = InetAddress.getByName("243.243.45.3");
        assertFalse(ia2.isSiteLocalAddress());
        ia2 = InetAddress.getByName("10.0.0.2");
        assertTrue(ia2.isSiteLocalAddress());
    }
    
    /**
     * @tests java.net.InetAddress#isMCGlobal()/isMCLinkLocal/isMCNodeLocal/isMCOrgLocal/isMCSiteLocal
     */
    public void test_isMCVerify() throws UnknownHostException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia2 = InetAddress.getByName("239.255.255.255");
        assertFalse(ia2.isMCGlobal());
        assertFalse(ia2.isMCLinkLocal());
        assertFalse(ia2.isMCNodeLocal());
        assertFalse(ia2.isMCOrgLocal());
        assertTrue(ia2.isMCSiteLocal());
        ia2 = InetAddress.getByName("243.243.45.3");
        assertFalse(ia2.isMCGlobal());
        assertFalse(ia2.isMCLinkLocal());
        assertFalse(ia2.isMCNodeLocal());
        assertFalse(ia2.isMCOrgLocal());
        assertFalse(ia2.isMCSiteLocal());
        ia2 = InetAddress.getByName("250.255.255.254");
        assertFalse(ia2.isMCGlobal());
        assertFalse(ia2.isMCLinkLocal());
        assertFalse(ia2.isMCNodeLocal());
        assertFalse(ia2.isMCOrgLocal());
        assertFalse(ia2.isMCSiteLocal());
        ia2 = InetAddress.getByName("10.0.0.2");
        assertFalse(ia2.isMCGlobal());
        assertFalse(ia2.isMCLinkLocal());
        assertFalse(ia2.isMCNodeLocal());
        assertFalse(ia2.isMCOrgLocal());
        assertFalse(ia2.isMCSiteLocal());
    }
         
    /**
     * @tests java.net.InetAddress#toString()
     */
    public void test_toString() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Test for method java.lang.String java.net.InetAddress.toString()
        InetAddress ia2 = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        assertEquals("/" + Support_Configuration.InetTestIP, ia2.toString());
        // Regression for HARMONY-84
        InetAddress addr = InetAddress.getByName("localhost");
        assertEquals("Assert 0: wrong string from name", "localhost/127.0.0.1", addr.toString());
        InetAddress addr2 = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        assertEquals("Assert 1: wrong string from address", "/127.0.0.1", addr2.toString());
    }

    /**
     * @tests java.net.InetAddress#getByAddress(java.lang.String, byte[])
     */
    public void test_getByAddressLjava_lang_String$B() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Check an IPv4 address with an IPv6 hostname
        byte ipAddress[] = { 127, 0, 0, 1 };
        String addressStr = "::1";
        try {
            InetAddress addr = InetAddress.getByAddress(addressStr, ipAddress);
            addr = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }

        byte ipAddress2[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0,
                1 };
        addressStr = "::1";
        try {
            InetAddress addr = InetAddress.getByAddress(addressStr, ipAddress2);
            addr = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException e) {
            fail("Unexpected problem creating IP Address "
                    + ipAddress.length);
        }
    }

    /**
     * @tests java.net.InetAddress#getCanonicalHostName()
     */
    public void test_getCanonicalHostName() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress theAddress = null;
        theAddress = InetAddress.getLocalHost();
        assertTrue("getCanonicalHostName returned a zero length string ",
                theAddress.getCanonicalHostName().length() != 0);
        assertTrue("getCanonicalHostName returned an empty string ",
                !theAddress.equals(""));

        // test against an expected value
        InetAddress ia = InetAddress
                .getByName(Support_Configuration.InetTestIP);
        
        // Intentionally not testing for exact string match
        /* FIXME: comment the assertion below because it is platform/configuration dependent
         * Please refer to HARMONY-1664 (https://issues.apache.org/jira/browse/HARMONY-1664)
         * for details
         */
//        assertTrue(
//           "Expected " + Support_Configuration.InetTestAddress + "*", 
//           ia.getCanonicalHostName().startsWith(Support_Configuration.InetTestAddress));
    }
    
    /**
     * @tests java.net.InetAddress#isReachableI
     */
    public void test_isReachableI() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        InetAddress ia = Inet4Address.getByName("127.0.0.1");
        assertTrue(ia.isReachable(10000));
        ia = Inet4Address.getByName("127.0.0.1");
        try {
            ia.isReachable(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
    }

    /**
     * @tests java.net.InetAddress#isReachableLjava_net_NetworkInterfaceII
     */
    public void test_isReachableLjava_net_NetworkInterfaceII() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // tests local address
        InetAddress ia = Inet4Address.getByName("127.0.0.1");
        assertTrue(ia.isReachable(null, 0, 10000));
        ia = Inet4Address.getByName("127.0.0.1");
        try {
            ia.isReachable(null, -1, 10000);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
        try {
            ia.isReachable(null, 0, -1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
        try {
            ia.isReachable(null, -1, -1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
        // tests nowhere
        ia = Inet4Address.getByName("1.1.1.1");
        assertFalse(ia.isReachable(1000));
        assertFalse(ia.isReachable(null, 0, 1000));

        // Regression test for HARMONY-1842.
        ia = InetAddress.getByName("localhost"); //$NON-NLS-1$
        Enumeration<NetworkInterface> nif = NetworkInterface.getNetworkInterfaces();
        NetworkInterface netif;
        while(nif.hasMoreElements()) {
            netif = nif.nextElement();
            ia.isReachable(netif, 10, 1000);
        }
    } 

    // comparator for InetAddress objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            InetAddress initAddr = (InetAddress) initial;
            InetAddress desrAddr = (InetAddress) deserialized;

            byte[] iaAddresss = initAddr.getAddress();
            byte[] deIAAddresss = desrAddr.getAddress();
            for (int i = 0; i < iaAddresss.length; i++) {
                assertEquals(iaAddresss[i], deIAAddresss[i]);
            }
            assertEquals(initAddr.getHostName(), desrAddr.getHostName());
        }
    };
    
    // Regression Test for Harmony-2290
    public void test_isReachableLjava_net_NetworkInterfaceII_loopbackInterface() throws IOException {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        final int TTL = 20;
        final int TIME_OUT = 3000;
        
        NetworkInterface loopbackInterface = null;
        ArrayList<InetAddress> localAddresses = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                .getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface
                    .getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) {
                    loopbackInterface = networkInterface;
                } else {
                    localAddresses.add(address);
                }
            }
        }

        //loopbackInterface can reach local address
        if (null != loopbackInterface) {
            for (InetAddress destAddress : localAddresses) {
                assertTrue(destAddress.isReachable(loopbackInterface, TTL, TIME_OUT));
            }
        }

        //loopback Interface cannot reach outside address
        InetAddress destAddress = InetAddress.getByName("www.google.com");
        assertFalse(destAddress.isReachable(loopbackInterface, TTL, TIME_OUT));
    }

    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {

        if (Support_Excludes.isExcluded()) {
            return;
        }

        SerializationTest.verifySelf(InetAddress.getByName("localhost"),
                COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        if (Support_Excludes.isExcluded()) {
            return;
        }

        SerializationTest.verifyGolden(this,
                InetAddress.getByName("localhost"), COMPARATOR);
    }

    /**
     * @tests java.net.InetAddress#getByAddress(byte[])
     */
    public void test_getByAddress() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        // Regression for HARMONY-61
        try {
            InetAddress.getByAddress(null);
            fail("Assert 0: UnknownHostException must be thrown");
        } catch (UnknownHostException e) {
            // Expected
        }
    }
    
    class MockSecurityManager extends SecurityManager {        
        public void checkPermission(Permission permission) {
            if (permission.getName().equals("setSecurityManager")){
                return;
            }
            if (permission.getName().equals("3d.com")){
                throw new SecurityException();
            }
            super.checkPermission(permission);
        }
    }
}
