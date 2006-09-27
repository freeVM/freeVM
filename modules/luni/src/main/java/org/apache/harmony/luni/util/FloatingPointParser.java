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

package org.apache.harmony.luni.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse a string and return either a single or double precision
 * floating point number.
 */
public final class FloatingPointParser {

	private static final class StringExponentPair {
		String s;

		int e;

		boolean negative;

		StringExponentPair(String s, int e, boolean negative) {
			this.s = s;
			this.e = e;
			this.negative = negative;
		}
	}

	/**
	 * Takes a String and an integer exponent. The String should hold a positive
	 * integer value (or zero). The exponent will be used to calculate the
	 * floating point number by taking the positive integer the String
	 * represents and multiplying by 10 raised to the power of the of the
	 * exponent. Returns the closest double value to the real number
	 * 
	 * @param s
	 *            the String that will be parsed to a floating point
	 * @param e
	 *            an int represent the 10 to part
	 * @return the double closest to the real number
	 * 
	 * @exception NumberFormatException
	 *                if the String doesn't represent a positive integer value
	 */
	private static native double parseDblImpl(String s, int e);

	/**
	 * Takes a String and an integer exponent. The String should hold a positive
	 * integer value (or zero). The exponent will be used to calculate the
	 * floating point number by taking the positive integer the String
	 * represents and multiplying by 10 raised to the power of the of the
	 * exponent. Returns the closest float value to the real number
	 * 
	 * @param s
	 *            the String that will be parsed to a floating point
	 * @param e
	 *            an int represent the 10 to part
	 * @return the float closest to the real number
	 * 
	 * @exception NumberFormatException
	 *                if the String doesn't represent a positive integer value
	 */
	private static native float parseFltImpl(String s, int e);

	/**
	 * Takes a String and does some initial parsing. Should return a
	 * StringExponentPair containing a String with no leading or trailing white
	 * space and trailing zeroes eliminated. The exponent of the
	 * StringExponentPair will be used to calculate the floating point number by
	 * taking the positive integer the String represents and multiplying by 10
	 * raised to the power of the of the exponent.
	 * 
	 * @param s
	 *            the String that will be parsed to a floating point
	 * @param length
	 *            the length of s
	 * @return a StringExponentPair with necessary values
	 * 
	 * @exception NumberFormatException
	 *                if the String doesn't pass basic tests
	 */
	private static StringExponentPair initialParse(String s, int length) {
		boolean negative = false;
		char c;
		int start, end, decimal;
		int e = 0;

		start = 0;
		if (length == 0)
			throw new NumberFormatException(s);

		c = s.charAt(length - 1);
		if (c == 'D' || c == 'd' || c == 'F' || c == 'f') {
			length--;
			if (length == 0)
				throw new NumberFormatException(s);
		}

		end = Math.max(s.indexOf('E'), s.indexOf('e'));
		if (end > -1) {
			if (end + 1 == length)
				throw new NumberFormatException(s);

                        int exponent_offset = end + 1;
                        if (s.charAt(exponent_offset) == '+') {
                                if (s.charAt(exponent_offset + 1) == '-') {
                                        throw new NumberFormatException(s);
                                }
                                exponent_offset++; // skip the plus sign
                        }
			try {
				e = Integer.parseInt(s.substring(exponent_offset,
                                                                 length));
                        } catch (NumberFormatException ex) {
                                // ex contains the exponent substring
                                // only so throw a new exception with
                                // the correct string
				throw new NumberFormatException(s);
                        }                            
                                    
		} else {
			end = length;
		}
		if (length == 0)
			throw new NumberFormatException(s);

		c = s.charAt(start);
		if (c == '-') {
			++start;
			--length;
			negative = true;
		} else if (c == '+') {
			++start;
			--length;
		}
		if (length == 0)
			throw new NumberFormatException(s);

		decimal = s.indexOf('.');
		if (decimal > -1) {
			e -= end - decimal - 1;
			s = s.substring(start, decimal) + s.substring(decimal + 1, end);
		} else {
			s = s.substring(start, end);
		}

		if ((length = s.length()) == 0)
			throw new NumberFormatException();

		end = length;
		while (end > 1 && s.charAt(end - 1) == '0')
			--end;

		start = 0;
		while (start < end - 1 && s.charAt(start) == '0')
			start++;

		if (end != length || start != 0) {
			e += length - end;
			s = s.substring(start, end);
		}

		return new StringExponentPair(s, e, negative);
	}

	/*
	 * Assumes the string is trimmed.
	 */
	private static double parseDblName(String namedDouble, int length) {
		// Valid strings are only +Nan, NaN, -Nan, +Infinity, Infinity,
		// -Infinity.
		if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
			throw new NumberFormatException();
		}

		boolean negative = false;
		int cmpstart = 0;
		switch (namedDouble.charAt(0)) {
		case '-':
			negative = true; // fall through
		case '+':
			cmpstart = 1;
		default:
		}

