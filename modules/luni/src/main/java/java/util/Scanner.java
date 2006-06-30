/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
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
package java.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.harmony.luni.util.NotYetImplementedException;

/**
 * A parser that parses a text string to primitive types with the help of
 * regular expression. It supports localized number and various radixes.
 * 
 * The input is broken into tokens by the delimiter pattern, which is whitespace
 * by default. The primitive types can be got via corresponding next methods. If
 * the token is not in valid format, an InputMissmatchException is thrown.
 * 
 * For example: Scanner s = new Scanner("1A true");
 * System.out.println(s.nextInt(16)); System.out.println(s.nextBoolean()); The
 * result: 26 true
 * 
 * A scanner can find or skip specific pattern with no regard to the delimiter.
 * All these methods and the various next and hasNext methods may block.
 * 
 * Scanner is not thread-safe without external synchronization
 */
public final class Scanner implements Iterator<String> {

    //  Default delimiting pattern
    private static final Pattern DEFAULT_DELIMITER = Pattern
            .compile("\\p{javaWhitespace}+"); //$NON-NLS-1$
    
    // The pattern matching anything
    private static final Pattern ANY_PATTERN = Pattern.compile("(?s).*");

    private static final int DIPLOID = 2;

    // Default radix
    private static final int DEFAULT_RADIX = 10;

    private static final int READ_TRUNK_SIZE = 1024;

    // The input source of scanner
    private Readable input;

    private CharBuffer buffer;

    private Pattern delimiter = DEFAULT_DELIMITER;

    private Matcher matcher;

    private int radix = DEFAULT_RADIX;

    private Locale locale = Locale.getDefault();

    // The position where find begins
    private int findStartIndex = 0;

    // The last find start position
    private int preStartIndex = findStartIndex;

    // The length of the buffer
    private int bufferLength = 0;

    // Used by find and nextXXX operation
    private boolean closed = false;

    private IOException lastIOException;

