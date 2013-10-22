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

package java.text;


import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

import com.ibm.oti.util.Msg;

/**
 * NumberFormat is the abstract superclass of Formats which format and parse
 * Numbers.
 */
public abstract class NumberFormat extends Format {

	private static final long serialVersionUID = -2308460125733713944L;

	/**
	 * Field constant.
	 */
	public static final int INTEGER_FIELD = 0;

	/**
	 * Field constant.
	 */
	public static final int FRACTION_FIELD = 1;

	private boolean groupingUsed = true, parseIntegerOnly = false;

	private int maximumIntegerDigits = 40, minimumIntegerDigits = 1,
			maximumFractionDigits = 3, minimumFractionDigits = 0;

	/**
	 * Constructs a new instance of DateFormat.
	 */
	public NumberFormat() {
	}

	/**
	 * Answers a new NumberFormat with the same properties as this NumberFormat.
	 * 
	 * @return a shallow copy of this NumberFormat
	 * 
	 * @see java.lang.Cloneable
	 */
	public Object clone() {
		return super.clone();
	}

	/**
	 * Compares the specified object to this NumberFormat and answer if they are
	 * equal. The object must be an instance of NumberFormat and have the same
	 * properties.
	 * 
	 * @param object
	 *            the object to compare with this object
	 * @return true if the specified object is equal to this NumberFormat, false
	 *         otherwise
	 * 
	 * @see #hashCode
	 */
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (!(object instanceof NumberFormat))
			return false;
		NumberFormat obj = (NumberFormat) object;
		return groupingUsed == obj.groupingUsed
				&& parseIntegerOnly == obj.parseIntegerOnly
				&& maximumFractionDigits == obj.maximumFractionDigits
				&& maximumIntegerDigits == obj.maximumIntegerDigits
				&& minimumFractionDigits == obj.minimumFractionDigits
				&& minimumIntegerDigits == obj.minimumIntegerDigits;
	}

	/**
	 * Formats the specified double using the rules of this NumberFormat.
	 * 
	 * @param value
	 *            the double to format
	 * @return the formatted String
	 */
	public final String format(double value) {
		return format(value, new StringBuffer(), new FieldPosition(0))
				.toString();
	}

	/**
	 * Formats the double value into the specified StringBuffer using the rules
	 * of this NumberFormat. If the field specified by the FieldPosition is
	 * formatted, set the begin and end index of the formatted field in the
	 * FieldPosition.
	 * 
	 * @param value
	 *            the double to format
	 * @param buffer
	 *            the StringBuffer
	 * @param field
	 *            the FieldPosition
	 * @return the StringBuffer parameter <code>buffer</code>
	 */
	public abstract StringBuffer format(double value, StringBuffer buffer,
			FieldPosition field);

	/**
	 * Formats the specified long using the rules of this NumberFormat.
	 * 
	 * @param value
	 *            the long to format
	 * @return the formatted String
	 */
	public final String format(long value) {
		return format(value, new StringBuffer(), new FieldPosition(0))
				.toString();
	}

	/**
	 * Formats the long value into the specified StringBuffer using the rules of
	 * this NumberFormat. If the field specified by the FieldPosition is
	 * formatted, set the begin and end index of the formatted field in the
	 * FieldPosition.
	 * 
	 * @param value
	 *            the long to format
	 * @param buffer
	 *            the StringBuffer
	 * @param field
	 *            the FieldPosition
	 * @return the StringBuffer parameter <code>buffer</code>
	 */
	public abstract StringBuffer format(long value, StringBuffer buffer,
			FieldPosition field);

	/**
	 * Formats the specified object into the specified StringBuffer using the
	 * rules of this DateFormat. If the field specified by the FieldPosition is
	 * formatted, set the begin and end index of the formatted field in the
	 * FieldPosition.
	 * 
	 * @param object
	 *            the object to format, must be a Number
	 * @param buffer
	 *            the StringBuffer
	 * @param field
	 *            the FieldPosition
	 * @return the StringBuffer parameter <code>buffer</code>
	 * 
	 * @exception IllegalArgumentException
	 *                when the object is not a Number
	 */
	public StringBuffer format(Object object, StringBuffer buffer,
			FieldPosition field) {
		if (object instanceof Number) {
			double dv = ((Number) object).doubleValue();
			long lv = ((Number) object).longValue();
			if (dv == lv)
				return format(lv, buffer, field);
			return format(dv, buffer, field);
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Gets the list of installed Locales which support NumberFormat.
	 * 
	 * @return an array of Locale
	 */
	public static Locale[] getAvailableLocales() {
		return Locale.getAvailableLocales();
	}

	/**
	 * Answers the currency used by this number format
	 * <p>
	 * This implementation throws UnsupportedOperationException, concrete sub
	 * classes should override if they support currency formatting.
	 * <p>
	 * 
	 * @return currency currency that was set in getInstance() or in
	 *         setCurrency(), or null
	 * @throws java.lang.UnsupportedOperationException
	 */
	public Currency getCurrency() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Answers a NumberFormat for formatting and parsing currency for the
	 * default Locale.
	 * 
	 * @return a NumberFormat
	 */
	public final static NumberFormat getCurrencyInstance() {
		return getCurrencyInstance(Locale.getDefault());
	}

	/**
	 * Answers a NumberFormat for formatting and parsing currency for the
	 * specified Locale.
	 * 
	 * @param locale
	 *            the Locale
	 * @return a NumberFormat
	 */
	public static NumberFormat getCurrencyInstance(Locale locale) {
		return getInstance(locale, "Currency");
	}

	/**
	 * Answers a NumberFormat for formatting and parsing integers for the
	 * default Locale.
	 * 
	 * @return a NumberFormat
	 */
	public final static NumberFormat getIntegerInstance() {
		return getIntegerInstance(Locale.getDefault());
	}

	/**
	 * Answers a NumberFormat for formatting and parsing integers for the
	 * specified Locale.
	 * 
	 * @param locale
	 *            the Locale
	 * @return a NumberFormat
	 */
	public static NumberFormat getIntegerInstance(Locale locale) {
		NumberFormat format = getInstance(locale, "Integer");
		format.setParseIntegerOnly(true);
		return format;
	}

	/**
	 * Answers a NumberFormat for formatting and parsing numbers for the default
	 * Locale.
	 * 
	 * @return a NumberFormat
	 */
	public final static NumberFormat getInstance() {
		return getNumberInstance();
	}

	/**
	 * Answers a NumberFormat for formatting and parsing numbers for the
	 * specified Locale.
	 * 
	 * @param locale
	 *            the Locale
	 * @return a NumberFormat
	 */
	public static NumberFormat getInstance(Locale locale) {
		return getNumberInstance(locale);
	}

	static NumberFormat getInstance(Locale locale, String type) {
		return new DecimalFormat(getPattern(locale, type),
				new DecimalFormatSymbols(locale));
	}

	/**
	 * Answers the maximum number of fraction digits that are printed when
	 * formatting. If the maximum is less than the number of fraction digits,
	 * the least significant digits are truncated.
	 * 
	 * @return the maximum number of fraction digits
	 */
	public int getMaximumFractionDigits() {
		return maximumFractionDigits;
	}

	/**
	 * Answers the maximum number of integer digits that are printed when
	 * formatting. If the maximum is less than the number of integer digits, the
	 * most significant digits are truncated.
	 * 
	 * @return the maximum number of integer digits
	 */
	public int getMaximumIntegerDigits() {
		return maximumIntegerDigits;
	}

	/**
	 * Answers the minimum number of fraction digits that are printed when
	 * formatting.
	 * 
	 * @return the minimum number of fraction digits
	 */
	public int getMinimumFractionDigits() {
		return minimumFractionDigits;
	}

	/**
	 * Answers the minimum number of integer digits that are printed when
	 * formatting.
	 * 
	 * @return the minimum number of integer digits
	 */
	public int getMinimumIntegerDigits() {
		return minimumIntegerDigits;
	}

	/**
	 * Answers a NumberFormat for formatting and parsing numbers for the default
	 * Locale.
	 * 
	 * @return a NumberFormat
	 */
	public final static NumberFormat getNumberInstance() {
		return getNumberInstance(Locale.getDefault());
	}

	/**
	 * Answers a NumberFormat for formatting and parsing numbers for the
	 * specified Locale.
	 * 
	 * @param locale
	 *            the Locale
	 * @return a NumberFormat
	 */
	public static NumberFormat getNumberInstance(Locale locale) {
		return getInstance(locale, "Number");
	}

	static String getPattern(Locale locale, String type) {
		ResourceBundle bundle = getBundle(locale);
		return bundle.getString(type);
	}

	/**
	 * Answers a NumberFormat for formatting and parsing percentages for the
	 * default Locale.
	 * 
	 * @return a NumberFormat
	 */
	public final static NumberFormat getPercentInstance() {
		return getPercentInstance(Locale.getDefault());
	}

	/**
	 * Answers a NumberFormat for formatting and parsing percentages for the
	 * specified Locale.
	 * 
	 * @param locale
	 *            the Locale
	 * @return a NumberFormat
	 */
	public static NumberFormat getPercentInstance(Locale locale) {
		return getInstance(locale, "Percent");
	}

	/**
	 * Answers an integer hash code for the receiver. Objects which are equal
	 * answer the same value for this method.
	 * 
	 * @return the receiver's hash
	 * 
	 * @see #equals
	 */
	public int hashCode() {
		return (groupingUsed ? 1231 : 1237) + (parseIntegerOnly ? 1231 : 1237)
				+ maximumFractionDigits + maximumIntegerDigits
				+ minimumFractionDigits + minimumIntegerDigits;
	}

	/**
	 * Answers whether this NumberFormat formats and parses numbers using a
	 * grouping separator.
	 * 
	 * @return true when a grouping separator is used, false otherwise
	 */
	public boolean isGroupingUsed() {
		return groupingUsed;
	}

	/**
	 * Answers whether this NumberFormat only parses integer numbers. Parsing
	 * stops if a decimal separator is encountered.
	 * 
	 * @return true if this NumberFormat only parses integers, false for parsing
	 *         integers or fractions
	 */
	public boolean isParseIntegerOnly() {
		return parseIntegerOnly;
	}

	/**
	 * Parse a Number from the specified String using the rules of this
	 * NumberFormat.
	 * 
	 * @param string
	 *            the String to parse
	 * @return the Number resulting from the parse
	 * 
	 * @exception ParseException
	 *                when an error occurs during parsing
	 */
	public Number parse(String string) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Number number = parse(string, pos);
		if (pos.getErrorIndex() != -1 || pos.getIndex() == 0)
			throw new ParseException(null, pos.getErrorIndex());
		return number;
	}

	/**
	 * Parse a Number from the specified String starting at the index specified
	 * by the ParsePosition. If the string is successfully parsed, the index of
	 * the ParsePosition is updated to the index following the parsed text.
	 * 
	 * @param string
	 *            the String to parse
	 * @param position
	 *            the ParsePosition, updated on return with the index following
	 *            the parsed text, or on error the index is unchanged and the
	 *            error index is set to the index where the error occurred
	 * @return the Number resulting from the parse, or null if there is an error
	 */
	public abstract Number parse(String string, ParsePosition position);

	/**
	 * Parse a Number from the specified String starting at the index specified
	 * by the ParsePosition. If the string is successfully parsed, the index of
	 * the ParsePosition is updated to the index following the parsed text.
	 * 
	 * @param string
	 *            the String to parse
	 * @param position
	 *            the ParsePosition, updated on return with the index following
	 *            the parsed text, or on error the index is unchanged and the
	 *            error index is set to the index where the error occurred
	 * @return the Number resulting from the parse, or null if there is an error
	 */
	public final Object parseObject(String string, ParsePosition position) {
		return parse(string, position);
	}

	/**
	 * Sets the currency used by this number format when formatting currency
	 * values.
	 * <p>
	 * The min and max fraction digits remain the same.
	 * <p>
	 * This implementation throws UnsupportedOperationException, concrete sub
	 * classes should override if they support currency formatting.
	 * <p>
	 * 
	 * @param currency
	 *            the new Currency
	 * @throws java.lang.UnsupportedOperationException
	 */
	public void setCurrency(Currency currency) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets whether this NumberFormat formats and parses numbers using a
	 * grouping separator.
	 * 
	 * @param value
	 *            true when a grouping separator is used, false otherwise
	 */
	public void setGroupingUsed(boolean value) {
		groupingUsed = value;
	}

	/**
	 * Sets the maximum number of fraction digits that are printed when
	 * formatting. If the maximum is less than the number of fraction digits,
	 * the least significant digits are truncated.
	 * 
	 * @param value
	 *            the maximum number of fraction digits
	 */
	public void setMaximumFractionDigits(int value) {
		maximumFractionDigits = value < 0 ? 0 : value;
		if (maximumFractionDigits < minimumFractionDigits)
			minimumFractionDigits = maximumFractionDigits;
	}

	/**
	 * Used to specify the new maximum count of integer digits that are printed
	 * when formatting. If the maximum is less than the number of integer
	 * digits, the most significant digits are truncated.
	 * 
	 * @param value
	 *            the new maximum number of integer numerals for display
	 */
	public void setMaximumIntegerDigits(int value) {
		maximumIntegerDigits = value < 0 ? 0 : value;
		if (maximumIntegerDigits < minimumIntegerDigits)
			minimumIntegerDigits = maximumIntegerDigits;
	}

	/**
	 * Sets the minimum number of fraction digits that are printed when
	 * formatting.
	 * 
	 * @param value
	 *            the minimum number of fraction digits
	 */
	public void setMinimumFractionDigits(int value) {
		minimumFractionDigits = value < 0 ? 0 : value;
		if (maximumFractionDigits < minimumFractionDigits)
			maximumFractionDigits = minimumFractionDigits;
	}

	/**
	 * Sets the minimum number of integer digits that are printed when
	 * formatting.
	 * 
	 * @param value
	 *            the minimum number of integer digits
	 */
	public void setMinimumIntegerDigits(int value) {
		minimumIntegerDigits = value < 0 ? 0 : value;
		if (maximumIntegerDigits < minimumIntegerDigits)
			maximumIntegerDigits = minimumIntegerDigits;
	}

	/**
	 * Specifies if this NumberFormat should only parse numbers as integers or
	 * else as any kind of number. If this is called with a <code>true</code>
	 * value then subsequent parsing attempts will stop if a decimal separator
	 * is encountered.
	 * 
	 * @param value
	 *            <code>true</code> to only parse integers, <code>false</code>
	 *            to parse integers and fractions
	 */
	public void setParseIntegerOnly(boolean value) {
		parseIntegerOnly = value;
	}

	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField("groupingUsed", Boolean.TYPE),
			new ObjectStreamField("maxFractionDigits", Byte.TYPE),
			new ObjectStreamField("maximumFractionDigits", Integer.TYPE),
			new ObjectStreamField("maximumIntegerDigits", Integer.TYPE),
			new ObjectStreamField("maxIntegerDigits", Byte.TYPE),
			new ObjectStreamField("minFractionDigits", Byte.TYPE),
			new ObjectStreamField("minimumFractionDigits", Integer.TYPE),
			new ObjectStreamField("minimumIntegerDigits", Integer.TYPE),
			new ObjectStreamField("minIntegerDigits", Byte.TYPE),
			new ObjectStreamField("parseIntegerOnly", Boolean.TYPE),
			new ObjectStreamField("serialVersionOnStream", Integer.TYPE), };

	private void writeObject(ObjectOutputStream stream) throws IOException {
		ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("groupingUsed", groupingUsed);
		fields
				.put(
						"maxFractionDigits",
						maximumFractionDigits < Byte.MAX_VALUE ? (byte) maximumFractionDigits
								: Byte.MAX_VALUE);
		fields.put("maximumFractionDigits", maximumFractionDigits);
		fields.put("maximumIntegerDigits", maximumIntegerDigits);
		fields
				.put(
						"maxIntegerDigits",
						maximumIntegerDigits < Byte.MAX_VALUE ? (byte) maximumIntegerDigits
								: Byte.MAX_VALUE);
		fields
				.put(
						"minFractionDigits",
						minimumFractionDigits < Byte.MAX_VALUE ? (byte) minimumFractionDigits
								: Byte.MAX_VALUE);
		fields.put("minimumFractionDigits", minimumFractionDigits);
		fields.put("minimumIntegerDigits", minimumIntegerDigits);
		fields
				.put(
						"minIntegerDigits",
						minimumIntegerDigits < Byte.MAX_VALUE ? (byte) minimumIntegerDigits
								: Byte.MAX_VALUE);
		fields.put("parseIntegerOnly", parseIntegerOnly);
		fields.put("serialVersionOnStream", 1);
		stream.writeFields();
	}

	private void readObject(ObjectInputStream stream) throws IOException,
			ClassNotFoundException {
		ObjectInputStream.GetField fields = stream.readFields();
		groupingUsed = fields.get("groupingUsed", true);
		parseIntegerOnly = fields.get("parseIntegerOnly", false);
		if (fields.get("serialVersionOnStream", 0) == 0) {
			maximumFractionDigits = fields.get("maxFractionDigits", (byte) 3);
			maximumIntegerDigits = fields.get("maxIntegerDigits", (byte) 40);
			minimumFractionDigits = fields.get("minFractionDigits", (byte) 0);
			minimumIntegerDigits = fields.get("minIntegerDigits", (byte) 1);
		} else {
			maximumFractionDigits = fields.get("maximumFractionDigits", 3);
			maximumIntegerDigits = fields.get("maximumIntegerDigits", 40);
			minimumFractionDigits = fields.get("minimumFractionDigits", 0);
			minimumIntegerDigits = fields.get("minimumIntegerDigits", 1);
		}
		if (minimumIntegerDigits > maximumIntegerDigits
				|| minimumFractionDigits > maximumFractionDigits)
			throw new InvalidObjectException(com.ibm.oti.util.Msg
					.getString("K00fa"));
		if (minimumIntegerDigits < 0 || maximumIntegerDigits < 0
				|| minimumFractionDigits < 0 || maximumFractionDigits < 0)
			throw new InvalidObjectException(com.ibm.oti.util.Msg
					.getString("K00fb"));
	}

	/**
	 * The instances of this inner class are used as attribute keys and values
	 * in AttributedCharacterIterator that
	 * NumberFormat.formatToCharacterIterator() method returns.
	 * <p>
	 * There is no public constructor to this class, the only instances are the
	 * constants defined here.
	 * <p>
	 */
	public static class Field extends Format.Field {

		public static final Field SIGN = new Field("sign");

		public static final Field INTEGER = new Field("integer");

		public static final Field FRACTION = new Field("fraction");

		public static final Field EXPONENT = new Field("exponent");

		public static final Field EXPONENT_SIGN = new Field("exponent sign");

		public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");

		public static final Field DECIMAL_SEPARATOR = new Field(
				"decimal separator");

		public static final Field GROUPING_SEPARATOR = new Field(
				"grouping separator");

		public static final Field PERCENT = new Field("percent");

		public static final Field PERMILLE = new Field("per mille");

		public static final Field CURRENCY = new Field("currency");

		/**
		 * Constructs a new instance of NumberFormat.Field with the given field
		 * name.
		 */
		protected Field(String fieldName) {
			super(fieldName);
		}

		/**
		 * serizalization method resolve instances to the constant
		 * NumberFormat.Field values
		 */
		protected Object readResolve() throws InvalidObjectException {
			if (this.equals(INTEGER))
				return INTEGER;
			if (this.equals(FRACTION))
				return FRACTION;
			if (this.equals(EXPONENT))
				return EXPONENT;
			if (this.equals(EXPONENT_SIGN))
				return EXPONENT_SIGN;
			if (this.equals(EXPONENT_SYMBOL))
				return EXPONENT_SYMBOL;
			if (this.equals(CURRENCY))
				return CURRENCY;
			if (this.equals(DECIMAL_SEPARATOR))
				return DECIMAL_SEPARATOR;
			if (this.equals(GROUPING_SEPARATOR))
				return GROUPING_SEPARATOR;
			if (this.equals(PERCENT))
				return PERCENT;
			if (this.equals(PERMILLE))
				return PERMILLE;
			if (this.equals(SIGN))
				return SIGN;

			throw new InvalidObjectException(Msg.getString("K000d"));
		}
	}

}
