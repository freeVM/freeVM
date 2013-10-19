/* Copyright 2004, 2006 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This is the portable implementation of the file system interface.
 * 
 */
class OSFileSystem extends OSComponent implements IFileSystem {

	/**
	 * 
	 */
	public OSFileSystem() {
		super();
	}

	private final void validateLockArgs(int type, long start, long length) {
		if ((type != IFileSystem.SHARED_LOCK_TYPE)
				&& (type != IFileSystem.EXCLUSIVE_LOCK_TYPE)) {
			throw new IllegalArgumentException("Illegal lock type requested."); //$NON-NLS-1$
		}

		// Start position
		if (start < 0) {
			throw new IllegalArgumentException(
					"Lock start position must be non-negative"); //$NON-NLS-1$
		}

		// Length of lock stretch
		if (length < 0) {
			throw new IllegalArgumentException(
					"Lock length must be non-negative"); //$NON-NLS-1$
		}
	}

	private native int lockImpl(long fileDescriptor, long start, long length,
			int type, boolean wait);

	public native int getPageSize();

	public boolean lock(long fileDescriptor, long start, long length, int type,
			boolean waitFlag) throws IOException {
		// Validate arguments
		validateLockArgs(type, start, length);
		int result = lockImpl(fileDescriptor, start, length, type, waitFlag);
		return result != -1;
	}

	private native int unlockImpl(long fileDescriptor, long start, long length);

	public void unlock(long fileDescriptor, long start, long length)
			throws IOException {
		// Validate arguments
		validateLockArgs(IFileSystem.SHARED_LOCK_TYPE, start, length);
		int result = unlockImpl(fileDescriptor, start, length);
		if (result == -1) {
			throw new IOException();
		}
	}

	private native int fflushImpl(long fd, boolean metadata);

	public void fflush(long fileDescriptor, boolean metadata)
			throws IOException {
		int result = fflushImpl(fileDescriptor, metadata);
		if (result == -1) {
			throw new IOException();
		}
	}

	/*
	 * File position seeking.
	 */

	private native long seekImpl(long fd, long offset, int whence);

	public long seek(long fileDescriptor, long offset, int whence)
			throws IOException {
		long pos = seekImpl(fileDescriptor, offset, whence);
		if (pos == -1) {
			throw new IOException();
		}
		return pos;
	}

	/*
	 * Direct read/write APIs work on addresses.
	 */
	private native long readDirectImpl(long fileDescriptor, long address,
			int offset, int length);

	public long readDirect(long fileDescriptor, long address, int offset,
			int length) throws IOException {
		long bytesRead = readDirectImpl(fileDescriptor, address, offset, length);
		if (bytesRead < -1) {
			throw new IOException();
		}
		return bytesRead;
	}

	private native long writeDirectImpl(long fileDescriptor, long address,
			int offset, int length);

	public long writeDirect(long fileDescriptor, long address, int offset,
			int length) throws IOException {
		long bytesWritten = writeDirectImpl(fileDescriptor, address, offset,
				length);
		if (bytesWritten < 0) {
			throw new IOException();
		}
		return bytesWritten;
	}

	/*
	 * Indirect read/writes work on byte[]'s
	 */
	private native long readImpl(long fileDescriptor, byte[] bytes, int offset,
			int length);

	public long read(long fileDescriptor, byte[] bytes, int offset, int length)
			throws IOException {
		long bytesRead = readImpl(fileDescriptor, bytes, offset, length);
		if (bytesRead < -1) {
			throw new IOException();
		}
		return bytesRead;
	}

	private native long writeImpl(long fileDescriptor, byte[] bytes,
			int offset, int length);

	public long write(long fileDescriptor, byte[] bytes, int offset, int length)
			throws IOException {
		long bytesWritten = writeImpl(fileDescriptor, bytes, offset, length);
		if (bytesWritten < 0) {
			throw new IOException();
		}
		return bytesWritten;
	}

	/*
	 * Scatter/gather calls.
	 */
	public long readv(long fileDescriptor, long[] addresses, int[] offsets,
			int[] lengths, int size) throws IOException {
		long bytesRead = readvImpl(fileDescriptor, addresses, offsets, lengths,
				size);
		if (bytesRead < -1) {
			throw new IOException();
		}
		return bytesRead;
	}

	private native long readvImpl(long fileDescriptor, long[] addresses,
			int[] offsets, int[] lengths, int size);

	public long writev(long fileDescriptor, long[] addresses, int[] offsets,
			int[] lengths, int size) throws IOException {
		long bytesWritten = writevImpl(fileDescriptor, addresses, offsets,
				lengths, size);
		if (bytesWritten < 0) {
			throw new IOException();
		}
		return bytesWritten;
	}

	private native long writevImpl(long fileDescriptor, long[] addresses,
			int[] offsets, int[] lengths, int size);

	private native int closeImpl(long fileDescriptor);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.harmony.luni.platform.IFileSystem#close(long)
	 */
	public void close(long fileDescriptor) throws IOException {
		int rc = closeImpl(fileDescriptor);
		if (rc == -1) {
			throw new IOException();
		}
	}

	public void truncate(long fileDescriptor, long size) throws IOException {
		int rc = truncateImpl(fileDescriptor, size);
		if (rc < 0) {
			throw new IOException();
		}
	}

	private native int truncateImpl(long fileDescriptor, long size);

	public long open(byte[] fileName, int mode) throws FileNotFoundException {
		if (fileName == null) {
			throw new NullPointerException();
		}
		long handler = openImpl(fileName, mode);
		if (handler < 0) {
			throw new FileNotFoundException();
		}
		return handler;
	}

	private native long openImpl(byte[] fileName, int mode);

	public long transfer(long fileHandler, FileDescriptor socketDescriptor,
			long offset, long count) throws IOException {
		long result = transferImpl(fileHandler, socketDescriptor, offset, count);
		if (result < 0)
			throw new IOException();
		return result;
	}

	private native long transferImpl(long fileHandler,
			FileDescriptor socketDescriptor, long offset, long count);

}
