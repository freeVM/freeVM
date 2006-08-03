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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests DefaultStyledDocument class. This test checks that
 * ElementBuffer calls AbstractDocument.create{Branch,Leaf}Element methods
 * to actually create elements.
 *
 * Methods to modify document structure are: insertString, remove,
 * set{Character,Paragraph}Attributes, setLogicalStyle.
 *
 */
public class DefaultStyledDocument_CreateElementTest extends TestCase {
    private static class LeafChild {
        public final Element parent;
        public final AttributeSet attrs;
        public final int start;
        public final int end;
        public final Element child;

        public LeafChild(Element parent, AttributeSet attrs,
                         int start, int end, Element child) {
            this.parent = parent;
            this.attrs = attrs;
            this.end = end;
            this.start = start;
            this.child = child;
        }

        public void assertExpected(Element parent, AttributeSet attrs,
                                   int start, int end, Element child) {
            assertSame(parent, this.parent);
            assertEquals(attrs, new SimpleAttributeSet(this.attrs));
            assertEquals(start, this.start);
            assertEquals(end, this.end);
            assertSame(child, this.child);
        }
    }

    private static class BranchChild {
        public final Element parent;
        public final AttributeSet attrs;
        public final Element child;
        public BranchChild(Element parent, AttributeSet attrs,
                           Element child) {
            this.parent = parent;
            this.attrs = attrs;
            this.child = child;
        }

        public void assertExpected(Element parent, AttributeSet attrs,
                                   Element child) {
            assertSame(parent, this.parent);
            assertEquals(attrs, new SimpleAttributeSet(this.attrs));
            assertSame(child, this.child);
        }
    }

    private DefaultStyledDocument doc;
    private Element root;

    private List branches;
    private List leaves;

    protected void setUp() throws Exception {
        super.setUp();
        branches = new ArrayList();
        leaves = new ArrayList();
        doc = new DefaultStyledDocument() {
            protected Element createBranchElement(Element parent,
                                                  AttributeSet as) {
                Element child = super.createBranchElement(parent, as);
                branches.add(new BranchChild(parent, as, child));
                return child;
            }

            protected Element createLeafElement(Element parent,
                                                AttributeSet as,
                                                int start,
                                                int end) {
                Element child = super.createLeafElement(parent, as, start, end);
                leaves.add(new LeafChild(parent, as, start, end, child));
                return child;
            }

        };
        root = doc.getDefaultRootElement();
        doc.insertString(0, "012abc\ntwo\nthree", null);
        branches.clear();
        leaves.clear();
    }

    /*
     * DefaultStyledDocument.setCharacterAttributes()
     */
    public void testSetCharacterAttributes() {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, true);
        doc.setCharacterAttributes(2, 2, attrs, false);

        assertEquals(0, branches.size());
        assertEquals(3, leaves.size());
        final Element parent = root.getElement(0);
        ((LeafChild)leaves.get(0)).assertExpected(parent,
                                                  SimpleAttributeSet.EMPTY,
                                                  0, 2,
                                                  parent.getElement(0));
        assertEquals(SimpleAttributeSet.EMPTY,
                     parent.getElement(0).getAttributes());
        ((LeafChild)leaves.get(1)).assertExpected(parent,
                                                  SimpleAttributeSet.EMPTY,
                                                  2, 4,
                                                  parent.getElement(1));
        assertEquals(attrs, parent.getElement(1).getAttributes());
        ((LeafChild)leaves.get(2)).assertExpected(parent,
                                                  SimpleAttributeSet.EMPTY,
                                                  4, 7,
                                                  parent.getElement(2));
        assertEquals(SimpleAttributeSet.EMPTY,
                     parent.getElement(2).getAttributes());
    }

    /*
     * DefaultStyledDocument.setLogicalStyle()
     */
    public void testSetLogicalStyle() {
        Style style = doc.addStyle("aStyle",
                                   doc.getStyle(StyleContext.DEFAULT_STYLE));
        doc.setLogicalStyle(3, style);

        assertEquals(0, branches.size());
        assertEquals(0, leaves.size());
    }

    /*
     * DefaultStyledDocument.setParagraphAttributes()
     */
    public void testSetParagraphAttributes() {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, true);
        doc.setParagraphAttributes(3, 6, attrs, false);

        assertEquals(0, branches.size());
        assertEquals(0, leaves.size());
    }

    /*
     * AbstractDocument.insertString(int, String, AttributeSet)
     */
    public void testInsertString() throws BadLocationException {
        doc.insertString(7, "^^^\n", null);

        assertEquals(1, branches.size());
        assertEquals(2, leaves.size());

        MutableAttributeSet logStyle = new SimpleAttributeSet();
        logStyle.setResolveParent(doc.getStyle(StyleContext.DEFAULT_STYLE));
        ((BranchChild)branches.get(0)).assertExpected(root, logStyle,
                                                      root.getElement(1));

        ((LeafChild)leaves.get(0)).assertExpected(
                root.getElement(0), SimpleAttributeSet.EMPTY, 0, 7,
                root.getElement(0).getElement(0));
        ((LeafChild)leaves.get(1)).assertExpected(
                root.getElement(1), SimpleAttributeSet.EMPTY, 7, 11,
                root.getElement(1).getElement(0));
    }

    /*
     * AbstractDocument.remove(int, int)
     */
    public void testRemove() throws BadLocationException {
        doc.remove(3, 4);

        assertEquals(1, branches.size());
        assertEquals(2, leaves.size());

        MutableAttributeSet logStyle = new SimpleAttributeSet();
        logStyle.setResolveParent(doc.getStyle(StyleContext.DEFAULT_STYLE));
        ((BranchChild)branches.get(0)).assertExpected(root, logStyle,
                                                      root.getElement(0));

        ((LeafChild)leaves.get(0)).assertExpected(
                root.getElement(0), SimpleAttributeSet.EMPTY, 0, 7,
                root.getElement(0).getElement(0));
        ((LeafChild)leaves.get(1)).assertExpected(
                root.getElement(0), SimpleAttributeSet.EMPTY, 7, 11,
                root.getElement(0).getElement(1));
    }

}
