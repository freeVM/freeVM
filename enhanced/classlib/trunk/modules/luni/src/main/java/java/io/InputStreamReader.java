/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.security.AccessController;

import org.apache.harmony.luni.util.HistoricalNamesUtil;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * InputStreamReader is class for turning a byte Stream into a character Stream.
 * Data read from the source input stream is converted into characters by either
 * a default or provided character converter. By default, the encoding is
 * assumed to ISO8859_1. The InputStreamReader contains a buffer of bytes read
 * from the source input stream and converts these into characters as needed.
 * The buffer size is 8K.
 * 
 * @see OutputStreamWriter
 */
public class InputStreamReader extends Reader {
    private InputStream in;

    private static final int BUFFER_SIZE = 8192;

    private boolean endOfInput = false;

    CharsetDecoder decoder;

    ByteBuffer bytes = ByteBuffer.allocate(BUFFER_SIZE);

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
        String encoding = AccessController
                .doPrivileged(new PriviAction<String>(
                        "file.encoding", "ISO8859_1")); //$NON-NLS-1$//$NON-NLS-2$
        decoder = Charset.forName(encoding).newDecoder().onMalformedInput(
                CodingErrorAction.REPLACE).onUnmappableCharacter(
                CodingErrorAction.REPLACE);
        bytes.limit(0);
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
        if (enc == null) {
            throw new NullPointerException();
        }
        this.in = in;
        try {
            decoder = Charset.forName(enc).newDecoder().onMalformedInput(
                    CodingErrorAction.REPLACE).onUnmappableCharacter(
                    CodingErrorAction.REPLACE);
        } catch (IllegalArgumentException e) {
            throw (UnsupportedEncodingException)
                    new UnsupportedEncodingException().initCause(e);
        }
        bytes.limit(0);
    }

    /**
     * Constructs a new InputStreamReader on the InputStream <code>in</code>
     * and CharsetDecoder <code>dec</code>. Now character reading can be
     * filtered through this InputStreamReader.
     * 
     * @param in
     *            the InputStream to convert to characters
     * @param dec
     *            a CharsetDecoder used by the character conversion
     */
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        dec.averageCharsPerByte();
        this.in = in;
        decoder = dec;
        bytes.limit(0);
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
        decoder = charset.newDecoder().onMalformedInput(
                CodingErrorAction.REPLACE).onUnmappableCharacter(
                CodingErrorAction.REPLACE);
        bytes.limit(0);
    }

    /**
     * Close this InputStreamReader. This implementation closes the source
     * InputStream and releases all local storage.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this
     *             InputStreamReader.
     */
    @Override
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
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                // K0070=InputStreamReader is closed.
                throw new IOException(Msg.getString("K0070")); //$NON-NLS-1$
            }

            char buf[] = new char[1];
            return read(buf, 0, 1) != -1 ? buf[0] : -1;
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
    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                // K0070=InputStreamReader is closed.
                throw new IOException(Msg.getString("K0070")); //$NON-NLS-1$
            }
            if (offset < 0 || offset > buf.length - length || length < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (length == 0) {
                return 0;
            }

            CharBuffer out = CharBuffer.wrap(buf, offset, length);
            CoderResult result = CoderResult.UNDERFLOW;

            // bytes.remaining() indicates number of bytes in buffer
            // when 1-st time entered, it'll be equal to zero
            boolean needInput = !bytes.hasRemaining();

            while (out.hasRemaining()) {
                // fill the buffer if needed
                if (needInput) {
                    if ((in.available() == 0) && (out.position() > offset)) {
                        // we could return the result without blocking read
                        break;
                    }

                    int to_read = bytes.capacity() - bytes.limit();
                    int off = bytes.arrayOffset() + bytes.limit();
                    int was_red = in.read(bytes.array(), off, to_read);

                    if (was_red == -1) {
                        endOfInput = true;
                        break;
                    } else if (was_red == 0) {
                        break;
                    }
                    bytes.limit(bytes.limit() + was_red);
                    needInput = false;
                }

                // decode bytes
                result = decoder.decode(bytes, out, false);

                if (result.isUnderflow()) {
                    // compact the buffer if no space left
                    if (bytes.limit() == bytes.capacity()) {
                        bytes.compact();
                        bytes.limit(bytes.position());
                        bytes.position(0);
                    }
                    needInput = true;
                } else {
                    break;
                }
            }

            if (result == CoderResult.UNDERFLOW && endOfInput) {
                result = decoder.decode(bytes, out, true);
                decoder.flush(out);
                decoder.reset();
            }
            if (result.isMalformed()) {
                throw new MalformedInputException(result.length());
            } else if (result.isUnmappable()) {
                throw new UnmappableCharacterException(result.length());
            }

            return out.position() - offset == 0 ? -1 : out.position() - offset;
        }
    }

    /*
     * Answer a boolean indicating whether or not this InputStreamReader is
     * open.
     */
    private boolean isOpen() {
        return in != null;
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
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (in == null) {
                // K0070=InputStreamReader is closed.
                throw new IOException(Msg.getString("K0070")); //$NON-NLS-1$
            }
            try {
                return bytes.hasRemaining() || in.available() > 0;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
