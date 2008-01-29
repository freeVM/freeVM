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
package org.apache.harmony.pack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CPUTF8 extends ConstantPoolEntry {
	private String utf8;

	public CPUTF8(String utf8, int domain) {
		super(ConstantPoolEntry.CP_UTF8);
		this.utf8 = utf8;
		this.domain = domain;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final CPUTF8 other = (CPUTF8) obj;
		if (utf8 == null) {
			if (other.utf8 != null)
				return false;
		} else if (!utf8.equals(other.utf8))
			return false;
		return true;
	}
	
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((utf8 == null) ? 0 : utf8.hashCode());
		return result;
	}

	public String toString() {
		return "UTF8: " + utf8;
	}

	protected void writeBody(DataOutputStream dos) throws IOException {
		byte[] bytes;
		try {
			// TODO Check that this is the right UTF-8 for bytes
			if (utf8 == null) {
				bytes = new byte[0];
			} else {
				bytes = utf8.getBytes("UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Couldn't convert string " + utf8
					+ " to UTF-8");
		}
		dos.writeShort(bytes.length);
		dos.write(bytes);
	}

	public String underlyingString() {
	    return utf8;
	}
	
	public String comparisonString() {
	    // Should use either normalComparisonString or signatureComparisonString.
	    // If we get here, that might indicate an error.
	    throw new Error("Should use specific kinds of comparisonString() on CPUTF8s");
	}

	public String normalComparisonString() {
	   // TODO: what to do about inner classes?
	   if(utf8==null) {return "null:utf8 (probably an inner class?)";};
        return utf8;
    }

    public String signatureComparisonString() {
        // TODO: what to do about inner classes?
        if(utf8==null) {return "null:utf8 (probably an inner class?)";};
        StringBuffer alphaChars = new StringBuffer();
        StringBuffer extraChars = new StringBuffer();
        if(utf8.length() > 0){
            if(utf8.charAt(0) == '(') {
                // Things with return values (which apparently
                // always begin with '(') sort after things
                // without return values.
                // TODO: need a better way for this - possibly in the comparator?
                alphaChars.append(Character.MAX_VALUE);
            }
        }
        // TODO: need a better way for this - possibly in the comparator?
        extraChars.append(Character.MAX_VALUE);
        for(int index=0; index < utf8.length(); index++) {
            if( (utf8.charAt(index) == '(') || (utf8.charAt(index) == ')') || (utf8.charAt(index) == '[') || (utf8.charAt(index) == ']') ) {
                extraChars.append(utf8.charAt(index));
            } else {
                alphaChars.append(utf8.charAt(index));
            }
        }
        return(alphaChars.toString() + extraChars.toString());
    }
}