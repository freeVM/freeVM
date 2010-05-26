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
 * @author Alexander T. Simbirtsev
 */
package javax.swing;

import java.awt.Frame;
import tests.support.Support_Excludes;

public class JOptionPaneRTest extends SwingTestCase {
    public JOptionPaneRTest(final String name) {
        super(name);
    }

    public void testGetFrameForComponent() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        final Frame f = new Frame();
        final JDialog dialog = new JDialog(f);
        assertSame(f, JOptionPane.getFrameForComponent(dialog));
    }
}