    /**
     * Constructs a scanner that uses File as its input. The default charset is
     * applied when reading the file.
     * 
     * @param src
     *            the file to be scanned
     * @throws FileNotFoundException
     *             if the specified file is not found
     */
    public Scanner(File src) throws FileNotFoundException {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses File as its input. The specified charset
     * is applied when reading the file.
     * 
     * @param src
     *            the file to be scanned
     * @param charsetName
     *            the name of the encoding type of the file
     * @throws FileNotFoundException
     *             if the specified file is not found
     * @throws IllegalArgumentException
     *            if the specified coding does not exist
     */
    public Scanner(File src, String charsetName) throws FileNotFoundException {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00a"));
        }
        FileInputStream fis = new FileInputStream(src);
        if (null == charsetName) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA009"));
        }
        try {
            input = new InputStreamReader(fis, charsetName);
        } catch (UnsupportedEncodingException e) {
            try {
                fis.close();
            } catch (IOException ioException) {
                // ignore
            }
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Constructs a scanner that uses String as its input.
     * 
     * @param src
     *            the string to be scanned
     */
    public Scanner(String src) {
        input = new StringReader(src);
    }

    /**
     * Constructs a scanner that uses InputStream as its input. The default
     * charset is applied when decoding the input.
     * 
     * @param src
     *            the input stream to be scanned
     */
    public Scanner(InputStream src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses InputStream as its input. The specified
     * charset is applied when decoding the input.
     * 
     * @param src
     *            the input stream to be scanned
     * @param charsetName
     *            the encoding type of the input stream
     * @throws IllegalArgumentException
     *            if the specified character set is not found
     */
    public Scanner(InputStream src, String charsetName) {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00b"));
        }
        try {
            input = new InputStreamReader(src, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Constructs a scanner that uses Readable as its input.
     * 
     * @param src
     *            the Readable to be scanned
     */
    public Scanner(Readable src) {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00c"));
        }
        input = src;
    }

    /**
     * Constructs a scanner that uses ReadableByteChannel as its input. The
     * default charset is applied when decoding the input.
     * 
     * @param src
     *            the ReadableByteChannel to be scanned
     */
    public Scanner(ReadableByteChannel src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Constructs a scanner that uses ReadableByteChannel as its input. The
     * specified charset is applied when decoding the input.
     * 
     * @param src
     *            the ReadableByteChannel to be scanned
     * @param charsetName
     *            the encoding type of the content in the ReadableByteChannel
     * @throws IllegalArgumentException
     *            if the specified character set is not found           
     */
    public Scanner(ReadableByteChannel src, String charsetName) {
        if (null == src) {
            throw new NullPointerException(org.apache.harmony.luni.util.Msg
                    .getString("KA00d"));
        }
        if (null == charsetName) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA009"));
        }
        try {
            input = new InputStreamReader(Channels.newInputStream(src),
                    charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Closes the underlying input if the input implements Closeable. If the
     * scanner has been closed, this method will take no effect. The scanning
     * operation after calling this method will throw IllegalStateException
     * 
     */
    public void close() {
        if (closed == true) {
            return;
        }
        if (input instanceof Closeable) {
            try {
                ((Closeable) input).close();
            } catch (IOException e) {
                lastIOException = e;
            }
        }
        closed = true;
    }

    /**
     * Returns the <code>Pattern</code> in use by this scanner.
     * 
     * @return the <code>Pattern</code> presently in use by this scanner
     */
    public Pattern delimiter() {
        return delimiter;
    }

    //TODO: To implement this feature
    public String findInLine(Pattern pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public String findInLine(String pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public String findWithinHorizon(Pattern pattern, int horizon) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public String findWithinHorizon(String pattern, int horizon) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNext() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNext(Pattern pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNext(String pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextBigDecimal() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextBigInteger() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextBigInteger(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextBoolean() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextByte() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextByte(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextDouble() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextFloat() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextInt() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextInt(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextLine() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextLong() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextLong(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextShort() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean hasNextShort(int radix) {
        throw new NotYetImplementedException();
    }

    /**
     * returns the last IOException thrown when reading the underlying input. If
     * no exception is thrown, return null.
     * 
     * @return the last IOException thrown
     */
    public IOException ioException() {
        return lastIOException;
    }

    /**
     * return the locale of this scanner.
     * 
     * @return 
     *             the locale of this scanner
     */
    public Locale locale() {
        return locale;
    }

    //TODO: To implement this feature
    public MatchResult match() {
        throw new NotYetImplementedException();
    }

    /**
     * Finds and Returns the next complete token which is prefixed and postfixed
     * by input that matches the delimiter pattern. This method may be blocked
     * when it is waiting for input to scan, even if a previous invocation of
     * hasNext() returned true. If this match successes, the scanner advances
     * past the next complete token.
     * 
     * @return 
     *             the next complete token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next() {
        return next(ANY_PATTERN);
    }

    /**
     * Returns the next token which is prefixed and postfixed by input that
     * matches the delimiter pattern if this token matches the specified
     * pattern. This method may be blocked when it is waiting for input to scan,
     * even if a previous invocation of hasNext(Pattern) returned true. If this
     * match successes, the scanner advances past the next token that matched
     * the pattern.
     * 
     * @param pattern
     *            the specified pattern to scan
     * @return 
     *             the next token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next(Pattern pattern) {
        checkClosed();
        if (isInputExhausted()) {
            throw new NoSuchElementException();
        }
        saveCurrentStatus();
        if (!setTokenRegion()) {
            recoverPreviousStatus();
            // if setting match region fails
            throw new NoSuchElementException();
        }
        matcher.usePattern(pattern);
        if (matcher.matches()) {
            return matcher.group(0);
        } else {
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
    }

    /**
     * Returns the next token which is prefixed and postfixed by input that
     * matches the delimiter pattern if this token matches the pattern
     * constructed from the sepcified string. This method may be blocked when it
     * is waiting for input to scan. If this match successes, the scanner
     * advances past the next token that matched the pattern.
     * 
     * The invocation of this method in the form next(pattern) behaves in the
     * same way as the invocaiton of next(Pattern.compile(pattern)).
     * 
     * @param pattern
     *            the string specifying the pattern to scan for
     * @return 
     *             the next token
     * @throws IllegalStateException
     *             if this scanner has been closed
     * @throws NoSuchElementException
     *             if input has been exhausted
     */
    public String next(String pattern) {
        return next(Pattern.compile(pattern));
    }

    //TODO: To implement this feature
    public BigDecimal nextBigDecimal() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public BigInteger nextBigInteger() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public BigInteger nextBigInteger(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public boolean nextBoolean() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public byte nextByte() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public byte nextByte(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public double nextDouble() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public float nextFloat() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public int nextInt() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public int nextInt(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public String nextLine() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public long nextLong() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public long nextLong(int radix) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public short nextShort() {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public short nextShort(int radix) {
        throw new NotYetImplementedException();
    }

    /**
     * return the radix of this scanner.
     * 
     * @return
     *            the radix of this scanner
     */
    public int radix() {
        return radix;
    }

    //TODO: To implement this feature
    public Scanner skip(Pattern pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public Scanner skip(String pattern) {
        throw new NotYetImplementedException();
    }

    //TODO: To implement this feature
    public String toString() {
        throw new NotYetImplementedException();
    }

    /**
     * Set the delimiting pattern of this scanner
     * 
     * @param pattern
     *            the delimiting pattern to use
     * @return this scanner
     */
    public Scanner useDelimiter(Pattern pattern) {
        delimiter = pattern;
        return this;
    }

    /**
     * Set the delimiting pattern of this scanner with a pattern compiled from
     * the supplied string value
     * 
     * @param pattern
     *            a string from which a <code>Pattern</code> can be compiled
     * @return this scanner
     */
    public Scanner useDelimiter(String pattern) {
        return useDelimiter(Pattern.compile(pattern));
    }

    /**
     * 
     * set the locale of this scanner to a specified locale. 
     *
     * @param locale
     *              the specified locale to use
     * @return
     *              this scanner
     */
    public Scanner useLocale(Locale locale) {
        if (null == locale)
            throw new NullPointerException();
        this.locale = locale;
        return this;
    }

    /**
     * 
     * set the radix of this scanner to a specified radix.
     * 
     * @param radix
     *             the specified radix to use
     * @return
     *             this scanner
     */
    public Scanner useRadix(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("KA008", radix));
        }
        this.radix = radix;
        return this;
    }

    //TODO: To implement this feature
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
     * Check the scanner's state, if it is closed, IllegalStateException will be
     * thrown.
     */
    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException();
        }
    }

    /*
     * Check input resource of this scanner, if it has been exhausted, return
     * true.
     */
    private boolean isInputExhausted() {
        if (findStartIndex == bufferLength) {
            if (readMore()) {
                resetMatcher();
            } else {
                return true;
            }
        }
        return false;
    }

    /*
     * Change the matcher's string after reading input
     */
    private void resetMatcher() {
        if (null == matcher) {
            matcher = delimiter.matcher(buffer);
        } else {
            matcher.reset(buffer);
        }
        matcher.region(findStartIndex, bufferLength);
    }

    /*
     * save the matcher's last find position
     */
    private void saveCurrentStatus() {
        preStartIndex = findStartIndex;
    }

    /*
     * Change the matcher's status to  last find position
     */
    private void recoverPreviousStatus() {
        findStartIndex = preStartIndex;
    }

    /*
     * Find the prefixed delimiter and posefixed delimiter in the input resource
     * and set the start index and end index of Matcher region. If postfixed
     * delimiter does not exist, the end index is set to be end of input.
     */
    private boolean setTokenRegion() {
        // The position where token begins
        int tokenStartIndex = 0;
        // The position where token ends
        int tokenEndIndex = 0;
        // Use delimiter pattern
        matcher.usePattern(delimiter);
        matcher.region(findStartIndex, bufferLength);

        tokenStartIndex = findPreDelimiter();
        if (setHeadTokenRegion(tokenStartIndex)) {
            return true;
        }
        tokenEndIndex = findPostDelimiter();
        // If the second delimiter is not found
        if (-1 == tokenEndIndex) {
            // Just first Delimiter Exists
            if (findStartIndex == bufferLength) {
                return false;
            }
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
        }

        matcher.region(tokenStartIndex, tokenEndIndex);
        return true;
    }

    /*
     * Find prefixed delimiter
     */
    private int findPreDelimiter() {
        int tokenStartIndex;
        boolean findComplete = false;
        while (!findComplete) {
            if (findComplete = matcher.find()) {
                // If just delimiter remains
                if (matcher.start() == findStartIndex
                        && matcher.end() == bufferLength) {
                    // If more input resource exists
                    if (readMore()) {
                        resetMatcher();
                        findComplete = false;
                    }
                }
            } else {
                if (readMore()) {
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        tokenStartIndex = matcher.end();
        findStartIndex = matcher.end();
        return tokenStartIndex;
    }

    /*
     * Handle some special case
     */
    private boolean setHeadTokenRegion(int findIndex) {
        int tokenStartIndex;
        int tokenEndIndex;
        boolean setSuccess = false;
        // If no delimiter exists, but something exites in this scanner
        if (-1 == findIndex && preStartIndex != bufferLength) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        // If the first delimiter of scanner is not at the find start position
        if (-1 != findIndex && preStartIndex != matcher.start()) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = matcher.start();
            findStartIndex = matcher.start();
            // set match region and return
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        return setSuccess;
    }

    /*
     * Find postfixed delimiter
     */
    private int findPostDelimiter() {
        int tokenEndIndex = 0;
        boolean findComplete = false;
        while (!findComplete) {
            if (findComplete = matcher.find()) {
                tokenEndIndex = matcher.start();
                findStartIndex = matcher.start();
            } else {
                if (readMore()) {
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        return tokenEndIndex;
    }

    /*
     * Read more data from underlying Readable. Return false if nothing is
     * available.
     */
    private boolean readMore() {
        int oldBufferSize = (buffer == null ? 0 : buffer.limit());
        int oldBufferCapacity = (buffer == null ? 0 : buffer.capacity());
        // Increase capacity if empty space is not enough
        if (oldBufferSize >= oldBufferCapacity / 2) {
            expandBuffer(oldBufferSize, oldBufferCapacity);
        }

        // Read input resource
        int readCount = 0;
        try {
            buffer.limit(buffer.capacity());
            buffer.position(oldBufferSize);
            // TODO Writes test cases to test whether this == 0 is correct.
            while ((readCount = input.read(buffer)) == 0) {
                // nothing to do here
            }
        } catch (IOException e) {
            lastIOException = e;
        }
        buffer.flip();
        buffer.position(bufferLength);
        bufferLength = buffer.length() + bufferLength;
        buffer.position(0);
        return readCount != -1;
    }

    // Expand the size of internal buffer.
    private void expandBuffer(int oldBufferSize, int oldBufferCapacity) {
        int newCapacity = oldBufferCapacity * DIPLOID + READ_TRUNK_SIZE;
        char[] newBuffer = new char[newCapacity];
        if (buffer != null) {
            System.arraycopy(buffer.array(), 0, newBuffer, 0, oldBufferSize);
        }
        buffer = CharBuffer.wrap(newBuffer, 0, newCapacity);
    }
}
