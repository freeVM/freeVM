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
package org.apache.harmony.archive.internal.pack200;

import java.io.IOException;
import java.io.InputStream;

import org.apache.harmony.archive.internal.nls.Messages;

/**
 * A run codec is a grouping of two nested codecs; K values are decoded from
 * the first codec, and the remaining codes are decoded from the remaining
 * codec. Note that since this codec maintains state, the instances are
 * not reusable.
 *
 * @author Alex Blewitt
 * @version $Revision: $
 */
public class RunCodec extends Codec {
	private int k;
	private Codec aCodec;
	private Codec bCodec;
	private long last;

	public RunCodec(int k, Codec aCodec, Codec bCodec) throws Pack200Exception {
		if (k <= 0)
			throw new Pack200Exception(Messages.getString("archive.12")); //$NON-NLS-1$
		if (aCodec == null || bCodec == null)
			throw new Pack200Exception(Messages.getString("archive.13")); //$NON-NLS-1$
		this.k = k;
		this.aCodec = aCodec;
		this.bCodec = bCodec;
	}
    
	@Override
    public long decode(InputStream in) throws IOException, Pack200Exception {
		return decode(in,this.last);
	}

	@Override
    public long decode(InputStream in, long last) throws IOException, Pack200Exception {
		if(--k>=0) {
			long value = aCodec.decode(in,last);
			this.last = (k == 0 ? 0 : value);
			return value;
		}
        this.last = bCodec.decode(in,last);
        return this.last;
	}
    
	@Override
    public String toString() {
        return "RunCodec[k="+k+";aCodec="+aCodec+"bCodec="+bCodec+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
