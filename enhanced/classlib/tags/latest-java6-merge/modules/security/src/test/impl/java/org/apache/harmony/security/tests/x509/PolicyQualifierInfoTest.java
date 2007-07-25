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

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.x509;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Test for thread safety of the PolicyQualifierInfo DER decoder
 * ("DER" stands for "Distinguished Encoding Rules",
 *  see ITU-T Recommendation X.690,
 *  http://asn1.elibel.tm.fr)
 */
public class PolicyQualifierInfoTest extends TestCase {
    // Number of test working threads (may be set externally)
    private static int workersNumber;
    // Number of test iterations performed by each thread
    // (may be set externally)
    private static int iterationsNumber;
    
    static {
        try {
            workersNumber = Integer.parseInt(
                  System.getProperty("PolicyQualifierInfoTest.workersNumber",
                    "10"));
            iterationsNumber = Integer.parseInt(
                  System.getProperty("PolicyQualifierInfoTest.iterationsNumber",
                    "10000"));
        } catch (Exception e) {
            workersNumber = 10;
            iterationsNumber = 10000;
        }
    }
    
    // Holder for thread-specific PolicyQualifier DER encodings
    private static final byte[][] enc = new byte [workersNumber][];

    private volatile boolean arrayPassed = true;
    private volatile boolean inpstPassed = true;

    // "Valid" reference DER encoding
    // (generated by own encoder during test development)
    private static final byte[] encoding = {
            (byte)0x30, (byte)0x26, // tag Seq, length
            (byte)0x06, (byte)0x08, // tag OID, length
              (byte)0x2b, (byte)0x06, (byte)0x01, (byte)0x05, // oid value 
              (byte)0x05, (byte)0x07, (byte)0x02, (byte)0x01, // oid value
            (byte)0x16, (byte)0x1a, // tag IA5String, length
              (byte)0x68, (byte)0x74, (byte)0x74, (byte)0x70,  // IA5String value
              (byte)0x3a, (byte)0x2f, (byte)0x2f, (byte)0x77,  // IA5String value
              (byte)0x77, (byte)0x77, (byte)0x2e, (byte)0x71,  // IA5String value
              (byte)0x71, (byte)0x2e, (byte)0x63, (byte)0x6f,  // IA5String value
              (byte)0x6d, (byte)0x2f, (byte)0x73, (byte)0x74,  // IA5String value
              (byte)0x6d, (byte)0x74, (byte)0x2e, (byte)0x74,  // IA5String value
              (byte)0x78, (byte)0x74   // IA5String value
    };


    // Test worker for decoding from byte array
    private class TestWorker1 extends Thread {

        private final int myIntValue;

        public TestWorker1(int num) {
            super("Worker_" + num);
            myIntValue = num;
        }

        public void run() {
            for (int i=0; i<iterationsNumber; i++) {
                try {
                    // Perform DER decoding:
                    Object[] decoded =
                        (Object[])org.apache.harmony.security.x509.
                        PolicyQualifierInfo.ASN1.decode(
                                getDerEncoding(myIntValue));
                    // check OID value
                    assertEquals(this.getName()+"(OID)",
                            myIntValue, ((int[])decoded[0])[8]);
                    // check qualifier
                    assertEquals(this.getName()+"(QA)",
                            (byte)myIntValue, ((byte[])decoded[1])[2]);
                } catch (Throwable e) {
                    System.err.println(e);
                    arrayPassed = false;
                    return;
                }
            }
        }
    }

    // Test worker for decoding from InputStream
    private class TestWorker2 extends Thread {

        private final int myIntValue;

        public TestWorker2(int num) {
            super("Worker_" + num);
            myIntValue = num;
        }

        public void run() {
            for (int i=0; i<iterationsNumber; i++) {
                try {
                    // Perform DER decoding:
                    Object[] decoded =
                        (Object[])org.apache.harmony.security.x509.
                        PolicyQualifierInfo.ASN1.decode(
                                getDerInputStream(myIntValue));
                    // check OID value
                    assertEquals(this.getName()+"(OID)",
                            myIntValue, ((int[])decoded[0])[8]);
                    // check qualifier
                    assertEquals(this.getName()+"(QA)",
                            (byte)myIntValue, ((byte[])decoded[1])[2]);
                } catch (Throwable e) {
                    System.err.println(e);
                    inpstPassed = false;
                    return;
                }
            }
        }
    }

    /**
     * Test 1
     * @throws InterruptedException
     */
    public final void testMtByteArray() throws InterruptedException {
        Thread[] workers = new Thread[workersNumber];
        for(int i=0; i<workersNumber; i++) {
            workers[i] = new TestWorker1(i);
        }
        for(int i=0; i<workersNumber; i++) {
            workers[i].start();
        }
        for(int i=0; i<workersNumber; i++) {
            workers[i].join();
        }
        assertTrue(arrayPassed);
    }

    /**
     * Test 2
     * @throws InterruptedException
     */
    public final void testMtInputStream() throws InterruptedException {
        Thread[] workers = new Thread[workersNumber];
        for(int i=0; i<workersNumber; i++) {
            workers[i] = new TestWorker2(i);
        }
        for(int i=0; i<workersNumber; i++) {
            workers[i].start();
        }
        for(int i=0; i<workersNumber; i++) {
            workers[i].join();
        }
        assertTrue(inpstPassed);
    }

    //
    // Generates unique (based on parameter) DER encoding
    // @param intVal value to be incorporated into the resulting encoding
    // @return PolicyQualifier DER encoding
    //
    private static final byte[] getDerEncoding(int intVal) {
        setEncArray(intVal);
        return enc[intVal];
    }

    //
    // Generates unique (based on parameter) DER encoding
    // @param intVal value to be incorporated into the resulting encoding
    // @return PolicyQualifier DER encoding
    //
    private static final InputStream getDerInputStream(int intVal) {
        setEncArray(intVal);
        return new ByteArrayInputStream(enc[intVal]);
    }

    //
    // Init thread specific data
    // @param intVal worker thread number
    //
    private static void setEncArray(int intVal) {
        if (enc[intVal] == null) {
            // make encoding thread-specific
            byte[] a = encoding.clone();
            a[11] = (byte)intVal;
            a[14] = (byte)intVal;
            enc[intVal] = a;
        }
    }
}
