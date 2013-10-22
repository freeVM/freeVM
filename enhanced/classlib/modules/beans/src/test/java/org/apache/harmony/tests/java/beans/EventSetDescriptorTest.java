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
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.2.6.3 $
 */
package org.apache.harmony.tests.java.beans;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.beans.EventSetDescriptor;
import org.apache.harmony.tests.java.beans.auxiliary.OtherBean;
import org.apache.harmony.tests.java.beans.auxiliary.SampleListener;

/**
 * The test checks the class java.beans.EventSetDescriptor
 * @author Maxim V. Berkultsev
 * @version $Revision: 1.2.6.3 $
 */

public class EventSetDescriptorTest extends TestCase {
    
    /**
     * 
     */
    public EventSetDescriptorTest() {
        super();
    }
    
    /**
     * @param name
     */
    public EventSetDescriptorTest(String name) {
        super(name);
    }
    
    /**
     * The test checks the constructor
     */
    public void testEventSetDescriptorConstructor() {
        try {
            new EventSetDescriptor(OtherBean.class, "sample",
                    SampleListener.class, "fireSampleEvent");
        } catch (Exception e) {
            fail("Exception of " + e.getClass() + 
                " class with message " + e.getMessage() + " is thrown");
        }
        
    }
    
    /**
     * 
     */
    public static Test suite() {
        return new TestSuite(EventSetDescriptorTest.class);
    }
    
    /**
     * 
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
