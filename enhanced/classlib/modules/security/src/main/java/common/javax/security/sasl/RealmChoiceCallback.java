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

package javax.security.sasl;

import javax.security.auth.callback.ChoiceCallback;

/**
 * @com.intel.drl.spec_ref
 * 
 */

public class RealmChoiceCallback extends ChoiceCallback {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -8588141348846281332L;

    /**
     * @com.intel.drl.spec_ref
     */
    public RealmChoiceCallback(String prompt, String[] choices,
            int defaultChoice, boolean multiple) {
        super(prompt, choices, defaultChoice, multiple);
    }
}