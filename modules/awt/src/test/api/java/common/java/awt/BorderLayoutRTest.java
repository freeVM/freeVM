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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import junit.framework.TestCase;

public class BorderLayoutRTest extends TestCase {
    Container emptyContainer;
    Dimension defSize;
    BorderLayout layout;
    private Dimension maxSize;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(BorderLayoutRTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        emptyContainer = new Container();
        defSize = new Dimension();
        maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        layout = new BorderLayout();
    }
    
    public final void testGetLayoutAlignmentX1() {
        try {
            layout.getLayoutAlignmentX(null);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testGetLayoutAlignmentX2() {        
        try {
            layout.getLayoutAlignmentX(emptyContainer);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testGetLayoutAlignmentY1() {
        try {
            layout.getLayoutAlignmentY(null);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testGetLayoutAlignmentY2() {        
        try {
            layout.getLayoutAlignmentY(emptyContainer);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testInvalidateLayout1() {
        try {
            layout.invalidateLayout(null);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testInvalidateLayout2() {        
        try {
            layout.invalidateLayout(emptyContainer);
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testMaximumLayoutSize1() {
        try {
            assertEquals(maxSize, layout.maximumLayoutSize(null));
        } catch (Throwable t) {
            fail();
        }        
    }
    
    public final void testMaximumLayoutSize2() {        
        try {
            assertEquals(maxSize, layout.maximumLayoutSize(emptyContainer));
        } catch (Throwable t) {
            fail();
        }        
    }


    public final void testMinimumLayoutSize() {
        assertEquals(defSize, layout.minimumLayoutSize(emptyContainer));
    }

    public final void testPreferredLayoutSize() {
        assertEquals(defSize, layout.preferredLayoutSize(emptyContainer));
    }   
    
    
}
