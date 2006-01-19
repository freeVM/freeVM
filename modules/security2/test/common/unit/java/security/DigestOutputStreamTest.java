/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.harmony.security.MDGoldenData;
import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for fields and methods of class <code>DigestInputStream</code>
 * 
 */
public class DigestOutputStreamTest extends PerformanceTest {

    /**
     * Message digest algorithm name used during testing
     */
    private static final String algorithmName[] = {
            "SHA-1",
            "SHA",
            "SHA1",
            "SHA-256",
            "SHA-384",
            "SHA-512",
            "MD5",
    };
    /**
     * Chunk size for read(byte, off, len) tests
     */
    private static final int CHUNK_SIZE = 32;
    /**
     * Test message for digest computations
     */
    private static final byte[] myMessage = MDGoldenData.getMessage();
    /**
     * The length of test message
     */
    private static final int MY_MESSAGE_LEN = myMessage.length;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for DigestInputStreamTest.
     * @param name
     */
    public DigestOutputStreamTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test #1 for <code>DigestOutputStream</code> constructor<br>
     * 
     * Assertion: creates new <code>DigestOutputStream</code> instance
     * using valid parameters (both non <code>null</code>)
     */
    public final void testDigestOutputStream01() {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                OutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                OutputStream dos = new DigestOutputStream(bos, md);
                assertTrue(dos instanceof DigestOutputStream);
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>DigestOutputStream</code> constructor<br>
     * 
     * Assertion: creates new <code>DigestOutputStream</code> instance
     * using valid parameters (both <code>null</code>)
     */
    public final void testDigestOutputStream02() {
        OutputStream dos = new DigestOutputStream(null, null);
        assertTrue(dos instanceof DigestOutputStream);
    }

    /**
     * Test #1 for <code>write(int)</code> method<br>
     * 
     * Assertion: writes the byte to the output stream<br>
     * Assertion: updates associated digest<br>
     */
    public final void testWriteint01()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }
                // check that bytes have been written correctly
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(),
                        bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>write(int)</code> method<br>
     * Test #1 for <code>on(boolean)</code> method<br>
     * 
     * Assertion: <code>write(int)</code> must not update digest if it is off<br>
     * Assertion: <code>on(boolean)</code> turns digest functionality on
     * if <code>true</code> passed as a parameter or off if <code>false</code>
     * passed
     */
    public final void testWriteint02()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // turn digest off
                dos.on(false);
                
                for (int i=0; i<MY_MESSAGE_LEN; i++) {
                    dos.write(myMessage[i]);
                }
                
                // check that bytes have been written correctly
                assertTrue("write", Arrays.equals(MDGoldenData.getMessage(),
                        bos.toByteArray()));
                // check that digest value has not been updated by write()
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #3 for <code>write(int)</code> method<br>
     * 
     * Assertion: broken <code>DigestOutputStream</code>instance: 
     * <code>OutputStream</code> not set. <code>write(int)</code> must
     * not work
     */
    public final void testWriteint03()
        throws IOException {
        boolean passed = false;
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(null, md);
                // must result in an exception
                try {
                    for (int i=0; i<MY_MESSAGE_LEN; i++) {
                        dos.write(myMessage[i]);
                    }
                } catch (Exception e) {
                    passed = true;
                    logln(getName() + ": " + e);
                }
                assertTrue(passed);
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>write(int)</code> method<br>
     * 
     * Assertion: broken <code>DigestOutputStream</code>instance: 
     * associated <code>MessageDigest</code> not set.
     * <code>write(int)</code> must not work when digest
     * functionality is on
     */
    public final void testWriteint04()
        throws IOException {
        boolean passed = false;
        OutputStream os = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(os, null);
        // must result in an exception
        try {
            for (int i=0; i<MY_MESSAGE_LEN; i++) {
                dos.write(myMessage[i]);
            }
        } catch (Exception e) {
            passed = true;
            logln(getName() + ": " + e);
        }
        assertTrue(passed);
    }

    /**
     * Test #5 for <code>write(int)</code> method<br>
     * Test #2 for <code>on(boolean)</code> method<br>
     * 
     * Assertion: broken <code>DigestOutputStream</code>instance: 
     * associated <code>MessageDigest</code> not set.
     * <code>write(int)</code> must work when digest
     * functionality is off
     */
    public final void testWriteint05()
        throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
        DigestOutputStream dos = new DigestOutputStream(bos, null);
        // set digest functionality to off
        dos.on(false);
        // the following must pass without any exception
        for (int i=0; i<MY_MESSAGE_LEN; i++) {
            dos.write(myMessage[i]);
        }
        // check that bytes have been written correctly
        assertTrue(Arrays.equals(MDGoldenData.getMessage(),
                bos.toByteArray()));
    }

