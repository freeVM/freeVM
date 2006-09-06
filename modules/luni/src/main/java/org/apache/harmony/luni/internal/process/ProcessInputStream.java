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

package org.apache.harmony.luni.internal.process;

import java.io.FileDescriptor;
import java.io.IOException;

class ProcessInputStream extends java.io.InputStream {

    private long handle;

    private FileDescriptor fd;

    // Fill in the JNI id caches
    private static native void oneTimeInitialization();

    static {
        oneTimeInitialization();
    }

    /**
     * Native to determine the bytes available.
     */
    private native int availableImpl() throws IOException;

    /**
     * Native to read into the buffer from the stream.
     */
    private native int readImpl(byte[] buf, int offset, int nbytes, long hndl)
            throws IOException;

    /**
     * Native to set the FileDescriptor handle.
     */
    private native void setFDImpl(FileDescriptor fd, long handle);

    /**
     * Native to close the stream.
     */
    private native void closeImpl() throws IOException;

    /**
     * Open an InputStream based on the handle.
     */
    protected ProcessInputStream(long handle) {
        this.fd = new java.io.FileDescriptor();
        setFDImpl(fd, handle);
        this.handle = handle;
    }

    @Override
    public int available() throws IOException {
        synchronized (this) {
            if (handle == -1) {
                return -1;
            }
            return availableImpl();
        }
    }

    /**
     * There is no way, at the library/vm level, to know when the stream will be
     * available for closing. If the user doesn't close it in his code, the
     * finalize() will run (eventually ?) and close the dangling OS
     * fileDescriptor.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (handle == -1) {
                return;
            }
            closeImpl();
            handle = -1;
        }
    }

    @Override
    public int read() throws IOException {
        byte buf[] = new byte[1];
        synchronized (this) {
            if (readImpl(buf, 0, 1, handle) == -1) {
                return -1;
            }
        }

        return buf[0];
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        synchronized (this) {
            return readImpl(buffer, 0, buffer.length, handle);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int nbytes) throws IOException {
        synchronized (this) {
            if (handle == -1) {
                return -1;
            }
            if ((nbytes < 0 || nbytes > buffer.length)
                    || (offset < 0 || offset > buffer.length)
                    || ((nbytes + offset) > buffer.length)) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return readImpl(buffer, offset, nbytes, handle);
        }
    }
}
