/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.swing.tests.javax.swing.text.html.parser;

import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import junit.framework.TestCase;

public class DTDTest extends TestCase {
    private DTD dtd;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dtd = DTD.getDTD("testDTD");
    }

    public void testGetElementLowerOutOfBounds() {
        try {
            Element e = dtd.getElement(-1);
            fail("IndexOutOfBoundsException wasn't raised as RI, but method returned: " + e);
        } catch (IndexOutOfBoundsException e) {
        } catch (Exception e) {
            fail(e.getClass().getName()
                    + " raised but IndexOutOfBoundsException had to be raised");
        }
    }

    public void testGetElementUpperOutOfBounds() {
        try {
            Element e = dtd.getElement(dtd.elements.size());
            fail("IndexOutOfBoundsException didn't raised as RI, but method returned: " + e);
        } catch (IndexOutOfBoundsException e) {
        } catch (Exception e) {
            fail(e.getClass().getName()
                    + " raised but IndexOutOfBoundsException had to be raised");
        }
    }
}
