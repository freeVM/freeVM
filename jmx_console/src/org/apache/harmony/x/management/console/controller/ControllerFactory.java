/* Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
package org.apache.harmony.x.management.console.controller;


/**
 * This class is used to get Controller's implementation. 
 * Right now it's always ControllerImpl instance. 
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class ControllerFactory {

    /**
     * Get Controller reference.
     * 
     * @return Controller reference.
     */
    public static Controller getController() {
        return ControllerImpl.getController();
    }
}
