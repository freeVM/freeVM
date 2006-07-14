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


/**
 * The platform address class is an unsafe virtualization of an OS memory block.
 * 
 */
public final class PlatformAddress implements ICommonDataTypes, Comparable {

	/**
	 * This final field defines the size of an address on this platform.
	 */
	static final int SIZEOF = Platform.getMemorySystem().getPointerSize();

	/**
	 * NULL is the canonical address with address value zero.
	 */
	public static final PlatformAddress NULL = new PlatformAddress(0);

	public static final IMemorySpy memorySpy = new RuntimeMemorySpy();

	private static final IMemorySystem osMemory = Platform.getMemorySystem();

	private final long osaddr;

	private final boolean isMappedAddress;

	public static PlatformAddress on(PlatformAddress other) {
		return new PlatformAddress(other.osaddr, other.isMappedAddress);
	}

	public static PlatformAddress on(long value) {
		return (value == 0) ? NULL : new PlatformAddress(value, false);
	}

	public static PlatformAddress on(long value, boolean isMapped) {
		PlatformAddress addr = (value == 0) ? NULL : new PlatformAddress(value,
				isMapped);
		if (isMapped) {
			// FIXME: some platform need the size to unmap/free, fix it
			memorySpy.alloc(addr, 0);
		}
		return addr;
	}

    /**
     * Allocates a contiguous block of OS heap memory.
     * 
     * @param size The number of bytes to allocate from the system heap.
     * @return PlatformAddress representing the memory block.
     */
	public static PlatformAddress alloc(long size) {
		long osAddress = osMemory.malloc(size);
		PlatformAddress newMemory = PlatformAddress.on(osAddress);
		memorySpy.alloc(newMemory, size);
		return newMemory;
	}
    
    /**
     * Allocates a contiguous block of OS heap memory and initializes it to
     * a given value.
     * 
     * @param size The number of bytes to allocate from the system heap.
     * @param init The value to initialize the memory.
     * @return PlatformAddress representing the memory block.
     */
    public static PlatformAddress alloc(long size, byte init) {
        long osAddress = osMemory.malloc(size);
        osMemory.memset(osAddress, init, size);
        PlatformAddress newMemory = PlatformAddress.on(osAddress);
        memorySpy.alloc(newMemory, size);
        return newMemory;
    }

	public PlatformAddress(long address) {
		this(address, false);
	}

	public PlatformAddress(long address, boolean isMapped) {
		super();
		osaddr = address;
		isMappedAddress = isMapped;
	}

	/**
	 * Sending auto free to an address means that, when this subsystem has
	 * allocated the memory, it will automatically be freed when this object is
	 * collected by the garbage collector if the memory has not already been
	 * freed explicitly.
	 * 
	 */
	public void autoFree() {
		memorySpy.autoFree(this);
	}

	public PlatformAddress offsetBytes(int offset) {
		return PlatformAddress.on(osaddr + offset);
	}

	public PlatformAddress offsetBytes(long offset) {
		return PlatformAddress.on(osaddr + offset);
	}

	public void moveTo(PlatformAddress dest, long numBytes) {
		osMemory.memmove(dest.osaddr, osaddr, numBytes);
	}

	public boolean equals(Object other) {
		return (other instanceof PlatformAddress)
				&& (((PlatformAddress) other).osaddr == osaddr);
	}

	public int hashCode() {
		return (int) osaddr;
	}

	public boolean isNULL() {
		return this == NULL;
	}

	public void free() {
		// Memory spys can veto the basic free if they determine the memory was
		// not allocated.
		if (memorySpy.free(this)) {
			if (isMappedAddress) {
				unmap();
			}else{
			    osMemory.free(osaddr);
            }
		}
	}

	public void setAddress(int offset, PlatformAddress address) {
		osMemory.setAddress(osaddr + offset, address.osaddr);
	}

	public PlatformAddress getAddress(int offset) {
		int addr = getInt(offset);
		if (addr == 0) {
			return NULL;
		}
		return PlatformAddress.on(addr);
	}

