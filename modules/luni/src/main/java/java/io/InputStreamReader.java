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

package java.io; 


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.security.AccessController;
import java.util.HashMap;

import org.apache.harmony.luni.util.PriviAction;

/**
 * InputStreamReader is class for turning a byte Stream into a character Stream.
 * Data read from the source input stream is converted into characters by either
 * a default or provided character converter. By default, the encoding is
 * assumed to ISO8859_1. The InputStreamReader contains a buffer of bytes read
 * from the source input stream and converts these into characters as needed.
 * The buffer size is 8K.
 * 
 * 
 * @see OutputStreamWriter
 */
public class InputStreamReader extends Reader {
	private InputStream in;

	private static final int BUFFER_SIZE = 8192;

	CharsetDecoder decoder;

	ByteBuffer bytes = ByteBuffer.allocate(BUFFER_SIZE);

	CharBuffer chars = CharBuffer.allocate(BUFFER_SIZE);

	/**
	 * Constructs a new InputStreamReader on the InputStream <code>in</code>.
	 * Now character reading can be filtered through this InputStreamReader.
	 * This constructor assumes the default conversion of ISO8859_1
	 * (ISO-Latin-1).
	 * 
	 * @param in
	 *            the InputStream to convert to characters.
	 */
	public InputStreamReader(InputStream in) {
		super(in);
		this.in = in;
		String encoding = (String) AccessController
				.doPrivileged(new PriviAction("file.encoding", "ISO8859_1")); //$NON-NLS-1$//$NON-NLS-2$
		decoder = Charset.forName(encoding).newDecoder();
		chars.limit(0);
	}

	/**
	 * Constructs a new InputStreamReader on the InputStream <code>in</code>.
	 * Now character reading can be filtered through this InputStreamReader.
	 * This constructor takes a String parameter <code>enc</code> which is the
	 * name of an encoding. If the encoding cannot be found, an
	 * UnsupportedEncodingException error is thrown.
	 * 
	 * @param in
	 *            the InputStream to convert to characters.
	 * @param enc
	 *            a String describing the character converter to use.
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the encoding cannot be found.
	 */
	public InputStreamReader(InputStream in, final String enc)
			throws UnsupportedEncodingException {
		super(in);
		enc.length();
		this.in = in;
		try {
			decoder = Charset.forName(enc).newDecoder()
			          .onMalformedInput(CodingErrorAction.REPLACE)
			          .onUnmappableCharacter(CodingErrorAction.REPLACE);
		} catch (IllegalArgumentException e) {
			throw new UnsupportedEncodingException();
		}
		chars.limit(0);
	}

	/**
	 * Constructs a new InputStreamReader on the InputStream <code>in</code>
	 * and CharsetDecoder <code>dec</code>. Now character reading can be
	 * filtered through this InputStreamReader.
	 * 
	 * @param in
	 *            the InputStream to convert to characters
	 * @param dec
	 *            a CharsetDecoder used by the character convertion
	 */
	public InputStreamReader(InputStream in, CharsetDecoder dec) {
		super(in);
		dec.averageCharsPerByte();
		this.in = in;
		decoder = dec;
		chars.limit(0);
	}

	/**
	 * Constructs a new InputStreamReader on the InputStream <code>in</code>
	 * and Charset <code>charset</code>. Now character reading can be
	 * filtered through this InputStreamReader.
	 * 
	 * @param in
	 *            the InputStream to convert to characters
	 * @param charset
	 *            the Charset that specify the character converter
	 */
	public InputStreamReader(InputStream in, Charset charset) {
		super(in);
		this.in = in;
		decoder = charset.newDecoder();
		chars.limit(0);
	}

	/**
	 * Close this InputStreamReader. This implementation closes the source
	 * InputStream and releases all local storage.
	 * 
	 * 
	 * @throws IOException
	 *             If an error occurs attempting to close this
	 *             InputStreamReader.
	 */
	public void close() throws IOException {
		synchronized (lock) {
			decoder = null;
			if (in != null) {
				in.close();
				in = null;
			}
		}
	}

	/**
	 * Answer the String which identifies the encoding used to convert bytes to
	 * characters. The value <code>null</code> is returned if this Reader has
	 * been closed.
	 * 
	 * @return the String describing the converter or null if this Reader is
	 *         closed.
	 */
	public String getEncoding() {
		if (!isOpen()) {
			return null;
		}
		return HistoricalNamesUtil.getHistoricalName(decoder.charset().name());
	}

