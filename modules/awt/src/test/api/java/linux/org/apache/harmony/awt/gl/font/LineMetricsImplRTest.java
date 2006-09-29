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
 * @author Ilya S. Okomin
 * @version $Revision$
 */
package org.apache.harmony.awt.gl.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import junit.framework.TestCase;

public class LineMetricsImplRTest extends TestCase {
    
    public void testGetLineMetrics() {
            Font font = new Font("Dialog", Font.PLAIN, 12);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            final int count = 100;
        try {
            for (int i = 0; i < count; i++) {
                LineMetrics lm = font.getLineMetrics("", frc);
            }
        } catch (Exception e) {
            if (e.getMessage().indexOf("Error opening TrueType font file.") != -1){
                fail("NPE \"Error opening TrueType font file.\" caught.");
            }
        }
    }
}