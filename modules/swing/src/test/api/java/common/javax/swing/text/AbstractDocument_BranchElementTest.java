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
package javax.swing.text;

import java.util.Enumeration;

import javax.swing.BasicSwingTestCase;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AbstractDocument.LeafElement;

/**
 * Tests AbstractDocument.BranchElement class.
 *
 */
public class AbstractDocument_BranchElementTest extends BasicSwingTestCase {
    protected AbstractDocument doc;
    protected BranchElement    bidi;
    protected BranchElement    par;
    protected Element      leaf1;
    protected Element      leaf2;
    protected Element      leaf3;

    protected AttributeSet[]   as;

    protected static final String RTL = "\u05DC\u05DD";
    protected static final String LTR = "\u0061\u0062";


    protected void setUp() throws Exception {
        super.setUp();

        StyleContextTest.sc = StyleContext.getDefaultStyleContext();
        as = new AttributeSet[] {
            StyleContextTest.addAttribute(1),
            StyleContextTest.addAttribute(2),
            StyleContextTest.addAttribute(null, 3, 2)
        };

        doc = new PlainDocument();
        doc.insertString(0, LTR + RTL + LTR + RTL + "\n01234", as[0]);

        bidi  = (BranchElement)doc.getBidiRootElement();
        leaf1 = (LeafElement)bidi.getElement(0).getElement(0);
        par   = (BranchElement)doc.getDefaultRootElement();
        leaf2 = (LeafElement)par.getElement(0);
        leaf3 = (LeafElement)par.getElement(1);
    }

    public void testGetElement() {
        if (BasicSwingTestCase.isHarmony()) {
            assertNull(par.getElement(-1));
        }
        assertEquals(leaf2, par.getElement(0));
        assertEquals(leaf3, par.getElement(1));
        assertNull(par.getElement(2));
    }


    public void testChildren() {
        Enumeration elements = par.children();
        Element[] children = new Element[] {
            par.getElement(0),
            par.getElement(1)
        };
        int index = 0;
        while (elements.hasMoreElements()) {
            Object child = elements.nextElement();
            assertSame(children[index++], child);
        }
    }

    public void testGetName() {
        assertEquals("bidi root", bidi.getName());
        assertEquals("paragraph", par.getName());
        assertSame(AbstractDocument.ParagraphElementName, par.getName());
    }

    /**
     * Test getElementIndex with default set of elements.
     */
    public void testGetElementIndex01() {
        assertEquals(0, par.getElementIndex(-1));
        assertEquals(0, par.getElementIndex(7));
        assertEquals(0, par.getElementIndex(8));
        assertEquals(1, par.getElementIndex(9));
        assertEquals(1, par.getElementIndex(10));
        assertEquals(1, par.getElementIndex(11));
        assertEquals(1, par.getElementIndex(20));
    }

    /**
     * Test getElementIndex behavior if some elements are zero-length,
     * i.e. start and end offsets are the same.
     */
    public void testGetElementIndex02() {
        BranchElement root = doc.new BranchElement(null, null);
        LeafElement[] leaves = {
            doc.new LeafElement(root, null, 0, 0), // [0]
            doc.new LeafElement(root, null, 0, 1), // [1]
            doc.new LeafElement(root, null, 0, 1), // [2]
            doc.new LeafElement(root, null, 1, 1), // [3]
            doc.new LeafElement(root, null, 1, 1), // [4]
            doc.new LeafElement(root, null, 1, 2), // [5]
            doc.new LeafElement(root, null, 2, 3)  // [6]
        };
        root.replace(0, 0, leaves);
        assertEquals(0, root.getElementIndex(-1));
        assertEquals(1, root.getElementIndex(0));
        assertEquals(5 /*2*/, root.getElementIndex(1));
        assertEquals(6, root.getElementIndex(2));
        assertEquals(6, root.getElementIndex(3));
        assertEquals(6, root.getElementIndex(4));
    }

    /**
     * Tests getElementIndex behavior when there are no children in the
     * BranchElement.
     */
    public void testGetElementIndex03() {
        BranchElement root = doc.new BranchElement(null, null);
        try {
            assertEquals(-1, root.getElementIndex(-1));

            if (!BasicSwingTestCase.isHarmony()) {
                fail("NullPointerException should be thrown");
            }
        } catch (NullPointerException e) { }

        try {
            assertEquals(-1, root.getElementIndex(0));

            if (!BasicSwingTestCase.isHarmony()) {
                fail("NullPointerException should be thrown");
            }
        } catch (NullPointerException e) { }
    }

