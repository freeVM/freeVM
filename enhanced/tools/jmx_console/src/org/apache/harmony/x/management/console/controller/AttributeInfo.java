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

import javax.management.Attribute;


/**
 * 
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class AttributeInfo extends Attribute {

    /**
     * Serial version UID for serialization purposes.
     */
    private static final long serialVersionUID = 3562800810589989580L;
    
    /**
     * 
     */
    private boolean readable;
    
    /**
     * 
     */
    private boolean writable;
   
    /**
     * 
     */
    private String type;
    
    /**
     * 
     * @param attName
     * @param attValue
     * @param info
     */
    public AttributeInfo(String attName, Object attValue, boolean readable,
            boolean writable, String type) {
        
        super(attName, attValue);
        this.readable = readable;
        this.writable = writable;
        this.type = type;
    }
    
    /**
     * 
     * @return
     */
    public boolean isReadable() {
        return this.readable;
    }
    
    /**
     * 
     * @return
     */
    public boolean isWritable() {
        return this.writable;
    }
    
    /**
     * 
     * @return
     */
    public String getType() {
        return this.type;
    }
}
