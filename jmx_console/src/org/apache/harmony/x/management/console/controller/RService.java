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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * 
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class RService {

    /**
     * A reference to MBeanServer.
     */
    private MBeanServerConnection con = null;
    
    /**
     * An ObjectName this MLetService is registered with.
     */
    private ObjectName on = null;
    
    /**
     * 
     * @param con
     * @param on
     */
    public RService(MBeanServerConnection con, ObjectName on) {
        this.con = con;
        this.on = on;
    }
    
    public void addRelation() {
        
    }
    
    public void addRelationType() {
        
    }
    
    public Object getAllRelations() {
        return null;
    }
    
    public Object getAllTypes() {
        return null;
    }
    
    public void removeRelation() {
        
    }
}
