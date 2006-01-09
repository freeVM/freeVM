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

package java.security.spec;

import java.math.BigInteger;
import java.util.Arrays;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>ECFieldF2m</code> class fields and methods.
 * 
 */
public class ECFieldF2mTest extends PerformanceTest {

    /**
     * <code>PASSED</code> - test status 'passed'
     */
    private static final long PASSED = 0L;
    /**
     * <code>FAILED</code> - test status 'failed'
     */
    private static final long FAILED = 1L;

    /**
     * Support class for this test.
     * Incapsulates <code>ECFieldF2m</code> testing
     * domain parameters. 
     * 
     */
    private static final class ECFieldF2mDomainParams {

        /**
         * <code>NPE</code> reference object of class NullPointerException.
         * NullPointerException must be thrown by <code>ECFieldF2m</code>
         * ctors in some circumstances
         */
        static final NullPointerException NPE = new NullPointerException();
        /**
         * <code>IArgE</code> reference object of class IllegalArgumentException.
         * IllegalArgumentException must be thrown by <code>ECFieldF2m</code>
         * ctors in some circumstances
         */
        static final IllegalArgumentException IArgE = new IllegalArgumentException();
        
        /**
         * The <code>m</code> parameter for <code>ECFieldF2m</code>
         * ctor for the current test.
         */
        final int m;
        /**
         * The <code>rp</code> parameter for <code>ECFieldF2m</code>
         * ctor for the current test.
         */
        final BigInteger rp;
        /**
         * The <code>ks</code> parameter for <code>ECFieldF2m</code>
         * ctor for the current test.
         */
        final int[] ks;
        
        
        /**
         * Exception expected with this parameters set or <code>null</code>
         * if no exception expected.
         */
        final Exception x;
        
        /**
         * Constructs ECFieldF2mDomainParams
         * 
         * @param m
         * @param rp
         * @param ks
         * @param expectedException
         */
        ECFieldF2mDomainParams(final int m,
                final BigInteger rp,
                final int[] ks,
                final Exception expectedException) {
            this.m = m;
            this.rp = rp;
            this.ks = ks;
            this.x = expectedException;
        }
    }

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
     * Constructor for ECFieldF2mTest.
     * @param name
     */
    public ECFieldF2mTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Set of parameters used for <code>ECFieldF2m(int)</code>
     * constructor tests.
     */
    private final ECFieldF2mDomainParams[] intCtorTestParameters =
        new ECFieldF2mDomainParams[] {
            // set 0: valid m
            new ECFieldF2mDomainParams(1, null, null, null),
            // set 1: valid m
            new ECFieldF2mDomainParams(Integer.MAX_VALUE, null, null, null),
            // set 2: invalid m
            new ECFieldF2mDomainParams(0, null, null, ECFieldF2mDomainParams.IArgE),
            // set 3: invalid m
            new ECFieldF2mDomainParams(-1, null, null, ECFieldF2mDomainParams.IArgE)
        };

