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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security.cert;

import java.util.Collection;
import java.util.Set;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public abstract class PKIXCertPathChecker implements Cloneable {

    /**
     * @com.intel.drl.spec_ref
     */
    protected PKIXCertPathChecker() {}

    /**
     * @com.intel.drl.spec_ref
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void init(boolean forward)
        throws CertPathValidatorException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract boolean isForwardCheckingSupported();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Set getSupportedExtensions();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void check(Certificate cert, Collection unresolvedCritExts)
        throws CertPathValidatorException;
}
