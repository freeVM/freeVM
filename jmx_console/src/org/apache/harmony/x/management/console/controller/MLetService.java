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

import java.util.Collections;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * 
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class MLetService {

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
    public MLetService(MBeanServerConnection con, ObjectName on) {
        this.con = con;
        this.on = on;
    }
    
    /**
     * 
     * @param url
     * @return
     */
    public Set getMBeansFromURL(String url) {
        try {
            return (Set) con.invoke(on, "getMBeansFromURL", 
                new Object[] { url }, new String[] { String.class.getName() });
        } catch (Exception e) {
            return Collections.EMPTY_SET;
        }
    }
}