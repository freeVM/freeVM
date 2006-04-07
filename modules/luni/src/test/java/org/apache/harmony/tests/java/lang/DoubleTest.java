/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

public class DoubleTest extends TestCase {

	/**
	 * @tests java.lang.Double#compareTo(java.lang.Double)
	 * @tests java.lang.Double#compare(double, double)
	 */
	public void test_compareToLjava_lang_Double() {
		// A selection of double values in ascending order.
		double[] values = new double[] { Double.NEGATIVE_INFINITY,
				-Double.MAX_VALUE, -2d, -Double.MIN_VALUE, -0d, 0d,
				Double.MIN_VALUE, 2d, Double.MAX_VALUE,
				Double.POSITIVE_INFINITY, Double.NaN };
		for (int i = 0; i < values.length; i++) {
			double d1 = values[i];

			// Test that each value compares equal to itself; and each object is
			// equal to another object like itself.
			assertTrue("Assert 0: compare() should be equal: " + d1,
					Double.compare(d1, d1) == 0);
			Double objDouble = new Double(d1);
			assertTrue("Assert 1: compareTo() should be equal: " + d1,
					objDouble.compareTo(objDouble) == 0);

			// Test that the Double-defined order is respected
			for (int j = i + 1; j < values.length; j++) {
				double d2 = values[j];
				assertTrue("Assert 2: compare() " + d1 + " should be less " + d2, 
						Double.compare(d1, d2) == -1);
				assertTrue("Assert 3: compare() " + d2 + " should be greater " + d1,
						Double.compare(d2, d1) == 1);
				Double D2 = new Double(d2);
				assertTrue("Assert 4: compareTo() " + d1 + " should be less " + d2,
						objDouble.compareTo(D2) == -1);
				assertTrue("Assert 5: compareTo() " + d2 + " should be greater " + d1,
						D2.compareTo(objDouble) == 1);
			}
		}
	}

	/**
	 * @tests java.lang.Double#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		Double d1 = new Double(87654321.12345d);
		Double d2 = new Double(87654321.12345d);
		Double d3 = new Double(0.0002f);
		assertTrue("Assert 0: Equality test failed",
				d1.equals(d2) && !(d1.equals(d3)));

		assertTrue("Assert 2: NaN should not be == Nan",
				Double.NaN != Double.NaN);
		assertTrue("Assert 3: NaN should not be == Nan",
				new Double(Double.NaN).equals(new Double(Double.NaN)));
		assertTrue("Assert 4: -0d should be == 0d",
				0d == -0d);
		assertTrue("Assert 5: -0d should not be equals() 0d",
				!new Double(0d).equals(new Double(-0d)));
	}
    
    /**
     * @tests java.lang.Double#toHexString(double)
     */
    public void test_toHexStringF() {
        //the follow values come from the Double Javadoc/Spec
        assertEquals("0x0.0p0", Double.toHexString(0.0D));
        assertEquals("-0x0.0p0", Double.toHexString(-0.0D));
        assertEquals("0x1.0p0", Double.toHexString(1.0D));
        assertEquals("-0x1.0p0", Double.toHexString(-1.0D));
        assertEquals("0x1.0p1", Double.toHexString(2.0D));
        assertEquals("0x1.8p1", Double.toHexString(3.0D));
        assertEquals("0x1.0p-1", Double.toHexString(0.5D));
        assertEquals("0x1.0p-2", Double.toHexString(0.25D));
        assertEquals("0x1.fffffffffffffp1023", Double.toHexString(Double.MAX_VALUE));
        assertEquals("0x0.0000000000001p-1022", Double.toHexString(Double.MIN_VALUE));
        
        //test edge cases
        assertEquals("NaN", Double.toHexString(Double.NaN));
        assertEquals("-Infinity", Double.toHexString(Double.NEGATIVE_INFINITY));
        assertEquals("Infinity", Double.toHexString(Double.POSITIVE_INFINITY));
        
        //test various numbers
        assertEquals("-0x1.da8p6", Double.toHexString(-118.625D));
        assertEquals("0x1.2957874cccccdp23", Double.toHexString(9743299.65D));
        assertEquals("0x1.2957874cccccdp23", Double.toHexString(9743299.65000D));
        assertEquals("0x1.2957874cccf63p23", Double.toHexString(9743299.650001234D));
        assertEquals("0x1.700d1061d3333p33", Double.toHexString(12349743299.65000D));
    }
    
    /**
     * @tests java.lang.Double#valueOf(double)
     */
    public void test_valueOfD() {
        assertEquals(new Double(Double.MIN_VALUE), Double.valueOf(Double.MIN_VALUE));
        assertEquals(new Double(Double.MAX_VALUE), Double.valueOf(Double.MAX_VALUE));
        assertEquals(new Double(0), Double.valueOf(0));

        int s = -128;
        while (s < 128) {
            assertEquals(new Double(s), Double.valueOf(s));
            assertEquals(new Double(s + 0.1D), Double.valueOf(s + 0.1D));
            s++;
        }
    }
}
