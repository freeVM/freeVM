/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
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

package java.sql;

import java.io.Serializable;

/**
 * An exception class that holds information about Database access warnings.
 */
public class SQLWarning extends SQLException implements Serializable {

    private static final long serialVersionUID = 3917336774604784856L;

    private SQLWarning chainedWarning = null;

    /**
     * Creates an SQLWarning object. The Reason string is set to null, the
     * SQLState string is set to null and the Error Code is set to 0.
     */
    public SQLWarning() {
        super();
    }

    /**
     * Creates an SQLWarning object. The Reason string is set to the given
     * reason string, the SQLState string is set to null and the Error Code is
     * set to 0.
     * 
     * @param theReason
     */
    public SQLWarning(String theReason) {
        super(theReason);
    }

    /**
     * Creates an SQLWarning object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string
     * and the Error Code is set to 0.
     * 
     * @param theReason
     *            the string to use as the Reason string
     * @param theSQLState
     *            the string to use as the SQLState string
     */
    public SQLWarning(String theReason, String theSQLState) {
        super(theReason, theSQLState);
    }

    /**
     * Creates an SQLWarning object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string
     * and the Error Code is set to the given ErrorCode value.
     * 
     * @param theReason
     * @param theSQLState
     * @param theErrorCode
     */
    public SQLWarning(String theReason, String theSQLState, int theErrorCode) {
        super(theReason, theSQLState, theErrorCode);
    }

    /**
     * Gets the SQLWarning chained to this SQLWarning object.
     * 
     * @return the SQLWarning chained to this SQLWarning. null if no SQLWarning
     *         is chained to this SQLWarning.
     */
    public SQLWarning getNextWarning() {
        return chainedWarning;
    }

    /**
     * Chains a supplied SQLWarning to this SQLWarning.
     * 
     * @param w
     *            the SQLWarning to chain to this SQLWarning.
     */
    public void setNextWarning(SQLWarning w) {
        chainedWarning = w;
        return;
    }
}
