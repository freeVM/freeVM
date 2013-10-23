/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * 
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class ControllerOperationException extends Exception {
    
    /**
     * Serial version UID for serialization purposes.
     */
    private static final long serialVersionUID = 2339143132091711307L;
    
    /**
     * Construct this object.
     */
    public ControllerOperationException() {
        super();
    }

    /**
     * Construct this object with message. 
     * 
     * @param msg The message.
     */
    public ControllerOperationException(String msg) {
        super(msg);
    }
}