    /**
     * Test #1 for <code>write(byte[],int,int)</code> method<br>
     * 
     * Assertion: put bytes into output stream<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testWritebyteArrayintint01()
        throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // write message at once
                dos.write(myMessage, 0, MY_MESSAGE_LEN);
                
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>write(byte[],int,int)</code> method<br>
     * 
     * Assertion: put bytes into output stream<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testWritebyteArrayintint02()
        throws IOException {
        // check precondition
        assertTrue(MY_MESSAGE_LEN % CHUNK_SIZE == 0);
        for (int k=0; k<algorithmName.length; k++) {
            try {
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }


    /**
     * Test #3 for <code>write(byte[],int,int)</code> method<br>
     * 
     * Assertion: put bytes into output stream<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testWritebyteArrayintint03()
        throws NoSuchAlgorithmException,
               IOException {
        // check precondition
        assertTrue(MY_MESSAGE_LEN % (CHUNK_SIZE+1) != 0);
        
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/(CHUNK_SIZE+1); i++) {
                    dos.write(myMessage, i*(CHUNK_SIZE+1), CHUNK_SIZE+1);
                }
                // write remaining bytes
                dos.write(myMessage,
                        MY_MESSAGE_LEN/(CHUNK_SIZE+1)*(CHUNK_SIZE+1),
                        MY_MESSAGE_LEN % (CHUNK_SIZE+1));
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has been updated properly
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k])));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>write(byte[],int,int)</code> method<br>
     * 
     * Assertion: put bytes into output stream<br>
     * 
     * Assertion: does not update associated digest if digest
     * functionality is off<br>
     */
    public final void testWritebyteArrayintint04()
        throws NoSuchAlgorithmException,
               IOException {
        // check precondition
        assertTrue(MY_MESSAGE_LEN % CHUNK_SIZE == 0);

        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // set digest functionality off
                dos.on(false);
                
                // write message by chunks
                for (int i=0; i<MY_MESSAGE_LEN/CHUNK_SIZE; i++) {
                    dos.write(myMessage, i*CHUNK_SIZE, CHUNK_SIZE);
                }
                
                // check write
                assertTrue("write", Arrays.equals(myMessage, bos.toByteArray()));
                // check that associated digest has not been updated
                assertTrue("update", Arrays.equals(dos.getMessageDigest().digest(),
                        MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>getMessageDigest()</code> method<br>
     * 
     * Assertion: reutns associated message digest<br>
     */
    public final void testGetMessageDigest() {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(null, md);
                
                assertTrue(dos.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }


    /**
     * Test for <code>setMessageDigest()</code> method<br>
     * 
     * Assertion: set associated message digest<br>
     */
    public final void testSetMessageDigest() {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                DigestOutputStream dos = new DigestOutputStream(null, null);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                dos.setMessageDigest(md);
                
                assertTrue(dos.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>on()</code> method<br>
     * Assertion: turns digest functionality on or off
     */
    public final void testOn() throws IOException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                
                // turn digest off
                dos.on(false);
                
                for (int i=0; i<MY_MESSAGE_LEN-1; i++) {
                    dos.write(myMessage[i]);
                }
                
                // turn digest on
                dos.on(true);
                
                // read remaining byte
                dos.write(myMessage[MY_MESSAGE_LEN-1]);
                
                byte[] digest = dos.getMessageDigest().digest();
                
                // check that digest value has been
                // updated by the last write(int) call
                assertFalse(
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k])) ||
                        Arrays.equals(digest,MDGoldenData.getDigest(algorithmName[k]+"_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: returns <code>String</code> representation of this object
     */
    public final void testToString() throws NoSuchAlgorithmException {
        for (int k=0; k<algorithmName.length; k++) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(MY_MESSAGE_LEN);
                MessageDigest md = MessageDigest.getInstance(algorithmName[k]);
                DigestOutputStream dos = new DigestOutputStream(bos, md);
                String rep = dos.toString();
                logln(getName() + ": " + rep);
                assertTrue(rep != null);
                return;
            } catch (NoSuchAlgorithmException e) {
                // allowed failure
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

}