    /**
     * Tests getElementIndex behavior when there are "gaps" between children.
     * The document has default length.
     */
    public void testGetElementIndex04() throws BadLocationException {
        final Element[] leaves = new Element[] {
            createLeaf(1, 2),
            createLeaf(3, 5),
            createLeaf(5, 8),
            createLeaf(15, 20)
        };
        assertEquals(14, doc.getLength());
        par.replace(0, par.getElementCount(), leaves);

        final int[] indexes = new int[] {
            0, 0, 0, 0, 1,   // [ 0] - [ 4]
            1, 1, 2, 2, 2,   // [ 5] - [ 9]
            3, 3, 3, 3, 3,   // [10] - [14]
            3, 3, 3, 3, 3,   // [15] - [19]
            3, 3, 3, 3, 3};  // [20] - [24]
        for (int offset = -2, i = 0; offset < 23; offset++, i++) {
            assertEquals("offset = " + offset + ", i = " + i,
                         indexes[i], par.getElementIndex(offset));
        }
    }

    /**
     * Tests getElementIndex behavior when there are "gaps" between children.
     * The document has zero length.
     */
    public void testGetElementIndex05() throws BadLocationException {
        doc.getContent().remove(0, doc.getLength());
        assertEquals(0, doc.getLength());
        final Element[] leaves = new Element[] {
            createLeaf(1, 2),
            createLeaf(3, 5),
            createLeaf(5, 8),
            createLeaf(15, 20)
        };
        par.replace(0, par.getElementCount(), leaves);

        final int[] indexes = new int[] {
            0, 0, 0, 0, 1,   // [ 0] - [ 4]
            1, 1, 2, 2, 2,   // [ 5] - [ 9]
            3, 3, 3, 3, 3,   // [10] - [14]
            3, 3, 3, 3, 3,   // [15] - [19]
            3, 3, 3, 3, 3};  // [20] - [24]
        for (int offset = -2, i = 0; offset < 23; offset++, i++) {
            assertEquals("offset = " + offset + ", i = " + i,
                         indexes[i], par.getElementIndex(offset));
        }
    }

    public void testIsLeaf() {
        assertFalse(bidi.isLeaf());
        assertFalse(par.isLeaf());
    }

    public void testGetAllowsChildren() {
        assertTrue(bidi.getAllowsChildren());
        assertTrue(par.getAllowsChildren());
    }

    public void testGetStartOffset() {
        assertEquals(0, bidi.getStartOffset());
        assertEquals(0, par.getStartOffset());
    }

    public void testGetEndOffset() {
        assertEquals(15, bidi.getEndOffset());
        assertEquals(15, par.getEndOffset());
    }

    public void testGetElementCount() {
        assertEquals(5, bidi.getElementCount());
        assertEquals(2, par.getElementCount());
    }

    public void testBranchElement() {
        doc.writeLock();
        bidi = doc.new BranchElement(par, as[2]);
        doc.writeUnlock();
        assertNotSame(as[2], bidi.getAttributes());
        assertEquals(as[2], bidi.getAttributes());
        assertSame(par, bidi.getParentElement());
        assertEquals(0, bidi.getElementCount());
        assertNull(bidi.getElement(0));
        Enumeration elements = bidi.children();
        assertNull(elements);
    }

    /**
     * Generic checks.
     */
    public void testReplace01() {
        assertEquals(5, bidi.getElementCount());
        bidi.replace(0, bidi.getElementCount(), new Element[] {});
        assertEquals(0, bidi.getElementCount());
        assertNull(bidi.children());

        bidi.replace(0, 0, new Element[] {leaf1, leaf2, leaf3});
        assertEquals(3, bidi.getElementCount());

        bidi = doc.new BranchElement(null, null);
        assertEquals(0, bidi.getElementCount());
        bidi.replace(0, 0, new Element[] {leaf2});
    }

    /**
     * Copy checks.
     */
    public void testReplace02() {
        assertEquals(5, bidi.getElementCount());
        Element[] copy = new Element[] {
                bidi.getElement(0),
                bidi.getElement(1),
                bidi.getElement(2),
                bidi.getElement(3),
                bidi.getElement(4),
        };
        bidi.replace(1, 3, new Element[] {leaf2});

        assertEquals(3, bidi.getElementCount());
        assertSame(copy[0], bidi.getElement(0));
        assertSame(leaf2,   bidi.getElement(1));
        assertSame(copy[4], bidi.getElement(2));
    }

    /**
     * Replace with null.
     */
    public void testReplace03() throws Exception {
        assertEquals(5, bidi.getElementCount());
        try {
            bidi.replace(0, 2, null);

            fail("NPE exception expected");
        } catch (NullPointerException e) { }

        /*
        // When NPE isn't thrown, the following assertion must be true
        assertEquals(3, bidi.getElementCount());
        */
    }

    public void testPositionToElement() {
        assertNull(par.positionToElement(-1));
        assertSame(leaf2, par.positionToElement(7));
        assertSame(leaf2, par.positionToElement(8));
        assertSame(leaf3, par.positionToElement(9));
        assertSame(leaf3, par.positionToElement(10));
        assertSame(leaf3, par.positionToElement(11));
        assertNull(par.positionToElement(20));
    }

    public void testToString() {
        assertEquals("BranchElement(bidi root) 0,15\n", bidi.toString());
        assertEquals("BranchElement(paragraph) 0,15\n", par.toString());
    }

    private Element createLeaf(final int start, final int end) {
        return doc.new LeafElement(par, null, start, end);
    }

}