		if (namedDouble.regionMatches(false, cmpstart, "Infinity", 0, 8)) {
			return negative ? Double.NEGATIVE_INFINITY
					: Float.POSITIVE_INFINITY;
		}

		if (namedDouble.regionMatches(false, cmpstart, "NaN", 0, 3)) {
			return Double.NaN;
		}

		throw new NumberFormatException();
	}

	/*
	 * Assumes the string is trimmed.
	 */
	private static float parseFltName(String namedFloat, int length) {
		// Valid strings are only +Nan, NaN, -Nan, +Infinity, Infinity,
		// -Infinity.
		if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
			throw new NumberFormatException();
		}

		boolean negative = false;
		int cmpstart = 0;
		switch (namedFloat.charAt(0)) {
		case '-':
			negative = true; // fall through
		case '+':
			cmpstart = 1;
		default:
		}

		if (namedFloat.regionMatches(false, cmpstart, "Infinity", 0, 8)) {
			return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		}

		if (namedFloat.regionMatches(false, cmpstart, "NaN", 0, 3)) {
			return Float.NaN;
		}

		throw new NumberFormatException();
	}

	/**
	 * Returns the closest double value to the real number in the string.
	 * 
	 * @param s
	 *            the String that will be parsed to a floating point
	 * @return the double closest to the real number
	 * 
	 * @exception NumberFormatException
	 *                if the String doesn't represent a double
	 */
	public static double parseDouble(String s) {
		s = s.trim();
		int length = s.length();

		if (length == 0) {
			throw new NumberFormatException(s);
		}

		// See if this could be a named double
		char last = s.charAt(length - 1);
		if ((last == 'y') || (last == 'N')) {
			return parseDblName(s, length);
		}
        
        // See if it could be a hexadecimal representation
        if (s.toLowerCase().indexOf("0x") != -1) { //$NON-NLS-1$
            return HexStringParser.parseDouble(s);
        }
        
		StringExponentPair info = initialParse(s, length);

		double result = parseDblImpl(info.s, info.e);
		if (info.negative)
			result = -result;

		return result;
	}

	/**
	 * Returns the closest float value to the real number in the string.
	 * 
	 * @param s
	 *            the String that will be parsed to a floating point
	 * @return the float closest to the real number
	 * 
	 * @exception NumberFormatException
	 *                if the String doesn't represent a float
	 */
	public static float parseFloat(String s) {
		s = s.trim();
		int length = s.length();

		if (length == 0) {
			throw new NumberFormatException(s);
		}

		// See if this could be a named float
		char last = s.charAt(length - 1);
		if ((last == 'y') || (last == 'N')) {
			return parseFltName(s, length);
		}
		StringExponentPair info = initialParse(s, length);

		float result = parseFltImpl(info.s, info.e);
		if (info.negative)
			result = -result;

		return result;
	}
}

/*
 * Parses hex string to a single or double precision floating point number.
 */
final class HexStringParser {

    private static final int DOUBLE_EXPONENT_WIDTH = 11;

    private static final int DOUBLE_MANTISSA_WIDTH = 52;

    private static final int FLOAT_EXPONENT_WIDTH = 8;

    private static final int FLOAT_MANTISSA_WIDTH = 23;
    
    private static final int HEX_RADIX = 16;

    private static final String HEX_SIGNIFICANT = "0[xX](\\p{XDigit}+\\.?|\\p{XDigit}*\\.\\p{XDigit}+)"; //$NON-NLS-1$

    private static final String BINARY_EXPONENT = "[pP]([+-]?\\d+)"; //$NON-NLS-1$

    private static final String FLOAT_TYPE_SUFFIX = "[fFdD]?"; //$NON-NLS-1$

    private static final String HEX_PATTERN = "[\\x00-\\x20]*([+-]?)" + HEX_SIGNIFICANT //$NON-NLS-1$
            + BINARY_EXPONENT + FLOAT_TYPE_SUFFIX + "[\\x00-\\x20]*"; //$NON-NLS-1$

    private static final Pattern PATTERN = Pattern.compile(HEX_PATTERN);

    private final int EXPONENT_WIDTH;

    private final int MANTISSA_WIDTH;
    
    private final long EXPONENT_BASE;
    
    private final long MAX_EXPONENT;

    private long sign;

    private long exponent;

    private long mantissa;

    public HexStringParser(int exponent_width, int mantissa_width) {
        this.EXPONENT_WIDTH = exponent_width;
        this.MANTISSA_WIDTH = mantissa_width;
        
        this.EXPONENT_BASE = ~(-1L << (exponent_width - 1));
        this.MAX_EXPONENT = ~(-1L << exponent_width);
    }

    /*
     * Parses the hex string to a double number.
     */
    public static double parseDouble(String hexString) {
        HexStringParser parser = new HexStringParser(DOUBLE_EXPONENT_WIDTH,
                DOUBLE_MANTISSA_WIDTH);
        long result = parser.parse(hexString);
        return Double.longBitsToDouble(result);
    }

