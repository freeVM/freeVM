/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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
package tests.api.java.lang;

import java.util.Properties;

public class LongTest extends junit.framework.TestCase {

	Properties orgProps;

	/**
	 * @tests java.lang.Long#Long(long)
	 */
	public void test_ConstructorJ() {
		// Test for method java.lang.Long(long)

		Long l = new Long(-8900000006L);
		assertTrue("Created incorrect Long", l.longValue() == -8900000006L);
	}

	/**
	 * @tests java.lang.Long#Long(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.lang.Long(java.lang.String)

		Long l = new Long("-8900000006");
		assertTrue("Created incorrect Long", l.longValue() == -8900000006L);
	}

	/**
	 * @tests java.lang.Long#byteValue()
	 */
	public void test_byteValue() {
		// Test for method byte java.lang.Long.byteValue()
		Long l = new Long(127);
		assertTrue("Returned incorrect byte value", l.byteValue() == 127);
		assertTrue("Returned incorrect byte value", new Long(Long.MAX_VALUE)
				.byteValue() == -1);
	}

	/**
	 * @tests java.lang.Long#compareTo(java.lang.Long)
	 */
	public void test_compareToLjava_lang_Long() {
		// Test for method int java.lang.Long.compareTo(java.lang.Long)
		assertTrue("-2 compared to 1 gave non-negative answer", new Long(-2L)
				.compareTo(new Long(1L)) < 0);
		assertTrue("-2 compared to -2 gave non-zero answer", new Long(-2L)
				.compareTo(new Long(-2L)) == 0);
		assertTrue("3 compared to 2 gave non-positive answer", new Long(3L)
				.compareTo(new Long(2L)) > 0);
        
        try {
            new Long(0).compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
	}

	/**
	 * @tests java.lang.Long#decode(java.lang.String)
	 */
	public void test_decodeLjava_lang_String() {
		// Test for method java.lang.Long
		// java.lang.Long.decode(java.lang.String)
		assertTrue("Returned incorrect value for hex string", Long.decode(
				"0xFF").longValue() == 255L);
		assertTrue("Returned incorrect value for dec string", Long.decode(
				"-89000").longValue() == -89000L);
		assertTrue("Returned incorrect value for 0 decimal", Long.decode("0")
				.longValue() == 0);
		assertTrue("Returned incorrect value for 0 hex", Long.decode("0x0")
				.longValue() == 0);
		assertTrue(
				"Returned incorrect value for most negative value decimal",
				Long.decode("-9223372036854775808").longValue() == 0x8000000000000000L);
		assertTrue(
				"Returned incorrect value for most negative value hex",
				Long.decode("-0x8000000000000000").longValue() == 0x8000000000000000L);
		assertTrue(
				"Returned incorrect value for most positive value decimal",
				Long.decode("9223372036854775807").longValue() == 0x7fffffffffffffffL);
		assertTrue(
				"Returned incorrect value for most positive value hex",
				Long.decode("0x7fffffffffffffff").longValue() == 0x7fffffffffffffffL);
		assertTrue("Failed for 07654321765432", Long.decode("07654321765432")
				.longValue() == 07654321765432l);

		boolean exception = false;
		try {
			Long
					.decode("999999999999999999999999999999999999999999999999999999");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for value > ilong", exception);

		exception = false;
		try {
			Long.decode("9223372036854775808");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

		exception = false;
		try {
			Long.decode("-9223372036854775809");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

		exception = false;
		try {
			Long.decode("0x8000000000000000");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

		exception = false;
		try {
			Long.decode("-0x8000000000000001");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for hex MIN_VALUE - 1", exception);

		exception = false;
		try {
			Long.decode("42325917317067571199");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for 42325917317067571199",
				exception);
	}

	/**
	 * @tests java.lang.Long#doubleValue()
	 */
	public void test_doubleValue() {
		// Test for method double java.lang.Long.doubleValue()
		Long l = new Long(10000000);
		assertTrue("Returned incorrect double value",
				l.doubleValue() == 10000000.0);

	}

	/**
	 * @tests java.lang.Long#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean java.lang.Long.equals(java.lang.Object)

		Long l = new Long(80000006777L);
		Long l2 = new Long(80000006777L);
		Long l3 = new Long(806777L);
		assertTrue("Equality test failed", l.equals(l2) && !(l.equals(l3)));
	}

	/**
	 * @tests java.lang.Long#floatValue()
	 */
	public void test_floatValue() {
		// Test for method float java.lang.Long.floatValue()
		Long l = new Long(100000);
		assertTrue("Returned incorrect float value: " + l.floatValue(), l
				.floatValue() == 100000f);
	}

	/**
	 * @tests java.lang.Long#getLong(java.lang.String)
	 */
	public void test_getLongLjava_lang_String() {
		// Test for method java.lang.Long
		// java.lang.Long.getLong(java.lang.String)
		Properties tProps = new Properties();
		tProps.put("testLong", "99");
		System.setProperties(tProps);
		assertTrue("returned incorrect Long", Long.getLong("testLong").equals(
				new Long(99)));
		assertTrue("returned incorrect default Long",
				Long.getLong("ff") == null);
	}

	/**
	 * @tests java.lang.Long#getLong(java.lang.String, long)
	 */
	public void test_getLongLjava_lang_StringJ() {
		// Test for method java.lang.Long
		// java.lang.Long.getLong(java.lang.String, long)
		Properties tProps = new Properties();
		tProps.put("testLong", "99");
		System.setProperties(tProps);
		assertTrue("returned incorrect Long", Long.getLong("testLong", 4L)
				.equals(new Long(99)));
		assertTrue("returned incorrect default Long", Long.getLong("ff", 4L)
				.equals(new Long(4)));
	}

	/**
	 * @tests java.lang.Long#getLong(java.lang.String, java.lang.Long)
	 */
	public void test_getLongLjava_lang_StringLjava_lang_Long() {
		// Test for method java.lang.Long
		// java.lang.Long.getLong(java.lang.String, java.lang.Long)
		Properties tProps = new Properties();
		tProps.put("testLong", "99");
		System.setProperties(tProps);
		assertTrue("returned incorrect Long", Long.getLong("testLong",
				new Long(4)).equals(new Long(99)));
		assertTrue("returned incorrect default Long", Long.getLong("ff",
				new Long(4)).equals(new Long(4)));
	}

	/**
	 * @tests java.lang.Long#hashCode()
	 */
	public void test_hashCode() {
		// Test for method int java.lang.Long.hashCode()

		Long l = new Long(Long.MAX_VALUE);
		Long l2 = new Long(89000L);
		// hashCode is implemented as val ^ (val>>>32)
		// for max int this is 7fffffffffffffff ^ 000000007fffffff
		// result is the low 32 of 7fffffff80000000
		assertTrue("Returned invalid hashcode", l.hashCode() == 0x80000000
				&& (l2.hashCode() == 89000));
	}

	/**
	 * @tests java.lang.Long#intValue()
	 */
	public void test_intValue() {
		// Test for method int java.lang.Long.intValue()
		Long l = new Long(100000);
		assertTrue("Returned incorrect int value", l.intValue() == 100000);
		assertTrue("Returned incorrect int value", new Long(Long.MAX_VALUE)
				.intValue() == -1);
	}

	/**
	 * @tests java.lang.Long#longValue()
	 */
	public void test_longValue() {
		// Test for method long java.lang.Long.longValue()
		Long l = new Long(89000000005L);
		assertTrue("Returned incorrect long value",
				l.longValue() == 89000000005L);
	}

	/**
	 * @tests java.lang.Long#parseLong(java.lang.String)
	 */
	public void test_parseLongLjava_lang_String() {
		// Test for method long java.lang.Long.parseLong(java.lang.String)

		long l = Long.parseLong("89000000005");
		assertTrue("Parsed to incorrect long value", l == 89000000005L);
		assertTrue("Returned incorrect value for 0", Long.parseLong("0") == 0);
		assertTrue("Returned incorrect value for most negative value", Long
				.parseLong("-9223372036854775808") == 0x8000000000000000L);
		assertTrue("Returned incorrect value for most positive value", Long
				.parseLong("9223372036854775807") == 0x7fffffffffffffffL);

		boolean exception = false;
		try {
			Long.parseLong("9223372036854775808");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

		exception = false;
		try {
			Long.parseLong("-9223372036854775809");
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);
	}

	/**
	 * @tests java.lang.Long#parseLong(java.lang.String, int)
	 */
	public void test_parseLongLjava_lang_StringI() {
		// Test for method long java.lang.Long.parseLong(java.lang.String, int)
		assertTrue("Returned incorrect value",
				Long.parseLong("100000000", 10) == 100000000L);
		assertTrue("Returned incorrect value from hex string", Long.parseLong(
				"FFFFFFFFF", 16) == 68719476735L);
		assertTrue("Returned incorrect value from octal string: "
				+ Long.parseLong("77777777777"), Long.parseLong("77777777777",
				8) == 8589934591L);
		assertTrue("Returned incorrect value for 0 hex", Long
				.parseLong("0", 16) == 0);
		assertTrue("Returned incorrect value for most negative value hex", Long
				.parseLong("-8000000000000000", 16) == 0x8000000000000000L);
		assertTrue("Returned incorrect value for most positive value hex", Long
				.parseLong("7fffffffffffffff", 16) == 0x7fffffffffffffffL);
		assertTrue("Returned incorrect value for 0 decimal", Long.parseLong(
				"0", 10) == 0);
		assertTrue(
				"Returned incorrect value for most negative value decimal",
				Long.parseLong("-9223372036854775808", 10) == 0x8000000000000000L);
		assertTrue(
				"Returned incorrect value for most positive value decimal",
				Long.parseLong("9223372036854775807", 10) == 0x7fffffffffffffffL);

		boolean exception = false;
		try {
			Long.parseLong("999999999999", 8);
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);

		exception = false;
		try {
			Long.parseLong("9223372036854775808", 10);
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

		exception = false;
		try {
			Long.parseLong("-9223372036854775809", 10);
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

		exception = false;
		try {
			Long.parseLong("8000000000000000", 16);
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

		exception = false;
		try {
			Long.parseLong("-8000000000000001", 16);
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for hex MIN_VALUE + 1", exception);

		exception = false;
		try {
			Long.parseLong("42325917317067571199", 10);
		} catch (NumberFormatException e) {
			// Correct
			exception = true;
		}
		assertTrue("Failed to throw exception for 42325917317067571199",
				exception);
	}

	/**
	 * @tests java.lang.Long#shortValue()
	 */
	public void test_shortValue() {
		// Test for method short java.lang.Long.shortValue()
		Long l = new Long(10000);
		assertTrue("Returned incorrect short value", l.shortValue() == 10000);
		assertTrue("Returned incorrect short value", new Long(Long.MAX_VALUE)
				.shortValue() == -1);
	}

	/**
	 * @tests java.lang.Long#toBinaryString(long)
	 */
	public void test_toBinaryStringJ() {
		// Test for method java.lang.String java.lang.Long.toBinaryString(long)
		assertTrue("Incorrect binary string returned", Long.toBinaryString(
				890000L).equals("11011001010010010000"));
		assertTrue(
				"Incorrect binary string returned",
				Long
						.toBinaryString(Long.MIN_VALUE)
						.equals(
								"1000000000000000000000000000000000000000000000000000000000000000"));
		assertTrue(
				"Incorrect binary string returned",
				Long
						.toBinaryString(Long.MAX_VALUE)
						.equals(
								"111111111111111111111111111111111111111111111111111111111111111"));
	}

	/**
	 * @tests java.lang.Long#toHexString(long)
	 */
	public void test_toHexStringJ() {
		// Test for method java.lang.String java.lang.Long.toHexString(long)
		assertTrue("Incorrect hex string returned", Long.toHexString(89000005L)
				.equals("54e0845"));
		assertTrue("Incorrect hex string returned", Long.toHexString(
				Long.MIN_VALUE).equals("8000000000000000"));
		assertTrue("Incorrect hex string returned", Long.toHexString(
				Long.MAX_VALUE).equals("7fffffffffffffff"));
	}

	/**
	 * @tests java.lang.Long#toOctalString(long)
	 */
	public void test_toOctalStringJ() {
		// Test for method java.lang.String java.lang.Long.toOctalString(long)
		assertTrue("Returned incorrect oct string", Long.toOctalString(
				8589934591L).equals("77777777777"));
		assertTrue("Returned incorrect oct string", Long.toOctalString(
				Long.MIN_VALUE).equals("1000000000000000000000"));
		assertTrue("Returned incorrect oct string", Long.toOctalString(
				Long.MAX_VALUE).equals("777777777777777777777"));
	}

	/**
	 * @tests java.lang.Long#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.lang.Long.toString()
		Long l = new Long(89000000005L);
		assertTrue("Returned incorrect String", l.toString().equals(
				"89000000005"));
		assertTrue("Returned incorrect String", new Long(Long.MIN_VALUE)
				.toString().equals("-9223372036854775808"));
		assertTrue("Returned incorrect String", new Long(Long.MAX_VALUE)
				.toString().equals("9223372036854775807"));
	}

	/**
	 * @tests java.lang.Long#toString(long)
	 */
	public void test_toStringJ() {
		// Test for method java.lang.String java.lang.Long.toString(long)

		assertTrue("Returned incorrect String", Long.toString(89000000005L)
				.equals("89000000005"));
		assertTrue("Returned incorrect String", Long.toString(Long.MIN_VALUE)
				.equals("-9223372036854775808"));
		assertTrue("Returned incorrect String", Long.toString(Long.MAX_VALUE)
				.equals("9223372036854775807"));
	}

	/**
	 * @tests java.lang.Long#toString(long, int)
	 */
	public void test_toStringJI() {
		// Test for method java.lang.String java.lang.Long.toString(long, int)
		assertTrue("Returned incorrect dec string", Long.toString(100000000L,
				10).equals("100000000"));
		assertTrue("Returned incorrect hex string", Long.toString(68719476735L,
				16).equals("fffffffff"));
		assertTrue("Returned incorrect oct string", Long.toString(8589934591L,
				8).equals("77777777777"));
		assertTrue("Returned incorrect bin string", Long.toString(
				8796093022207L, 2).equals(
				"1111111111111111111111111111111111111111111"));
		assertTrue("Returned incorrect min string", Long.toString(
				0x8000000000000000L, 10).equals("-9223372036854775808"));
		assertTrue("Returned incorrect max string", Long.toString(
				0x7fffffffffffffffL, 10).equals("9223372036854775807"));
		assertTrue("Returned incorrect min string", Long.toString(
				0x8000000000000000L, 16).equals("-8000000000000000"));
		assertTrue("Returned incorrect max string", Long.toString(
				0x7fffffffffffffffL, 16).equals("7fffffffffffffff"));
	}

	/**
	 * @tests java.lang.Long#valueOf(java.lang.String)
	 */
	public void test_valueOfLjava_lang_String() {
		// Test for method java.lang.Long
		// java.lang.Long.valueOf(java.lang.String)
		assertTrue("Returned incorrect value", Long.valueOf("100000000")
				.longValue() == 100000000L);
		assertTrue("Returned incorrect value", Long.valueOf(
				"9223372036854775807").longValue() == Long.MAX_VALUE);
		assertTrue("Returned incorrect value", Long.valueOf(
				"-9223372036854775808").longValue() == Long.MIN_VALUE);

		boolean exception = false;
		try {
			Long
					.valueOf("999999999999999999999999999999999999999999999999999999999999");
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);

		exception = false;
		try {
			Long.valueOf("9223372036854775808");
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);

		exception = false;
		try {
			Long.valueOf("-9223372036854775809");
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);
	}

	/**
	 * @tests java.lang.Long#valueOf(java.lang.String, int)
	 */
	public void test_valueOfLjava_lang_StringI() {
		// Test for method java.lang.Long
		// java.lang.Long.valueOf(java.lang.String, int)
		assertTrue("Returned incorrect value", Long.valueOf("100000000", 10)
				.longValue() == 100000000L);
		assertTrue("Returned incorrect value from hex string", Long.valueOf(
				"FFFFFFFFF", 16).longValue() == 68719476735L);
		assertTrue("Returned incorrect value from octal string: "
				+ Long.valueOf("77777777777", 8).toString(), Long.valueOf(
				"77777777777", 8).longValue() == 8589934591L);
		assertTrue("Returned incorrect value", Long.valueOf(
				"9223372036854775807", 10).longValue() == Long.MAX_VALUE);
		assertTrue("Returned incorrect value", Long.valueOf(
				"-9223372036854775808", 10).longValue() == Long.MIN_VALUE);
		assertTrue("Returned incorrect value", Long.valueOf("7fffffffffffffff",
				16).longValue() == Long.MAX_VALUE);
		assertTrue("Returned incorrect value", Long.valueOf(
				"-8000000000000000", 16).longValue() == Long.MIN_VALUE);

		boolean exception = false;
		try {
			Long.valueOf("999999999999", 8);
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);

		exception = false;
		try {
			Long.valueOf("9223372036854775808", 10);
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);

		exception = false;
		try {
			Long.valueOf("-9223372036854775809", 10);
		} catch (NumberFormatException e) {
			// correct
			exception = true;
		}
		assertTrue("Failed to throw exception when passed invalid string",
				exception);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		orgProps = System.getProperties();
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		System.setProperties(orgProps);
	}
}