	/*
	 * helper for getEncoding()
	 */
	static class HistoricalNamesUtil {
		private static HashMap historicalNames = new HashMap();
		static {
			historicalNames.put("Big5-HKSCS", "Big5_HKSCS");
			historicalNames.put("EUC-JP", "EUC_JP");
			historicalNames.put("EUC-KR", "EUC_KR");
			historicalNames.put("GB2312", "EUC_CN");
			historicalNames.put("IBM-Thai", "Cp838");
			historicalNames.put("IBM00858", "Cp858");
			historicalNames.put("IBM01140", "Cp1140");
			historicalNames.put("IBM01141", "Cp1141");
			historicalNames.put("IBM01142", "Cp1142");
			historicalNames.put("IBM01143", "Cp1143");
			historicalNames.put("IBM01144", "Cp1144");
			historicalNames.put("IBM01145", "Cp1145");
			historicalNames.put("IBM01146", "Cp1146");
			historicalNames.put("IBM01147", "Cp1147");
			historicalNames.put("IBM01148", "Cp1148");
			historicalNames.put("IBM01149", "Cp1149");
			historicalNames.put("IBM037", "Cp037");
			historicalNames.put("IBM1026", "Cp1026");
			historicalNames.put("IBM1047", "Cp1047");
			historicalNames.put("IBM273", "Cp273");
			historicalNames.put("IBM277", "Cp277");
			historicalNames.put("IBM278", "Cp278");
			historicalNames.put("IBM280", "Cp280");
			historicalNames.put("IBM284", "Cp284");
			historicalNames.put("IBM285", "Cp285");
			historicalNames.put("IBM297", "Cp297");
			historicalNames.put("IBM420", "Cp420");
			historicalNames.put("IBM424", "Cp424");
			historicalNames.put("IBM437", "Cp437");
			historicalNames.put("IBM500", "Cp500");
			historicalNames.put("IBM775", "Cp775");
			historicalNames.put("IBM850", "Cp850");
			historicalNames.put("IBM852", "Cp852");
			historicalNames.put("IBM855", "Cp855");
			historicalNames.put("IBM857", "Cp857");
			historicalNames.put("IBM860", "Cp860");
			historicalNames.put("IBM861", "Cp861");
			historicalNames.put("IBM862", "Cp862");
			historicalNames.put("IBM863", "Cp863");
			historicalNames.put("IBM864", "Cp864");
			historicalNames.put("IBM865", "Cp865");
			historicalNames.put("IBM866", "Cp866");
			historicalNames.put("IBM868", "Cp868");
			historicalNames.put("IBM869", "Cp869");
			historicalNames.put("IBM870", "Cp870");
			historicalNames.put("IBM871", "Cp871");
			historicalNames.put("IBM918", "Cp918");
			historicalNames.put("ISO-2022-CN", "ISO2022CN");
			historicalNames.put("ISO-2022-JP", "ISO2022JP");
			historicalNames.put("ISO-2022-KR", "ISO2022KR");
			historicalNames.put("ISO-8859-1", "ISO8859_1");
			historicalNames.put("ISO-8859-13", "ISO8859_13");
			historicalNames.put("ISO-8859-15", "ISO8859_15");
			historicalNames.put("ISO-8859-2", "ISO8859_2");
			historicalNames.put("ISO-8859-3", "ISO8859_3");
			historicalNames.put("ISO-8859-4", "ISO8859_4");
			historicalNames.put("ISO-8859-5", "ISO8859_5");
			historicalNames.put("ISO-8859-6", "ISO8859_6");
			historicalNames.put("ISO-8859-7", "ISO8859_7");
			historicalNames.put("ISO-8859-8", "ISO8859_8");
			historicalNames.put("ISO-8859-9", "ISO8859_9");
			historicalNames.put("KOI8-R", "KOI8_R");
			historicalNames.put("Shift_JIS", "SJIS");
			historicalNames.put("TIS-620", "TIS620");
			historicalNames.put("US-ASCII", "ASCII");
			historicalNames.put("UTF-16BE", "UnicodeBigUnmarked");
			historicalNames.put("UTF-16LE", "UnicodeLittleUnmarked");
			historicalNames.put("UTF-8", "UTF8");
			historicalNames.put("windows-1250", "Cp1250");
			historicalNames.put("windows-1251", "Cp1251");
			historicalNames.put("windows-1252", "Cp1252");
			historicalNames.put("windows-1253", "Cp1253");
			historicalNames.put("windows-1254", "Cp1254");
			historicalNames.put("windows-1255", "Cp1255");
			historicalNames.put("windows-1256", "Cp1256");
			historicalNames.put("windows-1257", "Cp1257");
			historicalNames.put("windows-1258", "Cp1258");
			historicalNames.put("windows-31j", "MS932");
			historicalNames.put("x-Big5-Solaris", "Big5_Solaris");
			historicalNames.put("x-euc-jp-linux", "EUC_JP_LINUX");
			historicalNames.put("x-EUC-TW", "EUC_TW");
			historicalNames.put("x-eucJP-Open", "EUC_JP_Solaris");
			historicalNames.put("x-IBM1006", "Cp1006");
			historicalNames.put("x-IBM1025", "Cp1025");
			historicalNames.put("x-IBM1046", "Cp1046");
			historicalNames.put("x-IBM1097", "Cp1097");
			historicalNames.put("x-IBM1098", "Cp1098");
			historicalNames.put("x-IBM1112", "Cp1112");
			historicalNames.put("x-IBM1122", "Cp1122");
			historicalNames.put("x-IBM1123", "Cp1123");
			historicalNames.put("x-IBM1124", "Cp1124");
			historicalNames.put("x-IBM1381", "Cp1381");
			historicalNames.put("x-IBM1383", "Cp1383");
			historicalNames.put("x-IBM33722", "Cp33722");
			historicalNames.put("x-IBM737", "Cp737");
			historicalNames.put("x-IBM856", "Cp856");
			historicalNames.put("x-IBM874", "Cp874");
			historicalNames.put("x-IBM875", "Cp875");
			historicalNames.put("x-IBM921", "Cp921");
			historicalNames.put("x-IBM922", "Cp922");
			historicalNames.put("x-IBM930", "Cp930");
			historicalNames.put("x-IBM933", "Cp933");
			historicalNames.put("x-IBM935", "Cp935");
			historicalNames.put("x-IBM937", "Cp937");
			historicalNames.put("x-IBM939", "Cp939");
			historicalNames.put("x-IBM942", "Cp942");
			historicalNames.put("x-IBM942C", "Cp942C");
			historicalNames.put("x-IBM943", "Cp943");
			historicalNames.put("x-IBM943C", "Cp943C");
			historicalNames.put("x-IBM948", "Cp948");
			historicalNames.put("x-IBM949", "Cp949");
			historicalNames.put("x-IBM949C", "Cp949C");
			historicalNames.put("x-IBM950", "Cp950");
			historicalNames.put("x-IBM964", "Cp964");
			historicalNames.put("x-IBM970", "Cp970");
			historicalNames.put("x-ISCII91", "ISCII91");
			historicalNames.put("x-ISO-2022-CN-CNS", "ISO2022CN");
			historicalNames.put("x-ISO-2022-CN-GB", "ISO2022CN");
			historicalNames.put("x-JISAutoDetect", "JISAutoDetect");
			historicalNames.put("x-MacArabic", "MacArabic");
			historicalNames.put("x-MacCentralEurope", "MacCentralEurope");
			historicalNames.put("x-MacCroatian", "MacCroatian");
			historicalNames.put("x-MacCyrillic", "MacCyrillic");
			historicalNames.put("x-MacDingbat", "MacDingbat");
			historicalNames.put("x-MacGreek", "MacGreek");
			historicalNames.put("x-MacHebrew", "MacHebrew");
			historicalNames.put("x-MacIceland", "MacIceland");
			historicalNames.put("x-MacRoman", "MacRoman");
			historicalNames.put("x-MacRomania", "MacRomania");
			historicalNames.put("x-MacSymbol", "MacSymbol");
			historicalNames.put("x-MacThai", "MacThai");
			historicalNames.put("x-MacTurkish", "MacTurkish");
			historicalNames.put("x-MacUkraine", "MacUkraine");
			historicalNames.put("x-MS950-HKSCS", "MS950_HKSCS");
			historicalNames.put("x-mswin-936", "MS936");
			historicalNames.put("x-PCK", "PCK");
			historicalNames.put("x-windows-874", "MS874");
			historicalNames.put("x-windows-949", "MS949");
			historicalNames.put("x-windows-950", "MS950");
		}