    /*
     * Parses the hex string to a float number.
     */
    public static float parseFloat(String hexString) {
        HexStringParser parser = new HexStringParser(FLOAT_EXPONENT_WIDTH,
                FLOAT_MANTISSA_WIDTH);
        int result = (int) parser.parse(hexString);
        return Float.intBitsToFloat(result);
    }

    private long parse(String hexString) {
        String[] hexSegments = getSegmentsFromHexString(hexString);
        String signStr = hexSegments[0];
        String significantStr = hexSegments[1];
        String exponentStr = hexSegments[2];

        parseHexSign(signStr);
        parseExponent(exponentStr);
        parseMantissa(significantStr);

        sign <<= (MANTISSA_WIDTH + EXPONENT_WIDTH);
        exponent <<= MANTISSA_WIDTH;
        return sign | exponent | mantissa;
    }

    /*
     * Analyzes the hex string and extracts the sign and digit segments.
     */
    private static String[] getSegmentsFromHexString(String hexString) {
        Matcher matcher = PATTERN.matcher(hexString);
        if (!matcher.matches()) {
            throw new NumberFormatException();
        }

        String[] hexSegments = new String[3];
        hexSegments[0] = matcher.group(1);
        hexSegments[1] = matcher.group(2);
        hexSegments[2] = matcher.group(3);

        return hexSegments;
    }

    // Parses the sign field
    private void parseHexSign(String signStr) {
        this.sign = signStr.equals("-") ? 1 : 0; //$NON-NLS-1$
    }

    // Parses the exponent field
    private void parseExponent(String exponentStr) {
        char leadingChar = exponentStr.charAt(0);
        int sign = (leadingChar == '-' ? -1 : 1);
        if (!Character.isDigit(leadingChar)) {
            exponentStr = exponentStr.substring(1);
        }

        try {
            exponent = sign * Long.parseLong(exponentStr) + EXPONENT_BASE;
        } catch (NumberFormatException e) {
            exponent = sign * Long.MAX_VALUE;
        }
    }
   
    // Parses the mantissa field
    private void parseMantissa(String significantStr) {
        String[] strings = significantStr.split("\\."); //$NON-NLS-1$
        String strIntegerPart = strings[0];
        String strDecimalPart = strings.length > 1 ? strings[1] : ""; //$NON-NLS-1$

        String significand = getNormalizedSignificand(strIntegerPart, strDecimalPart);
        if(significand.equals("0")){ //$NON-NLS-1$
            setZero();
            return;
        }
        
        int offset = getOffset(strIntegerPart, strDecimalPart);
        boolean isOverFlow = checkedAddExponent(offset);
        if (isOverFlow) {
            return;
        }

        // TODO
    }

    private void setInfinite() {
        exponent = MAX_EXPONENT;
        mantissa = 0;
    }

    private void setZero() {
        exponent = 0;
        mantissa = 0;
    }

    private boolean checkedAddExponent(long offset) {
        double d = exponent;
        boolean isOverFlow = false;
        d += offset;
        if (d >= Long.MAX_VALUE) {
            setInfinite();
            isOverFlow = true;
        } else if (d <= -Long.MAX_VALUE) {
            setZero();
            isOverFlow = true;
        } else {
            exponent += offset;
        }
        return isOverFlow;
    }

    /*
     * Returns the normalized significand after removing the leading zeros.
     */
    private String getNormalizedSignificand(String strIntegerPart, String strDecimalPart) {
        String significand = strIntegerPart + strDecimalPart;
        significand = significand.replaceFirst("^0+", ""); //$NON-NLS-1$//$NON-NLS-2$
        if (significand.length() == 0) {
            significand = "0"; //$NON-NLS-1$
        }
        return significand;
    }

    /*
     * Calculates the offset between the normalized number and unnormalized
     * number. In a normalized representation, significand is represented by the
     * characters "0x1." followed by a lowercase hexadecimal representation of
     * the rest of the significand as a fraction.
     */
    private int getOffset(String strIntegerPart, String strDecimalPart) {
        strIntegerPart = strIntegerPart.replaceFirst("^0+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        //If the Interger part is a nonzero number.
        if (strIntegerPart.length() != 0) {
            String leadingNumber = strIntegerPart.substring(0, 1);
            return (strIntegerPart.length() - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber,HEX_RADIX)) - 1;
        }
        
        //If the Interger part is a zero number.
        int i;
        for (i = 0; i < strDecimalPart.length() && strDecimalPart.charAt(i) == '0'; i++);   
        if (i == strDecimalPart.length()) {
            return 0;
        }
        String leadingNumber=strDecimalPart.substring(i,i + 1);
        return (-i - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber, HEX_RADIX)) - 1;
    }

    private int countBitsLength(long value) {
        int leadingZeros = Long.numberOfLeadingZeros(value);
        return Long.SIZE - leadingZeros;
    }
}