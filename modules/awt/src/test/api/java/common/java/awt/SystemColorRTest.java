/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Pavel Dolgov
 * @version $Revision$
 */
package java.awt;

import junit.framework.TestCase;

public class SystemColorRTest extends TestCase {

    public void testGetRGBComponents() {
        checkRGBComponents(SystemColor.text);
        checkRGBComponents(SystemColor.window);
        checkRGBComponents(SystemColor.control);
        checkRGBComponents(SystemColor.textHighlight);
    }

    private void checkRGBComponents(SystemColor color) {
        int r = (color.getRGB() >> 16) & 0xFF;;
        int g = (color.getRGB() >> 8) & 0xFF;
        int b = color.getRGB() & 0xFF;
        assertEquals(color.getRed(), r);
        assertEquals(color.getGreen(), g);
        assertEquals(color.getBlue(), b);
    }
}