	public void setByte(int offset, byte value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JBYTE);
		osMemory.setByte(osaddr + offset, value);
	}

	public void setByteArray(int offset, byte[] bytes, int bytesOffset,
			int length) {
		memorySpy.rangeCheck(this, offset, length * SIZEOF_JBYTE);
		osMemory.setByteArray(osaddr + offset, bytes, bytesOffset, length);
	}

	public byte getByte(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JBYTE);
		return osMemory.getByte(osaddr + offset);
	}

	public void getByteArray(int offset, byte[] bytes, int bytesOffset,
			int length) {
		memorySpy.rangeCheck(this, offset, length * SIZEOF_JBYTE);
		osMemory.getByteArray(osaddr + offset, bytes, bytesOffset, length);
	}

	public void setShort(int offset, short value, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
		osMemory.setShort(osaddr + offset, value, order);
	}

	public void setShort(int offset, short value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
		osMemory.setShort(osaddr + offset, value);
	}

	public short getShort(int offset, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
		return osMemory.getShort(osaddr + offset, order);
	}

	public short getShort(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JSHORT);
		return osMemory.getShort(osaddr + offset);
	}

	public void setInt(int offset, int value, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
		osMemory.setInt(osaddr + offset, value, order);
	}

	public void setInt(int offset, int value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
		osMemory.setInt(osaddr + offset, value);
	}

	public int getInt(int offset, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
		return osMemory.getInt(osaddr + offset, order);
	}

	public int getInt(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JINT);
		return osMemory.getInt(osaddr + offset);
	}

	public void setLong(int offset, long value, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
		osMemory.setLong(osaddr + offset, value, order);
	}

	public void setLong(int offset, long value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
		osMemory.setLong(osaddr + offset, value);
	}

	public long getLong(int offset, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
		return osMemory.getLong(osaddr + offset, order);
	}

	public long getLong(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JLONG);
		return osMemory.getLong(osaddr + offset);
	}

	public void setFloat(int offset, float value, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
		osMemory.setFloat(osaddr + offset, value, order);
	}

	public void setFloat(int offset, float value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
		osMemory.setFloat(osaddr + offset, value);
	}

	public float getFloat(int offset, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
		return osMemory.getFloat(osaddr + offset, order);
	}

	public float getFloat(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JFLOAT);
		return osMemory.getFloat(osaddr + offset);
	}

	public void setDouble(int offset, double value, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
		osMemory.setDouble(osaddr + offset, value, order);
	}

	public void setDouble(int offset, double value) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
		osMemory.setDouble(osaddr + offset, value);
	}

	public double getDouble(int offset, Endianness order) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
		return osMemory.getDouble(osaddr + offset, order);
	}

	public double getDouble(int offset) {
		memorySpy.rangeCheck(this, offset, SIZEOF_JDOUBLE);
		return osMemory.getDouble(osaddr + offset);
	}

	public void mmapLoad(long size) {
		memorySpy.rangeCheck(this, 0, (int) size * SIZEOF_JBYTE);
		osMemory.load(this, size);
	}

	public boolean mmapIsLoaded(long size) {
		memorySpy.rangeCheck(this, 0, (int) size * SIZEOF_JBYTE);
		return osMemory.isLoaded(this, size);
	}

	public void mmapFlush(long size) {
		memorySpy.rangeCheck(this, 0, (int) size * SIZEOF_JBYTE);
		osMemory.flush(this, size);
	}

	public void unmap() {
		osMemory.unmap(this);
	}

	public long toLong() {
		return osaddr;
	}

	public String toString() {
		return "PlatformAddress[" + osaddr + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int compareTo(Object other) {
		if (other == null) {
			throw new NullPointerException(); // per spec.
		}
		if (other instanceof PlatformAddress) {
			long otherPA = ((PlatformAddress) other).osaddr;
			if (osaddr == otherPA) {
				return 0;
			}
			return osaddr < otherPA ? -1 : 1;
		}

		throw new ClassCastException(); // per spec.
	}
}
