/*
    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
*/
/*
 * Created on 22.11.2004
 */

package org.apache.harmony.vmtt.ir.attributes;

/**
 * @author agolubit
 */

public class General extends Attribute {

	private byte[] bytes = null;
	
	public boolean isGeneral() {
		return true;
	}
	
	public void setBytes(byte[] bs) {
		bytes = new byte[bs.length];
		System.arraycopy(bs, 0, bytes, 0, bs.length);
	}
	
	public byte byteAt(int index) {
		return bytes[index];
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public int getBytesLength() {
		return bytes.length;
	}
}
