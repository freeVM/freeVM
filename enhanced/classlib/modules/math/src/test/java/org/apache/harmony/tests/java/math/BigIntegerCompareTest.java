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
 * @author Elena Semukhina
 * @version $Revision: 1.6.6.2 $
 */

package org.apache.harmony.tests.java.math;

import junit.framework.TestCase;
import java.math.BigInteger;

/**
 * Class:   java.math.BigInteger
 * Methods: abs, compareTo, equals, max, min, negate, signum
 */
public class BigIntegerCompareTest extends TestCase {
    /**
     * abs() for a positive number
     */
    public void testAbsPositive() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.abs();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * abs() for a negative number
     */
    public void testAbsNegative() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = -1;
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.abs();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare two positive numbers.
     * The first is greater.
     */
    public void testCompareToPosPos1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 1);
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two positive numbers.
     * The first is less.
     */
    public void testCompareToPosPos2() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        int result = aNumber.compareTo(bNumber);
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare two equal positive numbers.
     */
    public void testCompareToEqualPos() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 0);
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two negative numbers.
     * The first is greater in absolute value.
     */
    public void testCompareToNegNeg1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two negative numbers.
     * The first is less  in absolute value.
     */
    public void testCompareNegNeg2() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare two equal negative numbers.
     */
    public void testCompareToEqualNeg() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 0);
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two numbers of different signs.
     * The first is positive.
     */
    public void testCompareToDiffSigns1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare two numbers of different signs.
     * The first is negative.
     */
    public void testCompareToDiffSigns2() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare a positive number to ZERO.
     */
    public void testCompareToPosZero() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        int result = aNumber.compareTo(bNumber);
        assertTrue(aNumber.compareTo(bNumber) == 1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to a positive number.
     */
    public void testCompareToZeroPos() {
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int bSign = 1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare a negative number to ZERO.
     */
    public void testCompareToNegZero() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to a negative number.
     */
    public void testCompareToZeroNeg() {
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int bSign = -1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == 1);
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to ZERO.
     */
    public void testCompareToZeroZero() {
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = BigInteger.ZERO;
        assertTrue(aNumber.compareTo(bNumber) == 0);
    }

    /**
     * compareTo(Object obj).
     * Verify throwing an exception if obj is not a BigInteger
     */
    public void testCompareToObjectException() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object obj = new Object();
        try {
            int result = aNumber.compareTo(obj);
            fail("ClassCastException has not been caught");
        } catch (ClassCastException e) {
            assertTrue("Improper exception class", e.getClass().getName().equals("java.lang.ClassCastException"));
        }
    }
    
    /**
     * compareTo(Object obj).
     * if obj is not a BigInteger, the exception should be thrown
     */
    public void testCompareToObject() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object hello = (Object)new String("Hello, World");
        try {
            aNumber.compareTo(hello);
            fail("ClassCastException has not been caught");
        } catch (ClassCastException e) {
        }
    }

    /**
     * compareTo(Object obj).
     * obj should be a BigInteger
     */
    public void testCompareToObjectBI() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object bNumber = (Object)new BigInteger(bSign, bBytes);
        assertTrue(aNumber.compareTo(bNumber) == -1);
    }

    /**
     * equals(Object obj).
     * obj is not a BigInteger
     */
    public void testEqualsObject() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object obj = new Object();
        assertFalse(aNumber.equals(obj));
    }

    /**
     * equals(null).
     */
    public void testEqualsNull() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertFalse(aNumber.equals(null));
    }

    /**
     * equals(Object obj).
     * obj is a BigInteger.
     * numbers are equal.
     */
    public void testEqualsBigIntegerTrue() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object bNumber = (Object)new BigInteger(bSign, bBytes);
        assertTrue(aNumber.equals(bNumber));
    }

    /**
     * equals(Object obj).
     * obj is a BigInteger.
     * numbers are not equal.
     */
    public void testEqualsBigIntegerFalse() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object bNumber = (Object)new BigInteger(bSign, bBytes);
        assertFalse(aNumber.equals(bNumber));
    }

    /**
     * max(BigInteger val).
     * the first is greater.
     */
    public void testMaxGreater() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * the first is less.
     */
    public void testMaxLess() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * numbers are equal.
     */
    public void testMaxEqual() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * max of negative and ZERO.
     */
    public void testMaxNegZero() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 0);
    }

    /**
     * min(BigInteger val).
     * the first is greater.
     */
    public void testMinGreater() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * min(BigInteger val).
     * the first is less.
     */
    public void testMinLess() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * min(BigInteger val).
     * numbers are equal.
     */
    public void testMinEqual() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * min of positive and ZERO.
     */
    public void testMinPosZero() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 0);
    }
    
    /**
     * negate() a positive number.
     */
    public void testNegatePositive() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        byte rBytes[] = {-13, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -27, -4, -91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == -1);
    }

    /**
     * negate() a negative number.
     */
    public void testNegateNegative() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * negate() ZERO.
     */
    public void testNegateZero() {
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 0);
    }

    /**
     * signum() of a positive number.
     */
    public void testSignumPositive() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue("incorrect sign", aNumber.signum() == 1);
    }
    
    /**
     * signum() of a negative number.
     */
    public void testSignumNegative() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertTrue("incorrect sign", aNumber.signum() == -1);
    }
    
    /**
     * signum() of ZERO.
     */
    public void testSignumZero() {
        BigInteger aNumber = BigInteger.ZERO;
        assertTrue("incorrect sign", aNumber.signum() == 0);
    }
}