    /**
     * Tests for constructor <code>ECFieldF2m(int)</code><br>
     * 
     * Assertion: constructs new <code>ECFieldF2m</code> object
     * using valid parameter m.
     * 
     * Assertion: IllegalArgumentException if m is not positive.
     */
    public final void testECFieldF2mint() {
        long result = PASSED;
        for(int i=0; i<intCtorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = intCtorTestParameters[i];
            try {
                // perform test
                ECField f = new ECFieldF2m(tp.m);
                
                if (tp.x == null) {
                    // passed
                } else {
                    // exception has been expected 
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x != null && e.getClass().isInstance(tp.x)) {
                    // passed
                    logln(getName() + ", set " + i + ": " + e);
                } else {
                    // exception: failure
                    // if it has not been expected
                    // or wrong one has been thrown
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
        assertTrue(result == PASSED);
    }

    /**
     * Set of parameters used for <code>ECFieldF2m(int, BigInteger)</code>
     * constructor tests.
     */
    private final ECFieldF2mDomainParams[] intBigIntegerCtorTestParameters =
        new ECFieldF2mDomainParams[] {
            // set 0: valid m and rp - trinomial basis params
            new ECFieldF2mDomainParams(
                    1999,
                    BigInteger.valueOf(0L).setBit(0).setBit(367).setBit(1999),
                    null,
                    null),
            // set 1: valid m and rp - pentanomial basis params
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).setBit(981).setBit(2000),
                    null,
                    null),
            // set 2: valid m, invalid (null) rp
            new ECFieldF2mDomainParams(
                    1963,
                    (BigInteger)null,
                    null,
                    ECFieldF2mDomainParams.NPE),
            // set 3: valid m, invalid rp - bit 0 not set
            new ECFieldF2mDomainParams(
                    1999,
                    BigInteger.valueOf(0L).setBit(1).setBit(367).setBit(1999),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 4: valid m, invalid rp - bit m not set
            new ECFieldF2mDomainParams(
                    1999,
                    BigInteger.valueOf(0L).setBit(0).setBit(367).setBit(1998),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 5: valid m, invalid rp - bit k improperly set
            new ECFieldF2mDomainParams(
                    1999,
                    BigInteger.valueOf(0L).setBit(0).setBit(2367).setBit(1999),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 6: valid m, invalid rp - k1 k2 k3
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(2001).setBit(2002).setBit(2003).setBit(2000),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 7: valid m, invalid rp - number of bits set
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(2000),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 8: valid m, invalid rp - number of bits set
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).setBit(2000),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 9: valid m, invalid rp - number of bits set
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).
                                           setBit(981).setBit(985).setBit(2000),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 10: valid m, invalid rp
            new ECFieldF2mDomainParams(
                    2000,
                    BigInteger.valueOf(0L),
                    null,
                    ECFieldF2mDomainParams.IArgE),
            // set 11: invalid m
            new ECFieldF2mDomainParams(
                    -2000,
                    BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).
                    setBit(981).setBit(2000),
                    null,
                    ECFieldF2mDomainParams.IArgE),
        };  

    /**
     * Tests for constructor <code>ECFieldF2m(int, BigInteger)</code><br>
     * 
     * Assertion: constructs new <code>ECFieldF2m</code> object
     * using valid parameters m and rp. rp represents trinomial basis.
     * 
     * Assertion: constructs new <code>ECFieldF2m</code> object
     * using valid parameters m and rp. rp represents pentanomial basis.
     * 
     * Assertion: IllegalArgumentException if m is not positive.
     * 
     * Assertion: NullPointerException if rp is null.
     * 
     * Assertion: IllegalArgumentException if rp is invalid.
     */
    public final void testECFieldF2mintBigInteger() {
        long result = PASSED;
        for(int i=0; i<intBigIntegerCtorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = intBigIntegerCtorTestParameters[i];
            try {
                // perform test
                ECField f = new ECFieldF2m(tp.m, tp.rp);
                
                if (tp.x == null) {
                    // passed
                } else {
                    // exception has been expected 
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x != null && e.getClass().isInstance(tp.x)) {
                    // passed
                    logln(getName() + ", set " + i + ": " + e);
                } else {
                    // exception: failure
                    // if it has not been expected
                    // or wrong one has been thrown
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
        assertTrue(result == PASSED);
    }

    /**
     * Set of parameters used for <code>ECFieldF2m(int, BigInteger)</code>
     * constructor tests.
     */
    private final ECFieldF2mDomainParams[] intIntArrayCtorTestParameters =
        new ECFieldF2mDomainParams[] {
            // set 0: valid m and ks - trinomial basis params
            new ECFieldF2mDomainParams(
                    1999,
                    null,
                    new int[] {367},
                    null),
            // set 1: valid m and ks - pentanomial basis params
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[] {981,2,1},
                    null),
            // set 2: valid m, invalid (null) ks
            new ECFieldF2mDomainParams(
                    1963,
                    null,
                    null,
                    ECFieldF2mDomainParams.NPE),
            // set 3: valid m, invalid ks - wrong length
            new ECFieldF2mDomainParams(
                    1963,
                    null,
                    new int[] {981,2},
                    ECFieldF2mDomainParams.IArgE),
            // set 4: valid m, invalid ks - wrong length
            new ECFieldF2mDomainParams(
                    1963,
                    null,
                    new int[] {981,124,2,1},
                    ECFieldF2mDomainParams.IArgE),
            // set 5: valid m, invalid ks - wrong value
            new ECFieldF2mDomainParams(
                    1999,
                    null,
                    new int[] {1999},
                    ECFieldF2mDomainParams.IArgE),
            // set 6: valid m, invalid ks - wrong value
            new ECFieldF2mDomainParams(
                    1999,
                    null,
                    new int[] {0},
                    ECFieldF2mDomainParams.IArgE),
            // set 7: valid m, invalid ks - wrong values
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[] {2000,2,1},
                    ECFieldF2mDomainParams.IArgE),
            // set 8: valid m, invalid ks - wrong values
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[] {981,2,0},
                    ECFieldF2mDomainParams.IArgE),
            // set 9: invalid m
            new ECFieldF2mDomainParams(
                    -5,
                    null,
                    new int[] {981,2,1},
                    ECFieldF2mDomainParams.IArgE),
            // set 10: valid m, invalid ks - wrong order
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[] {981,1,2},
                    ECFieldF2mDomainParams.IArgE),
            // set 11: valid m, invalid ks - no content
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[3],
                    ECFieldF2mDomainParams.IArgE),
            // set 12: valid m, invalid ks - length is 0
            new ECFieldF2mDomainParams(
                    2000,
                    null,
                    new int[0],
                    ECFieldF2mDomainParams.IArgE),
        };

    /**
     * Tests for constructor <code>ECFieldF2m(int m, int[] ks)</code><br>
     * 
     * Assertion: constructs new <code>ECFieldF2m</code> object
     * using valid parameters m and rp. ks represents trinomial basis.
     * 
     * Assertion: constructs new <code>ECFieldF2m</code> object
     * using valid parameters m and ks. ks represents pentanomial basis.
     * 
     * Assertion: IllegalArgumentException if m is not positive.
     * 
     * Assertion: NullPointerException if ks is null.
     * 
     * Assertion: IllegalArgumentException if ks is invalid.
     */
    public final void testECFieldF2mintintArray() {
        long result = PASSED;
        for(int i=0; i<intIntArrayCtorTestParameters.length; i++) {
            ECFieldF2mDomainParams tp = intIntArrayCtorTestParameters[i];
            try {
                // perform test
                ECField f = new ECFieldF2m(tp.m, tp.ks);
                
                if (tp.x == null) {
                    // passed
                } else {
                    // exception has been expected 
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: expected exception has not been thrown");
                }
            } catch (Exception e){
                if (tp.x != null && e.getClass().isInstance(tp.x)) {
                    // passed
                    logln(getName() + ", set " + i + ": " + e);
                } else {
                    // exception: failure
                    // if it has not been expected
                    // or wrong one has been thrown
                    result |= (FAILED << i);
                    loglnError(getName() + ", set " + i +
                            " FAILED: unexpected " + e);
                }
            }
        }
        assertTrue(result == PASSED);
    }

    /**
     * Test #1 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * repeatedly on the same object. 
     */
    public final void testHashCode01() {
        ECFieldF2m f = new ECFieldF2m(2000);
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
    }

    /**
     * Test #2 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * repeatedly on the same object. 
     */
    public final void testHashCode02() {
        ECFieldF2m f = new ECFieldF2m(2000, new int[] {981, 2, 1});
        int hc = f.hashCode();
        assertTrue(hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode() &&
                   hc == f.hashCode());
    }

    /**
     * Test #3 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * on equal (according to the <code>equals(Object)</code> method) objects. 
     */
    public final void testHashCode03() {
        assertTrue(new ECFieldF2m(111).hashCode() ==
                   new ECFieldF2m(111).hashCode());
    }

    /**
     * Test #4 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * on equal (according to the <code>equals(Object)</code> method) objects. 
     */
    public final void testHashCode04() {
        assertTrue(new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode() ==
                   new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode());
    }

    /**
     * Test #5 for <code>hashCode()</code> method.<br>
     *
     * Assertion: must return the same value if invoked
     * on equal (according to the <code>equals(Object)</code> method) objects. 
     */
    public final void testHashCode05() {
        assertTrue(new ECFieldF2m(2000, new int[] {981, 2, 1}).hashCode() ==
                   new ECFieldF2m(2000, BigInteger.valueOf(0L).
                                        setBit(0).setBit(1).setBit(2).
                                        setBit(981).setBit(2000)).hashCode());
    }

    /**
     * Test #1 for <code>equals()</code> method.<br>
     *
     * Assertion: object equals to itself. 
     */
    public final void testEqualsObject01() {
        ECFieldF2m obj = new ECFieldF2m(1999, new int[] {367});
        assertTrue(obj.equals(obj));
    }

    /**
     * Test #2 for <code>equals()</code> method.<br>
     *
     * Assertion: normal basis - objects equal if their m are equal. 
     */
    public final void testEqualsObject02() {
        assertTrue(new ECFieldF2m(43).equals(new ECFieldF2m(43)));
    }

    /**
     * Test #3 for <code>equals()</code> method.<br>
     *
     * Assertion: trinomial basis - objects equal if their m, and rp
     * are mutually equal. 
     */
    public final void testEqualsObject03() {
        assertTrue(new ECFieldF2m(1999, new int[] {367}).equals(
                   new ECFieldF2m(1999, BigInteger.valueOf(0L).
                                        setBit(0).setBit(367).setBit(1999))));
    }

    /**
     * Test #4 for <code>equals()</code> method.<br>
     *
     * Assertion: pentanomial basis - objects equal if their m, and rp
     * are mutually equal. 
     */
    public final void testEqualsObject04() {
        ECFieldF2m f1 = new ECFieldF2m(2000, new int[] {981, 2, 1});
        ECFieldF2m f2 = new ECFieldF2m(2000, BigInteger.valueOf(0L).
                setBit(0).setBit(1).setBit(2).
                setBit(981).setBit(2000)); 
        assertTrue(f1.equals(f2) && f2.equals(f1));
    }

    /**
     * Test #5 for <code>equals()</code> method.<br>
     *
     * Assertion: objects equal if their m, and rp are mutually equal. 
     */
    public final void testEqualsObject05() {
        ECFieldF2m f1 = new ECFieldF2m(2000);
        ECFieldF2m f2 = new ECFieldF2m(2000, BigInteger.valueOf(0L).
                setBit(0).setBit(1).setBit(2).
                setBit(981).setBit(2000)); 
        assertFalse(f1.equals(f2) || f2.equals(f1));
    }

    /**
     * Test #6 for <code>equals(Object obj)</code> method.<br>
     *
     * Assertion: returns false if obj is <code>null</code>
     */
    public final void testEqualsObject06() {
        assertFalse(new ECFieldF2m(2000).equals(null));
    }

    /**
     * Test #7 for <code>equals(Object obj)</code> method.<br>
     *
     * Assertion: returns false if obj is not instance of <code>ECFieldF2m</code>
     */
    public final void testEqualsObject07() {
        assertFalse(new ECFieldF2m(2000).equals(new Object()));
    }

    /**
     * Test for <code>getFieldSize()</code> method.<br>
     *
     * Assertion: returns m value for <code>ECFieldF2m</code>
     */
    public final void testGetFieldSize() {
        assertTrue(new ECFieldF2m(2000).getFieldSize() == 2000);
    }

    /**
     * Test for <code>getM()</code> method.<br>
     *
     * Assertion: returns m value for <code>ECFieldF2m</code>
     */
    public final void testGetM() {
        assertTrue(new ECFieldF2m(2000).getM() == 2000);
    }

    /**
     * Test #1 for <code>getMidTermsOfReductionPolynomial()</code> method.<br>
     *
     * Assertion: returns mid terms of reduction polinomial
     */
    public final void testGetMidTermsOfReductionPolynomial01() {
        int[] a = new int[] {981,2,1};
        int[] b = new ECFieldF2m(2000,
                BigInteger.valueOf(0L).setBit(0).setBit(1).
                setBit(2).setBit(981).setBit(2000)).
                getMidTermsOfReductionPolynomial();
        assertTrue(Arrays.equals(a, b));
    }

    /**
     * Test #2 for <code>getMidTermsOfReductionPolynomial()</code> method.<br>
     *
     * Assertion: returns null for normal basis
     */
    public final void testGetMidTermsOfReductionPolynomial02() {
        assertTrue(new ECFieldF2m(2000).getMidTermsOfReductionPolynomial() == null);
    }

    /**
     * Test #3 for <code>getMidTermsOfReductionPolynomial()</code> method.<br>
     *
     * Assertion: returns mid terms of reduction polinomial
     */
    public final void testGetMidTermsOfReductionPolynomial03() {
        int[] a = new int[] {367};
        int[] b = new ECFieldF2m(1999, a).getMidTermsOfReductionPolynomial();
        assertTrue(Arrays.equals(a, b));
    }

    /**
     * Test #1 for <code>getReductionPolynomial()</code> method.<br>
     *
     * Assertion: returns reduction polinomial
     */
    public final void testGetReductionPolynomial01() {
        BigInteger rp = BigInteger.valueOf(0L).setBit(0).setBit(1).setBit(2).
        setBit(981).setBit(2000);
        assertTrue(new ECFieldF2m(2000, rp).getReductionPolynomial().equals(rp));
    }

    /**
     * Test #2 for <code>getReductionPolynomial()</code> method.<br>
     *
     * Assertion: returns null for normal basis
     */
    public final void testGetReductionPolynomial02() {
        assertTrue(new ECFieldF2m(2000).getReductionPolynomial() == null);
    }

    /**
     * Tests that object state is preserved against modifications
     * through array reference passed to the constructor.
     */
    public final void testIsStatePreserved01() {
        // reference array
        int[] a = new int[] {367};
        // reference array copy
        int[] aCopy = (int[])a.clone();
        // create obj using copy
        ECFieldF2m f = new ECFieldF2m(1999, aCopy);
        // modify copy
        aCopy[0] = 5;
        // compare reference with returned array
        assertTrue(Arrays.equals(a, f.getMidTermsOfReductionPolynomial()));        
    }

    /**
     * Tests that object state is preserved against
     * modifications through array reference returned by
     * <code>getMidTermsOfReductionPolynomial()</code> method.
     */
    public final void testIsStatePreserved02() {
        // reference array
        int[] a = new int[] {981,2,1};
        // reference array copy
        int[] aCopy = (int[])a.clone();
        // create obj using copy
        ECFieldF2m f = new ECFieldF2m(2000, aCopy);
        // get array reference and modify returned array
        f.getMidTermsOfReductionPolynomial()[0] = 1532;
        // compare reference with returned for the second time array
        assertTrue(Arrays.equals(a, f.getMidTermsOfReductionPolynomial()));        
    }

}
