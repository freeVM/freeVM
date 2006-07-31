/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Alexey A. Ivanov
 * @version $Revision$
 */
package javax.swing.text.html;

import javax.swing.BasicSwingTestCase;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS.Attribute;

import junit.framework.TestCase;

public class StyleSheet_ConvertAttr_FloatTest extends TestCase {
    private StyleSheet ss;
    private MutableAttributeSet simple;
    private Object cssValue;

    protected void setUp() throws Exception {
        super.setUp();
        ss = new StyleSheet();
        simple = new SimpleAttributeSet();
    }

    public void testFloatNone() {
        ss.addCSSAttribute(simple, Attribute.FLOAT, "none");
        cssValue = simple.getAttribute(Attribute.FLOAT);
        assertEquals("none", cssValue.toString());
    }

    public void testFloatLeft() {
        ss.addCSSAttribute(simple, Attribute.FLOAT, "left");
        cssValue = simple.getAttribute(Attribute.FLOAT);
        assertEquals("left", cssValue.toString());
    }

    public void testFloatRight() {
        ss.addCSSAttribute(simple, Attribute.FLOAT, "right");
        cssValue = simple.getAttribute(Attribute.FLOAT);
        assertEquals("right", cssValue.toString());
    }

    public void testFloatInvalid() {
        ss.addCSSAttribute(simple, Attribute.FLOAT, "top");
        cssValue = simple.getAttribute(Attribute.FLOAT);
        if (BasicSwingTestCase.isHarmony()) {
            assertEquals(0, simple.getAttributeCount());
            assertNull(cssValue);
        } else {
            assertEquals(1, simple.getAttributeCount());
            assertEquals("top", cssValue.toString());
        }
    }
}
