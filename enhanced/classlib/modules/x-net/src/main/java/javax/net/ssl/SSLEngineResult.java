/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.net.ssl;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class SSLEngineResult {
    
    // Store Status object
    private final SSLEngineResult.Status status;

    // Store HandshakeStatus object
    private final SSLEngineResult.HandshakeStatus handshakeStatus;

    // Store bytesConsumed
    private final int bytesConsumed;

    // Store bytesProduced
    private final int bytesProduced;

    /**
     * @com.intel.drl.spec_ref
     */
    public SSLEngineResult(SSLEngineResult.Status status,
            SSLEngineResult.HandshakeStatus handshakeStatus, int bytesConsumed,
            int bytesProduced) {
        if (status == null) {
            throw new IllegalArgumentException("status is null");
        }
        if (handshakeStatus == null) {
            throw new IllegalArgumentException("handshakeStatus is null");
        }
        if (bytesConsumed < 0) {
            throw new IllegalArgumentException("bytesConsumed is negative");
        }
        if (bytesProduced < 0) {
            throw new IllegalArgumentException("bytesProduced is negative");
        }
        this.status = status;
        this.handshakeStatus = handshakeStatus;
        this.bytesConsumed = bytesConsumed;
        this.bytesProduced = bytesProduced;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final int bytesConsumed() {
        return bytesConsumed;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final int bytesProduced() {
        return bytesProduced;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SSLEngineReport: Status = ");
        sb.append(status.toString());
        sb.append("  HandshakeStatus = ");
        sb.append(handshakeStatus.toString());
        sb.append("\n                 bytesConsumed = ");
        sb.append(Integer.toString(bytesConsumed));
        sb.append(" bytesProduced = ");
        sb.append(Integer.toString(bytesProduced));
        return sb.toString();
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * FIXME: this is class which was introduced in 1.5 specification
     */
    /*
     * public static enum HandshakeStatus { 
     *     FINISHED, 
     *     NEED_TASK, 
     *     NEED_UNWRAP,
     *     NEED_WRAP, 
     *     NOT_HANDSHAKING;
     * }
     */
    
    /**
     * 
     * FIXME: Template for HandshakeStatus class
     *  
     */
    public static final class HandshakeStatus {
        public static final SSLEngineResult.HandshakeStatus NOT_HANDSHAKING = new HandshakeStatus();

        public static final SSLEngineResult.HandshakeStatus FINISHED = new HandshakeStatus();

        public static final SSLEngineResult.HandshakeStatus NEED_TASK = new HandshakeStatus();

        public static final SSLEngineResult.HandshakeStatus NEED_WRAP = new HandshakeStatus();

        public static final SSLEngineResult.HandshakeStatus NEED_UNWRAP = new HandshakeStatus();

        private static final HandshakeStatus[] values = { NOT_HANDSHAKING,
                FINISHED, NEED_TASK, NEED_WRAP, NEED_UNWRAP };

        private HandshakeStatus() {
        }

        public static SSLEngineResult.HandshakeStatus valueOf(String str) {
            if ("FINISHED".equals(str)) {
                return FINISHED;
            }
            if ("NEED_TASK".equals(str)) {
                return NEED_TASK;
            }
            if ("NEED_TASK".equals(str)) {
                return NEED_TASK;
            }
            if ("NEED_WRAP".equals(str)) {
                return NEED_WRAP;
            }
            if ("NEED_UNWRAP".equals(str)) {
                return NEED_UNWRAP;
            }
            return null;
        }

        public static final SSLEngineResult.HandshakeStatus[] values() {
            return values;
        }
    }

    /**
     * @com.intel.drl.spec_ref 
     * 
     * FIXME: this is class which was introduced in 1.5  specification
     */
    /*
     * public static enum Status {
     *     BUFFER_OVERFLOW,
     *     BUFFER_UNDERFLOW,
     *     CLOSED,
     *     OK;
     * }
     */
    /**
     * 
     * FIXME: Template for Status class
     */
    public static final class Status {
        public static final SSLEngineResult.Status BUFFER_UNDERFLOW = new Status();

        public static final SSLEngineResult.Status BUFFER_OVERFLOW = new Status();

        public static final SSLEngineResult.Status OK = new Status();

        public static final SSLEngineResult.Status CLOSED = new Status();

        private static final Status[] values = { BUFFER_UNDERFLOW,
                BUFFER_OVERFLOW, OK, CLOSED };

        private Status() {
        }

        public static SSLEngineResult.Status valueOf(String str) {
            if ("BUFFER_UNDERFLOW".equals(str)) {
                return BUFFER_UNDERFLOW;
            }
            if ("BUFFER_OVERFLOW".equals(str)) {
                return BUFFER_OVERFLOW;
            }
            if ("OK".equals(str)) {
                return OK;
            }
            if ("CLOSED".equals(str)) {
                return CLOSED;
            }
            return null;
        }

        public static final SSLEngineResult.Status[] values() {
            return values;
        }
    }
}