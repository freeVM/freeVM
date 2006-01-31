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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.crypto;

import java.security.spec.AlgorithmParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.AlgorithmParameters;
import java.nio.ByteBuffer;

import junit.framework.TestCase;


/**
 * Tests for <code>CipherSpi</code> class constructors and methods.
 * 
 */

public class CipherSpiTests extends TestCase {

    /**
     * Constructor for CipherSpiTests.
     * 
     * @param arg0
     */
    public CipherSpiTests(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>CipherSpi</code> constructor 
     * Assertion: constructs CipherSpi
     */
    public void testCipherSpiTests01() throws IllegalBlockSizeException,
            BadPaddingException, ShortBufferException {
        
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue("Not cipherSpi object", cSpi instanceof CipherSpi);
        assertEquals("BlockSize is not 0", cSpi.engineGetBlockSize(), 0);
        assertEquals("OutputSize is not 0", cSpi.engineGetOutputSize(1), 0);
        byte[] bb = cSpi.engineGetIV();
        assertEquals("Length of result byte array is not 0", bb.length, 0);
        assertNull("Not null result", cSpi.engineGetParameters());
        byte[] bb1 = new byte[10];
        byte[] bb2 = new byte[10];
        bb = cSpi.engineUpdate(bb1, 1, 2);
        assertEquals("Incorrect result of engineUpdate(byte, int, int)",
                bb.length, 2);
        bb = cSpi.engineDoFinal(bb1, 1, 2);
        assertEquals("Incorrect result of enfineDoFinal(byte, int, int)", 2,
                bb.length);
        assertEquals(
                "Incorrect result of enfineUpdate(byte, int, int, byte, int)",
                cSpi.engineUpdate(bb1, 1, 2, bb2, 7), 2);
        assertEquals(
                "Incorrect result of enfineDoFinal(byte, int, int, byte, int)",
                2, cSpi.engineDoFinal(bb1, 1, 2, bb2, 0));
    }
    
    /**
     * Test for <code>engineGetKeySize(Key)</code> method 
     * Assertion: It throws UnsupportedOperationException if it is not overriden
     */
    public void testCipherSpi02() {
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue(cSpi instanceof CipherSpi);
        try {
            cSpi.engineGetKeySize(null);
            assertTrue("UnsupportedOperationException must be thrown", false);
        } catch (UnsupportedOperationException e) {
        } catch (Exception e) {
            assertTrue(
                    "Unexpected ".concat(e.toString()).concat(" was thrown"),
                    false);
        }
    }

    /**
     * Test for <code>engineWrap(Key)</code> method 
     * Assertion: It throws UnsupportedOperationException if it is not overriden
     */
    public void testCipherSpi03() {
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue(cSpi instanceof CipherSpi);
        try {
            cSpi.engineWrap(null);
            assertTrue("UnsupportedOperationException must be thrown", false);
        } catch (UnsupportedOperationException e) {
        } catch (Exception e) {
            assertTrue(
                    "Unexpected ".concat(e.toString()).concat(" was thrown"),
                    false);
        }
    }

    /**
     * Test for <code>engineUnwrap(byte[], String, int)</code> method
     * Assertion: It throws UnsupportedOperationException if it is not overriden
     */
    public void testCipherSpi04() {
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue(cSpi instanceof CipherSpi);
        try {
            cSpi.engineUnwrap(new byte[0], "", 0);
            assertTrue("UnsupportedOperationException must be thrown", false);
        } catch (UnsupportedOperationException e) {
        } catch (Exception e) {
            assertTrue(
                    "Unexpected ".concat(e.toString()).concat(" was thrown"),
                    false);
        }
    }
    
    /**
     * Test for <code>engineUpdate(ByteBuffer, ByteBuffer)</code> method
     * Assertions:
     * throws NullPointerException if one of these buffers is null;
     * throws ShortBufferException is there is no space in output to hold result
     */
    public void testCipherSpi05() throws ShortBufferException {
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue(cSpi instanceof CipherSpi);
        byte[] bb = { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4,
                (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10 };
        int pos = 5;
        int len = bb.length;
        ByteBuffer bbNull = null;
        ByteBuffer bb1 = ByteBuffer.allocate(len);
        bb1.put(bb);
        bb1.position(0);
        try {
            cSpi.engineUpdate(bbNull, bb1);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            cSpi.engineUpdate(bb1, bbNull);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        ByteBuffer bb2 = ByteBuffer.allocate(bb.length);
        bb1.position(len);
        assertEquals("Incorrect number of stored bytes", 0, cSpi.engineUpdate(
                bb1, bb2));

        bb1.position(0);
        bb2.position(len - 2);
        try {
            cSpi.engineUpdate(bb1, bb2);
            fail("ShortBufferException bust be thrown. Output buffer remaining: "
                    .concat(Integer.toString(bb2.remaining())));
        } catch (ShortBufferException e) {
        }
        bb1.position(10);
        bb2.position(0);
        assertTrue("Incorrect number of stored bytes", cSpi.engineUpdate(bb1,
                bb2) > 0);
        bb1.position(bb.length);
        cSpi.engineUpdate(bb1, bb2);

        bb1.position(pos);
        bb2.position(0);
        int res = cSpi.engineUpdate(bb1, bb2);
        assertTrue("Incorrect result", res > 0);
    }

    /**
     * Test for <code>engineDoFinal(ByteBuffer, ByteBuffer)</code> method
     * Assertions: 
     * throws NullPointerException if one of these buffers is null;
     * throws ShortBufferException is there is no space in output to hold result
     */
    public void testCipherSpi06() throws BadPaddingException,
            ShortBufferException, IllegalBlockSizeException {
        CipherSpi cSpi = (CipherSpi) new myCipherSpi();
        assertTrue(cSpi instanceof CipherSpi);
        int len = 10;
        byte[] bbuf = new byte[len];
        for (int i = 0; i < bbuf.length; i++) {
            bbuf[i] = (byte) i;
        }
        ByteBuffer bb1 = ByteBuffer.wrap(bbuf);
        ByteBuffer bbNull = null;
        try {
            cSpi.engineDoFinal(bbNull, bb1);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            cSpi.engineDoFinal(bb1, bbNull);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        ByteBuffer bb2 = ByteBuffer.allocate(len);
        bb1.position(bb1.limit());
        assertEquals("Incorrect result", 0, cSpi.engineDoFinal(bb1, bb2));

        bb1.position(0);
        bb2.position(len - 2);
        try {
            cSpi.engineDoFinal(bb1, bb2);
            fail("ShortBufferException must be thrown. Output buffer remaining: "
                    .concat(Integer.toString(bb2.remaining())));
        } catch (ShortBufferException e) {
        }
        int pos = 5;
        bb1.position(pos);
        bb2.position(0);
        assertTrue("Incorrect result", cSpi.engineDoFinal(bb1, bb2) > 0);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(CipherSpiTests.class);
    }
}
/**
 * 
 * Additional class for CipherGeneratorSpi constructor verification
 */

class myCipherSpi extends CipherSpi {
    private String cipherMode;

    private byte[] initV;

    private static byte[] resV = { (byte) 7, (byte) 6, (byte) 5, (byte) 4,
            (byte) 3, (byte) 2, (byte) 1, (byte) 0 };

    public myCipherSpi() {
        this.initV = new byte[0];
    }

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        this.cipherMode = mode;
    }

    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
    }

    protected int engineGetBlockSize() {
        return 0;
    }

    protected int engineGetOutputSize(int inputLen) {
        return 0;
    }

    protected byte[] engineGetIV() {
        return new byte[0];
    }

    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
    }

    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (initV.length < inputLen) {
            initV = new byte[inputLen];
        }
        for (int i = 0; i < inputLen; i++) {
            initV[i] = input[inputOffset + i];
        }
        return initV;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException {
        byte []res = engineUpdate(input, inputOffset, inputLen);
        int t = res.length;
        if ((output.length - outputOffset) < t) {
            throw new ShortBufferException("Update");
        }
        for (int i = 0; i < t; i++) {
            output[i + outputOffset] = initV[i];
        }
        return t;
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        if (resV.length > inputLen) {
            byte[] bb = new byte[inputLen];
            for (int i = 0; i < inputLen; i++) {
                bb[i] = resV[i];
            }
            return bb;
        }
        return resV;
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] res = engineDoFinal(input, inputOffset, inputLen);
        
        int t = res.length;
        if ((output.length - outputOffset) < t) {
            throw new ShortBufferException("DoFinal");
        }
        for (int i = 0; i < t; i++) {            
            output[i + outputOffset] = res[i];
        }
        return t;
    }

    
    protected int engineUpdate(ByteBuffer input, ByteBuffer output)
    throws ShortBufferException {
        return super.engineUpdate(input, output);
    }
    protected int engineDoFinal(ByteBuffer input, ByteBuffer output)
    throws ShortBufferException, IllegalBlockSizeException,
    BadPaddingException {
        return super.engineDoFinal(input, output);
    }
}
