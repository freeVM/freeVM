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

package javax.crypto.interfaces;

import javax.crypto.SecretKey;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public interface PBEKey extends SecretKey {

    /**
     * @com.intel.drl.spec_ref
     * @serial
     */
    public static final long serialVersionUID = -1430015993304333921L;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getIterationCount();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public byte[] getSalt();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public char[] getPassword();

}