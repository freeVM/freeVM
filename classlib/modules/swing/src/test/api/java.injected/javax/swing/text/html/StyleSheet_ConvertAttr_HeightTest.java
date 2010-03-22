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
/**
 * @author Alexey A. Ivanov
 */
package javax.swing.text.html;

import javax.swing.BasicSwingTestCase;

public class StyleSheet_ConvertAttr_HeightTest
    extends StyleSheet_ConvertAttr_WidthTest {

    protected void setUp() throws Exception {
        super.setUp();
        cssKey = CSS.Attribute.HEIGHT;
        percentageValuesInvalid = true;
    }

    public void testLength11_1Percent() {
        percentageValuesInvalid = BasicSwingTestCase.isHarmony();
        super.testLength11_1Percent();
    }

    public void testLengthPlus11_1Percent() {
        percentageValuesInvalid = BasicSwingTestCase.isHarmony();
        super.testLengthPlus11_1Percent();
    }

    public void testLengthMinus11_1Percent() {
        percentageValuesInvalid = BasicSwingTestCase.isHarmony();
        super.testLengthMinus11_1Percent();
    }
}
