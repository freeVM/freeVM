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

import java.util.Enumeration;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public interface SSLSessionContext {
    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Enumeration getIds();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public SSLSession getSession(byte[] sessionId);

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getSessionCacheSize();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getSessionTimeout();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void setSessionCacheSize(int size) throws IllegalArgumentException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void setSessionTimeout(int seconds) throws IllegalArgumentException;

}