		public static String getHistoricalName(String name) {
			return (String) (historicalNames.get(name) == null ? name
					: historicalNames.get(name));
		}
	}

	/**
	 * Reads a single character from this InputStreamReader and returns the
	 * result as an int. The 2 higher-order characters are set to 0. If the end
	 * of reader was encountered then return -1. The byte value is either
	 * obtained from converting bytes in this readers buffer or by first filling
	 * the buffer from the source InputStream and then reading from the buffer.
	 * 
	 * @return the character read or -1 if end of reader.
	 * 
	 * @throws IOException
	 *             If the InputStreamReader is already closed or some other IO
	 *             error occurs.
	 */
	public int read() throws IOException {
		synchronized (lock) {
			if (isOpen()) {
				if (chars.limit() == chars.position()) {
					fillBuf();
				}
				if (chars.limit() == 0) {
					return -1;
				}
				return chars.get();
			}
			throw new IOException("InputStreamReader is closed."); //$NON-NLS-1$
		}
	}

	/**
	 * Reads at most <code>count</code> characters from this Reader and stores
	 * them at <code>offset</code> in the character array <code>buf</code>.
	 * Returns the number of characters actually read or -1 if the end of reader
	 * was encountered. The bytes are either obtained from converting bytes in
	 * this readers buffer or by first filling the buffer from the source
	 * InputStream and then reading from the buffer.
	 * 
	 * @param buf
	 *            character array to store the read characters
	 * @param offset
	 *            offset in buf to store the read characters
	 * @param length
	 *            maximum number of characters to read
	 * @return the number of characters read or -1 if end of reader.
	 * 
	 * @throws IOException
	 *             If the InputStreamReader is already closed or some other IO
	 *             error occurs.
	 */
	public int read(char[] buf, int offset, int length) throws IOException {
		synchronized (lock) {
			if (isOpen()) {
				if (length == 0)
					return 0;
				if (offset < 0 || length < 0 || offset + length > buf.length) {
					throw new IndexOutOfBoundsException();
				}
				// read at least once
				if (chars.limit() == chars.position()) {
					fillBuf();
				}
				int position = chars.position();
				int availableChars = chars.limit() - position;
				// read at least once for one byte
				int needChars = length;
				while (availableChars < needChars) {
					System.arraycopy(chars.array(), position, buf, offset,
							availableChars);
					chars.position(position + availableChars);
					needChars -= availableChars;
					offset += availableChars;
					if (in.available() <= 0) {
						return needChars == length ? -1 : length - needChars;
					}
					fillBuf();
					position = chars.position();
					availableChars = chars.limit();
					if (availableChars == 0) {
						return needChars == length ? -1 : length - needChars;
					}
				}
				System.arraycopy(chars.array(), position, buf, offset,
						needChars);
				chars.position(chars.position() + needChars);
				return length;
			}
			throw new IOException("InputStreamReader is closed."); //$NON-NLS-1$
		}
	}

