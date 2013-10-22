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
* @author Maxim V. Makarov
* @version $Revision$
*/

package javax.security.auth.login;

/**
 * @com.intel.drl.spec_ref
 *
 */
public class FailedLoginException extends LoginException {

    /**
	 * @com.intel.drl.spec_ref
	 */
    private static final long serialVersionUID = 802556922354616286L;
    
    /**
     * @com.intel.drl.spec_ref
     */
    public FailedLoginException() {
        super();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public FailedLoginException(String message) {
        super(message);
    }

}