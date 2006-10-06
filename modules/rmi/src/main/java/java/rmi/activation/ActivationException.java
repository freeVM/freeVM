/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author  Victor A. Martynov
 * @version $Revision: 1.7.4.2 $
 */
package java.rmi.activation;


/**
 * @com.intel.drl.spec_ref
 *
 * @author  Victor A. Martynov
 * @version $Revision: 1.7.4.2 $
 */
public class ActivationException extends Exception {

    private static final long serialVersionUID = -4320118837291406071l;

    public Throwable detail;

    /**
     * @com.intel.drl.spec_ref
     */
    public ActivationException(String message, Throwable detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ActivationException(String message) {
        super(message);
        this.detail = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ActivationException() {
        super();
        this.detail = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Throwable getCause() {
        return detail;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getMessage() {
        return super.getMessage() + "[detail throwable = " + detail + "] "; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