	/*
	 * Answer a boolean indicating whether or not this InputStreamReader is
	 * open.
	 */
	private boolean isOpen() {
		return in != null;
	}

	/*
	 * refill the buffer from wrapped InputStream
	 */
	private void fillBuf() throws IOException {
		chars.clear();
		int read = 0;
		try {
			read = in.read(bytes.array());
		} catch (IOException e) {
			chars.limit(0);
			throw e;
		}
		if (read == -1) {
			chars.limit(0);
			return;
		}
		bytes.limit(read);
		boolean endOfInput = read < BUFFER_SIZE;
		CoderResult result = decoder.decode(bytes, chars, endOfInput);
		if (result.isError()) {
			throw new IOException(result.toString());
		}
		bytes.clear();
		chars.flip();
	}

	/**
	 * Answers a <code>boolean</code> indicating whether or not this
	 * InputStreamReader is ready to be read without blocking. If the result is
	 * <code>true</code>, the next <code>read()</code> will not block. If
	 * the result is <code>false</code> this Reader may or may not block when
	 * <code>read()</code> is sent. This implementation answers
	 * <code>true</code> if there are bytes available in the buffer or the
	 * source InputStream has bytes available.
	 * 
	 * @return <code>true</code> if the receiver will not block when
	 *         <code>read()</code> is called, <code>false</code> if unknown
	 *         or blocking will occur.
	 * 
	 * @throws IOException
	 *             If the InputStreamReader is already closed or some other IO
	 *             error occurs.
	 */
	public boolean ready() throws IOException {
		synchronized (lock) {
			if (in == null) {
				throw new IOException("This reader has been closed!"); //$NON-NLS-1$
			}
			try {
				return chars.limit() > chars.position() || in.available() > 0;
			} catch (IOException e) {
				return false;
			}

		}
	}
}
