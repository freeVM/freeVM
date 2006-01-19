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
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package java.security.serialization;

import java.security.PrivilegedActionException;

import org.apache.harmony.security.test.SerializationTest;


/**
 * Serialization testing for PrivilegedActionException.
 */

public class PrivilegedActionExceptionTest extends SerializationTest {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PrivilegedActionExceptionTest.class);
    }

    protected Object[] getData() {
        Exception ex = new Exception();
        PrivilegedActionException ex1 = new PrivilegedActionException(ex);
        return new PrivilegedActionException[] {
              new PrivilegedActionException(null),
              new PrivilegedActionException(ex),
              new PrivilegedActionException(ex1)
        };
    }
    
    protected void assertDeserialized(Object reference, Object otest) {
        PrivilegedActionException ref = (PrivilegedActionException)reference;
        PrivilegedActionException test = (PrivilegedActionException)otest;
        if( ref.getException() == null ) {
            assertNull( test.getException() );
        }
        else {
            // just be sure we've deserialized the right class
            assertSame(ref.getException().getClass(), test.getException().getClass() );
        }
    }
    

}
