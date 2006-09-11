/* Copyright 1998, 2006 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.luni.tests.java.lang;

public class MathTest extends junit.framework.TestCase {

	double HYP = Math.sqrt(2.0);

	double OPP = 1.0;

	double ADJ = 1.0;

	/* Required to make previous preprocessor flags work - do not remove */
	int unused = 0;

	/**
	 * @tests java.lang.Math#abs(double)
	 */
	public void test_absD() {
		// Test for method double java.lang.Math.abs(double)

		assertTrue("Incorrect double abs value",
				(Math.abs(-1908.8976) == 1908.8976));
		assertTrue("Incorrect double abs value",
				(Math.abs(1908.8976) == 1908.8976));
	}

	/**
	 * @tests java.lang.Math#abs(float)
	 */
	public void test_absF() {
		// Test for method float java.lang.Math.abs(float)
		assertTrue("Incorrect float abs value",
				(Math.abs(-1908.8976f) == 1908.8976f));
		assertTrue("Incorrect float abs value",
				(Math.abs(1908.8976f) == 1908.8976f));
	}

	/**
	 * @tests java.lang.Math#abs(int)
	 */
	public void test_absI() {
		// Test for method int java.lang.Math.abs(int)
		assertTrue("Incorrect int abs value", (Math.abs(-1908897) == 1908897));
		assertTrue("Incorrect int abs value", (Math.abs(1908897) == 1908897));
	}

	/**
	 * @tests java.lang.Math#abs(long)
	 */
	public void test_absJ() {
		// Test for method long java.lang.Math.abs(long)
		assertTrue("Incorrect long abs value",
				(Math.abs(-19088976000089L) == 19088976000089L));
		assertTrue("Incorrect long abs value",
				(Math.abs(19088976000089L) == 19088976000089L));
	}

	/**
	 * @tests java.lang.Math#acos(double)
	 */
	public void test_acosD() {
		// Test for method double java.lang.Math.acos(double)
		double r = Math.cos(Math.acos(ADJ / HYP));
		long lr = Double.doubleToLongBits(r);
		long t = Double.doubleToLongBits(ADJ / HYP);
		assertTrue("Returned incorrect arc cosine", lr == t || (lr + 1) == t
				|| (lr - 1) == t);
	}

	/**
	 * @tests java.lang.Math#asin(double)
	 */
	public void test_asinD() {
		// Test for method double java.lang.Math.asin(double)
		double r = Math.sin(Math.asin(OPP / HYP));
		long lr = Double.doubleToLongBits(r);
		long t = Double.doubleToLongBits(OPP / HYP);
		assertTrue("Returned incorrect arc sine", lr == t || (lr + 1) == t
				|| (lr - 1) == t);
	}

	/**
	 * @tests java.lang.Math#atan(double)
	 */
	public void test_atanD() {
		// Test for method double java.lang.Math.atan(double)
		double answer = Math.tan(Math.atan(1.0));
		assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
				&& answer >= 9.9999999999999983E-1);
	}

	/**
	 * @tests java.lang.Math#atan2(double, double)
	 */
	public void test_atan2DD() {
		// Test for method double java.lang.Math.atan2(double, double)
		double answer = Math.atan(Math.tan(1.0));
		assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
				&& answer >= 9.9999999999999983E-1);
	}
    
     /**
     * @tests java.lang.Math#cbrt(double)
     */
    @SuppressWarnings("boxing")
    public void test_cbrt_D() {
        //Test for special situations
        assertEquals("Should return Double.NaN", Double.NaN, Math
                .cbrt(Double.NaN));
        assertEquals("Should return Double.POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math
                        .cbrt(Double.POSITIVE_INFINITY));
        assertEquals("Should return Double.NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, Math
                        .cbrt(Double.NEGATIVE_INFINITY));
        assertEquals("Should return 0.0", 0.0, Math.cbrt(0.0));
        assertEquals("Should return +0.0", +0.0, Math.cbrt(+0.0));
        assertEquals("Should return -0.0", -0.0, Math.cbrt(-0.0));

        assertEquals("Should return 3.0", 3.0, Math.cbrt(27.0));
        assertEquals("Should return 23.111993172558684", 23.111993172558684,
                Math.cbrt(12345.6));
        assertEquals("Should return 5.643803094122362E102",
                5.643803094122362E102, Math.cbrt(Double.MAX_VALUE));
        assertEquals("Should return 0.01", 0.01, Math.cbrt(0.000001));

        assertEquals("Should return -3.0", -3.0, Math.cbrt(-27.0));
        assertEquals("Should return -23.111993172558684", -23.111993172558684,
                Math.cbrt(-12345.6));
        assertEquals("Should return 1.7031839360032603E-108",
                1.7031839360032603E-108, Math.cbrt(Double.MIN_VALUE));
        assertEquals("Should return -0.01", -0.01, Math.cbrt(-0.000001));
        
        try{
            Math.cbrt((Double)null);
            fail("Should throw NullPointerException");
        }catch(NullPointerException e){
            //expected
        }
    }

	/**
	 * @tests java.lang.Math#ceil(double)
	 */
	public void test_ceilD() {
		// Test for method double java.lang.Math.ceil(double)
                assertEquals("Incorrect ceiling for double",
                             79, Math.ceil(78.89), 0);
		assertEquals("Incorrect ceiling for double",
                             -78, Math.ceil(-78.89), 0);
	}

	/**
	 * @tests java.lang.Math#cos(double)
	 */
	public void test_cosD() {
		// Test for method double java.lang.Math.cos(double)
		assertEquals("Incorrect answer", 1.0, Math.cos(0));
		assertEquals("Incorrect answer", 0.5403023058681398, Math.cos(1));
	}

    /**
     * @tests java.lang.Math#cosh(double)
     */
    @SuppressWarnings("boxing")
    public void test_cosh_D() {
        // Test for special situations
        assertEquals("Should return NaN", Double.NaN, Math.cosh(Double.NaN));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.POSITIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.NEGATIVE_INFINITY));
        assertEquals("Should return 1.0", 1.0, Math.cosh(+0.0));
        assertEquals("Should return 1.0", 1.0, Math.cosh(-0.0));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(1234.56));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(-1234.56));
        assertEquals("Should return 1.0000000000005", 1.0000000000005, Math
                .cosh(0.000001));
        assertEquals("Should return 1.0000000000005", 1.0000000000005, Math
                .cosh(-0.000001));
        assertEquals("Should return 5.212214351945598", 5.212214351945598, Math
                .cosh(2.33482));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.MAX_VALUE));
        assertEquals("Should return 1.0", 1.0, Math.cosh(Double.MIN_VALUE));
    }
    
	/**
	 * @tests java.lang.Math#exp(double)
	 */
	public void test_expD() {
		// Test for method double java.lang.Math.exp(double)
		assertTrue("Incorrect answer returned for simple power", Math.abs(Math
				.exp(4D)
				- Math.E * Math.E * Math.E * Math.E) < 0.1D);
		assertTrue("Incorrect answer returned for larger power", Math.log(Math
				.abs(Math.exp(5.5D)) - 5.5D) < 10.0D);
	}
    
    /**
     * @tests java.lang.Math#expm1(double)
     */
    @SuppressWarnings("boxing")
    public void test_expm1_D() {
        // Test for special cases
        assertEquals("Should return NaN", Double.NaN, Math.expm1(Double.NaN));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.expm1(Double.POSITIVE_INFINITY));
        assertEquals("Should return -1.0", -1.0, Math
                .expm1(Double.NEGATIVE_INFINITY));
        assertEquals("Should return 0.0", 0.0, Math.expm1(0.0));
        assertEquals("Should return +0.0", +0.0, Math.expm1(+0.0));
        assertEquals("Should return -0.0", -0.0, Math.expm1(-0.0));

        assertEquals("Should return -9.999950000166666E-6",
                -9.999950000166666E-6, Math.expm1(-0.00001));
        assertEquals("Should return 1.0145103074469635E60",
                1.0145103074469635E60, Math.expm1(138.16951162));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math
                        .expm1(123456789123456789123456789.4521584223));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.expm1(Double.MAX_VALUE));
        assertEquals("Should return MIN_VALUE", Double.MIN_VALUE, Math
                .expm1(Double.MIN_VALUE));
    }

    /**
     * @tests java.lang.Math#floor(double)
     */
	public void test_floorD() {
		// Test for method double java.lang.Math.floor(double)
                assertEquals("Incorrect floor for double",
                             78, Math.floor(78.89), 0);
		assertEquals("Incorrect floor for double",
                             -79, Math.floor(-78.89), 0);
	}
    
    /**
     * @tests java.lang.Math#hypot(double, double)
     */
    @SuppressWarnings("boxing")
    public void test_hypot_DD() {
        // Test for special cases
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.POSITIVE_INFINITY,
                        1.0));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.NEGATIVE_INFINITY,
                        123.324));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(-758.2587,
                        Double.POSITIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(5687.21,
                        Double.NEGATIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.POSITIVE_INFINITY,
                        Double.NEGATIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY));
        assertEquals("Should return NaN", Double.NaN, Math.hypot(Double.NaN,
                2342301.89843));
        assertEquals("Should return NaN", Double.NaN, Math.hypot(-345.2680,
                Double.NaN));

        assertEquals("Should return 2396424.905416697", 2396424.905416697, Math
                .hypot(12322.12, -2396393.2258));
        assertEquals("Should return 138.16958070558556", 138.16958070558556,
                Math.hypot(-138.16951162, 0.13817035864));
        assertEquals("Should return 1.7976931348623157E308",
                1.7976931348623157E308, Math.hypot(Double.MAX_VALUE, 211370.35));
        assertEquals("Should return 5413.7185", 5413.7185, Math.hypot(
                -5413.7185, Double.MIN_VALUE));
    }

	/**
	 * @tests java.lang.Math#IEEEremainder(double, double)
	 */
	public void test_IEEEremainderDD() {
		// Test for method double java.lang.Math.IEEEremainder(double, double)
		assertEquals("Incorrect remainder returned",
				0.0, Math.IEEEremainder(1.0, 1.0));
		assertTrue("Incorrect remainder returned", Math.IEEEremainder(1.32,
				89.765) >= 1.4705063220631647E-2
				|| Math.IEEEremainder(1.32, 89.765) >= 1.4705063220631649E-2);
	}

	/**
	 * @tests java.lang.Math#log(double)
	 */
	public void test_logD() {
		// Test for method double java.lang.Math.log(double)
		for (double d = 10; d >= -10; d -= 0.5) {
			double answer = Math.log(Math.exp(d));
			assertTrue("Answer does not equal expected answer for d = " + d
					+ " answer = " + answer, Math.abs(answer - d) <= Math
					.abs(d * 0.00000001));
		}
	}
    
    /**
     * @tests java.lang.Math#log1p(double)
     */
    @SuppressWarnings("boxing")
    public void test_log1p_D() {
        // Test for special cases
        assertEquals("Should return NaN", Double.NaN, Math.log1p(Double.NaN));
        assertEquals("Should return NaN", Double.NaN, Math.log1p(-32.0482175));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.log1p(Double.POSITIVE_INFINITY));
        assertEquals("Should return 0.0", 0.0, Math.log1p(0.0));
        assertEquals("Should return +0.0", +0.0, Math.log1p(+0.0));
        assertEquals("Should return -0.0", -0.0, Math.log1p(-0.0));

        assertEquals("Should return -0.2941782295312541", -0.2941782295312541,
                Math.log1p(-0.254856327));
        assertEquals("Should return 7.368050685564151", 7.368050685564151, Math
                .log1p(1583.542));
        assertEquals("Should return 0.4633708685409921", 0.4633708685409921,
                Math.log1p(0.5894227));
        assertEquals("Should return 709.782712893384", 709.782712893384, Math
                .log1p(Double.MAX_VALUE));
        assertEquals("Should return Double.MIN_VALUE", Double.MIN_VALUE, Math
                .log1p(Double.MIN_VALUE));
    }

	/**
	 * @tests java.lang.Math#max(double, double)
	 */
	public void test_maxDD() {
		// Test for method double java.lang.Math.max(double, double)
		assertEquals("Incorrect double max value", 1908897.6000089, Math.max(-1908897.6000089,
				1908897.6000089));
		assertEquals("Incorrect double max value",
				1908897.6000089, Math.max(2.0, 1908897.6000089));
		assertEquals("Incorrect double max value", -2.0, Math.max(-2.0,
				-1908897.6000089));

	}

	/**
	 * @tests java.lang.Math#max(float, float)
	 */
	public void test_maxFF() {
		// Test for method float java.lang.Math.max(float, float)
		assertTrue("Incorrect float max value", Math.max(-1908897.600f,
				1908897.600f) == 1908897.600f);
		assertTrue("Incorrect float max value",
				Math.max(2.0f, 1908897.600f) == 1908897.600f);
		assertTrue("Incorrect float max value",
				Math.max(-2.0f, -1908897.600f) == -2.0f);
	}

	/**
	 * @tests java.lang.Math#max(int, int)
	 */
	public void test_maxII() {
		// Test for method int java.lang.Math.max(int, int)
		assertEquals("Incorrect int max value",
				19088976, Math.max(-19088976, 19088976));
		assertEquals("Incorrect int max value",
				19088976, Math.max(20, 19088976));
		assertEquals("Incorrect int max value", -20, Math.max(-20, -19088976));
	}

	/**
	 * @tests java.lang.Math#max(long, long)
	 */
	public void test_maxJJ() {
		// Test for method long java.lang.Math.max(long, long)
		assertEquals("Incorrect long max value", 19088976000089L, Math.max(-19088976000089L,
				19088976000089L));
		assertEquals("Incorrect long max value",
				19088976000089L, Math.max(20, 19088976000089L));
		assertEquals("Incorrect long max value",
				-20, Math.max(-20, -19088976000089L));
	}

	/**
	 * @tests java.lang.Math#min(double, double)
	 */
	public void test_minDD() {
		// Test for method double java.lang.Math.min(double, double)
		assertEquals("Incorrect double min value", -1908897.6000089, Math.min(-1908897.6000089,
				1908897.6000089));
		assertEquals("Incorrect double min value",
				2.0, Math.min(2.0, 1908897.6000089));
		assertEquals("Incorrect double min value", -1908897.6000089, Math.min(-2.0,
				-1908897.6000089));
	}

	/**
	 * @tests java.lang.Math#min(float, float)
	 */
	public void test_minFF() {
		// Test for method float java.lang.Math.min(float, float)
		assertTrue("Incorrect float min value", Math.min(-1908897.600f,
				1908897.600f) == -1908897.600f);
		assertTrue("Incorrect float min value",
				Math.min(2.0f, 1908897.600f) == 2.0f);
		assertTrue("Incorrect float min value",
				Math.min(-2.0f, -1908897.600f) == -1908897.600f);
	}

	/**
	 * @tests java.lang.Math#min(int, int)
	 */
	public void test_minII() {
		// Test for method int java.lang.Math.min(int, int)
		assertEquals("Incorrect int min value",
				-19088976, Math.min(-19088976, 19088976));
		assertEquals("Incorrect int min value", 20, Math.min(20, 19088976));
		assertEquals("Incorrect int min value",
				-19088976, Math.min(-20, -19088976));

	}

	/**
	 * @tests java.lang.Math#min(long, long)
	 */
	public void test_minJJ() {
		// Test for method long java.lang.Math.min(long, long)
		assertEquals("Incorrect long min value", -19088976000089L, Math.min(-19088976000089L,
				19088976000089L));
		assertEquals("Incorrect long min value",
				20, Math.min(20, 19088976000089L));
		assertEquals("Incorrect long min value",
				-19088976000089L, Math.min(-20, -19088976000089L));
	}

	/**
	 * @tests java.lang.Math#pow(double, double)
	 */
	public void test_powDD() {
		// Test for method double java.lang.Math.pow(double, double)
		assertTrue("pow returned incorrect value",
				(long) Math.pow(2, 8) == 256l);
		assertTrue("pow returned incorrect value",
				Math.pow(2, -8) == 0.00390625d);
		assertEquals("Incorrect root returned1",
                             2, Math.sqrt(Math.pow(Math.sqrt(2), 4)), 0);
	}

	/**
	 * @tests java.lang.Math#rint(double)
	 */
	public void test_rintD() {
		// Test for method double java.lang.Math.rint(double)
		assertEquals("Failed to round properly - up to odd",
				3.0, Math.rint(2.9));
		assertTrue("Failed to round properly - NaN", Double.isNaN(Math
				.rint(Double.NaN)));
		assertEquals("Failed to round properly down  to even",
				2.0, Math.rint(2.1));
		assertTrue("Failed to round properly " + 2.5 + " to even", Math
				.rint(2.5) == 2.0);
	}

	/**
	 * @tests java.lang.Math#round(double)
	 */
	public void test_roundD() {
		// Test for method long java.lang.Math.round(double)
		assertEquals("Incorrect rounding of a float", -91, Math.round(-90.89d));
	}

	/**
	 * @tests java.lang.Math#round(float)
	 */
	public void test_roundF() {
		// Test for method int java.lang.Math.round(float)
		assertEquals("Incorrect rounding of a float", -91, Math.round(-90.89f));
	}

	/**
	 * @tests java.lang.Math#sin(double)
	 */
	public void test_sinD() {
		// Test for method double java.lang.Math.sin(double)
		assertEquals("Incorrect answer", 0.0, Math.sin(0));
		assertEquals("Incorrect answer", 0.8414709848078965, Math.sin(1));
	}

    /**
     * @tests java.lang.Math#sinh(double)
     */
    public void test_sinh_D() {
        // Test for special situations
        assertEquals("Should return NaN", Double.NaN, Math.sinh(Double.NaN));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.sinh(Double.POSITIVE_INFINITY));
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, Math.sinh(Double.NEGATIVE_INFINITY));
        assertEquals("Should return 0.0", 0.0, Math.sinh(0.0));
        assertEquals("Should return +0.0", +0.0, Math.sinh(+0.0));
        assertEquals("Should return -0.0", -0.0, Math.sinh(-0.0));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.sinh(1234.56));
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, Math.sinh(-1234.56));
        assertEquals("Should return 1.0000000000001666E-6",
                1.0000000000001666E-6, Math.sinh(0.000001));
        assertEquals("Should return -1.0000000000001666E-6",
                -1.0000000000001666E-6, Math.sinh(-0.000001));
        assertEquals("Should return 5.115386441963859", 5.115386441963859, Math
                .sinh(2.33482));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.sinh(Double.MAX_VALUE));
        assertEquals("Should return 4.9E-324", 4.9E-324, Math
                .sinh(Double.MIN_VALUE));
    }
    
	/**
	 * @tests java.lang.Math#sqrt(double)
	 */
	public void test_sqrtD() {
		// Test for method double java.lang.Math.sqrt(double)
                assertEquals("Incorrect root returned2", 7, Math.sqrt(49), 0);
	}

	/**
	 * @tests java.lang.Math#tan(double)
	 */
	public void test_tanD() {
		// Test for method double java.lang.Math.tan(double)
		assertEquals("Incorrect answer", 0.0, Math.tan(0));
		assertEquals("Incorrect answer", 1.5574077246549023, Math.tan(1));

	}

    /**
     * @tests java.lang.Math#tanh(double)
     */
    public void test_tanh_D() {
        // Test for special situations
        assertEquals("Should return NaN", Double.NaN, Math.tanh(Double.NaN));
        assertEquals("Should return +1.0", +1.0, Math
                .tanh(Double.POSITIVE_INFINITY));
        assertEquals("Should return -1.0", -1.0, Math
                .tanh(Double.NEGATIVE_INFINITY));
        assertEquals("Should return +0.0", +0.0, Math.tanh(+0.0));
        assertEquals("Should return -0.0", -0.0, Math.tanh(-0.0));
        assertEquals("Should return 0.0", 0.0, Math.tanh(0.0));

        assertEquals("Should return 1.0", 1.0, Math.tanh(1234.56));
        assertEquals("Should return -1.0", -1.0, Math.tanh(-1234.56));
        assertEquals("Should return 9.999999999996666E-7",
                9.999999999996666E-7, Math.tanh(0.000001));
        assertEquals("Should return 0.981422884124941", 0.981422884124941, Math
                .tanh(2.33482));
        assertEquals("Should return 1.0", 1.0, Math.tanh(Double.MAX_VALUE));
        assertEquals("Should return 4.9E-324", 4.9E-324, Math
                .tanh(Double.MIN_VALUE));
    }
    
	/**
	 * @tests java.lang.Math#random()
	 */
	public void test_random() {
		// There isn't a place for these tests so just stick them here
		assertEquals("Wrong value E",
				4613303445314885481L, Double.doubleToLongBits(Math.E));
		assertEquals("Wrong value PI",
				4614256656552045848L, Double.doubleToLongBits(Math.PI));

		for (int i = 500; i >= 0; i--) {
			double d = Math.random();
			assertTrue("Generated number is out of range: " + d, d >= 0.0
					&& d < 1.0);
		}
	}

	/**
	 * @tests java.lang.Math#toRadians(double)
	 */
	public void test_toRadiansD() {
		for (double d = 500; d >= 0; d -= 1.0) {
			double converted = Math.toDegrees(Math.toRadians(d));
			assertTrue("Converted number not equal to original. d = " + d,
					converted >= d * 0.99999999 && converted <= d * 1.00000001);
		}
	}

	/**
	 * @tests java.lang.Math#toDegrees(double)
	 */
	public void test_toDegreesD() {
		for (double d = 500; d >= 0; d -= 1.0) {
			double converted = Math.toRadians(Math.toDegrees(d));
			assertTrue("Converted number not equal to original. d = " + d,
					converted >= d * 0.99999999 && converted <= d * 1.00000001);
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
