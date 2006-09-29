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
 * @author Evgeniya G. Maenkova
 * @version $Revision$
 */
package javax.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.apache.harmony.x.swing.StringConstants;

public class JTextArea extends JTextComponent {
    protected class AccessibleJTextArea extends
            JTextComponent.AccessibleJTextComponent {
        /**
         * Adds state AccessibleState.MULTI_LINE to super class states
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet ass = super.getAccessibleStateSet();
            ass.add(AccessibleState.MULTI_LINE);
            return ass;
        }
    }

    private static final String uiClassID = "TextAreaUI";

    private int rows;

    private int columns;

    private boolean wrapWord;

    private boolean wrapLine;

    private int rowHeight;

    private int columnsWidth;

    private AccessibleContext accessibleContext;

    private Integer tabSize;

    public JTextArea(final Document doc, final String s, final int r,
                     final int c) {
        super();
        if (r < 0) {
            throw new IllegalArgumentException("rows: " + r);
        }
        if (c < 0) {
            throw new IllegalArgumentException("columns: " + c);
        }
        Document document = doc;
        if (document == null) {
            document = createDefaultModel();
        }
        setDocument(document);
        if (s != null) {
            try {
                document.remove(0, document.getLength());
                document.insertString(0, s, null);
            } catch (final BadLocationException e) {
            }
        }
        rows = r;
        columns = c;
        evaluate(getFont());
        tabSize = (Integer) document.getProperty("tabSize");
    }

    public JTextArea(final Document doc) {
        this(doc, null, 0, 0);
    }

    public JTextArea(final String s, final int r, final int c) {
        this(null, s, r, c);
    }

    public JTextArea(final String s) {
        this(null, s, 0, 0);
    }

    public JTextArea(final int r, final int c) {
        this(null, null, r, c);
    }

    public JTextArea() {
        this(null, null, 0, 0);
    }

    public synchronized void append(final String s) {
        Document doc = getDocument();
        if (doc == null || (s == null || s == "")) {
            return;
        }
        try {
            doc.insertString(doc.getLength(), s, null);
        } catch (final BadLocationException e) {
        }
    }

    private int  checkLineCount(final int line) throws BadLocationException {
        int count = getDocument().getDefaultRootElement().getElementCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", line);
        }
        if (line >= count) {
            throw new BadLocationException("No such line", line);
        }
        return count;
    }

    protected Document createDefaultModel() {
        return new PlainDocument();
    }

    /*
     * Sets new columnsWidth and rowHeight
     */
    private void evaluate(final Font f) {
        if (f != null) {
            FontMetrics fm = getFontMetrics(f);
            rowHeight = fm.getHeight();
            columnsWidth = fm.charWidth('m');
        } else {
            rowHeight = 0;
            columnsWidth = 0;
        }
    }

    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTextArea();
        }
        return accessibleContext;
    }

    public int getColumns() {
        return columns;
    }

    protected int getColumnWidth() {
        return columnsWidth;
    }

    public int getLineCount() {
        Document doc = getDocument();
        int count = 0;
        readLock(doc);
        try {
            count = doc.getDefaultRootElement().getElementCount();
        } finally {
            readUnlock(doc);
        }
        return count;
    }

    public int getLineEndOffset(final int line) throws BadLocationException {
        Document doc = getDocument();
        int end = doc.getLength();
        int count = 0;
        readLock(doc);
        try {
            count = checkLineCount(line);
            end = doc.getDefaultRootElement().getElement(line).getEndOffset();
        } finally {
            readUnlock(doc);
        }
        return (line == count - 1) ? end - 1 : end;
    }

    public int getLineOfOffset(final int offset) throws BadLocationException {
        Document doc = getDocument();
        int length = doc.getLength();
        if (offset < 0 || offset > length) {
            throw new BadLocationException("Can't translate offset to line",
                    offset);
        }
        readLock(doc);
        int index = 0;
        try {
            index = doc.getDefaultRootElement().getElementIndex(offset);
        } finally {
            readUnlock(doc);
        }
        return index;
    }

    public int getLineStartOffset(final int line) throws BadLocationException {
        Document doc = getDocument();
        readLock(doc);
        int start = 0;
        try {
            checkLineCount(line);
            start = doc.getDefaultRootElement().getElement(line)
            .getStartOffset();
        } finally {
            readUnlock(doc);
        }
        return start;
    }

    public boolean getLineWrap() {
        return wrapLine;
    }

    public Dimension getPreferredScrollableViewportSize() {
        Dimension dim = getPreferredSize();
        int width = (columns == 0) ? dim.width : columns * columnsWidth;
        int height = (rows == 0) ? dim.height : rows * rowHeight;
        return new Dimension(width, height);
    }

    public Dimension getPreferredSize() {
        int width1 = columns * columnsWidth;
        int height1 = rows * rowHeight;
        Dimension dim2 = super.getPreferredSize();
        int width2 = dim2.width;
        int height2 = dim2.height;
        return new Dimension(Math.max(width1, width2), Math.max(height1,
                height2));
    }

    protected int getRowHeight() {
        return rowHeight;
    }

    public int getRows() {
        return rows;
    }

    public boolean getScrollableTracksViewportWidth() {
        return super.getScrollableTracksViewportWidth() || wrapLine;
    }

    public int getScrollableUnitIncrement(final Rectangle r,
             final int orientation, final int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return columnsWidth;
        }
        if (orientation == SwingConstants.VERTICAL) {
            return rowHeight;
        }
        throw new IllegalArgumentException("Invalid orientation: "
                + orientation);
    }

    public int getTabSize() {
        return tabSize.intValue();
    }

    public String getUIClassID() {
        return uiClassID;
    }

    public boolean getWrapStyleWord() {
        return wrapWord;
    }

    public synchronized void insert(final String s, final int pos) {
        Document doc = getDocument();
        if (doc == null || (s == null || s == "")) {
            return;
        }
        int length = doc.getLength();
        if (pos < 0 || pos > length) {
            throw new IllegalArgumentException("Invalid insert");
        }
        try {
            doc.insertString(pos, s, null);
        } catch (final BadLocationException e) {
        }

    }

    /*
     * The format of the string is based on 1.5 release behavior
     * which can be revealed using the following code:
     *
     *     Object obj = new JTextArea();
     *     System.out.println(obj.toString());
     */
    protected String paramString() {
        return super.paramString() + "," + "columns=" + getColumns() + ","
                + "columnWidth=" + getColumnWidth() + "," + "rows=" + getRows()
                + "," + "rowHeight=" + getRowHeight() + "," + "word="
                + getWrapStyleWord() + "," + "wrap=" + getLineWrap();
    }

    private void readLock(final Document doc) {
        if (!(doc instanceof AbstractDocument)) {
            return;
        }
        ((AbstractDocument) doc).readLock();
    }

    private void readUnlock(final Document doc) {
        if (!(doc instanceof AbstractDocument)) {
            return;
        }
        ((AbstractDocument) doc).readUnlock();
    }

    public synchronized void replaceRange(final String s, final int start,
                                          final int end) {
        Document doc = getDocument();
        if (doc == null) {
            return;
        }
        int length = doc.getLength();
        if (start < 0 || end > length) {
            throw new IllegalArgumentException("Invalid remove");
        }
        if (start > end) {
            throw new IllegalArgumentException("end before start");
        }

        try {
            doc.remove(start, end - start);
            if (s != null && s != "") {
                doc.insertString(start, s, null);
            }
        } catch (final BadLocationException e) {
        }
    }

    public void setColumns(final int c) {
        if (c < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        columns = c;
        invalidate();
    }

    public void setFont(final Font f) {
        super.setFont(f);
        evaluate(f);
        revalidate();
        //Perhaps JComponent should do it
        repaint();
    }

    public void setLineWrap(final boolean b) {
        boolean old = wrapLine;
        wrapLine = b;
        firePropertyChange(StringConstants.TEXT_COMPONENT_LINE_WRAP_PROPERTY,
                           old, b);
    }

    public void setRows(final int r) {
        if (r < 0) {
            throw new IllegalArgumentException("rows less than zero.");
        }
        rows = r;
        invalidate();
    }

    /**
     * Sets document's property PlainDocument.tabSizeAttribute.
     */
    public void setTabSize(final int size) {
        Integer old = tabSize;
        tabSize = new Integer(size);
        Document doc = getDocument();
        if (doc != null) {
            doc.putProperty(PlainDocument.tabSizeAttribute, tabSize);
        }
        firePropertyChange(PlainDocument.tabSizeAttribute, old, tabSize);
    }

    public void setWrapStyleWord(final boolean b) {
        boolean old = wrapWord;
        wrapWord = b;
        firePropertyChange(StringConstants
                           .TEXT_COMPONENT_WRAP_STYLE_WORD_PROPERTY,
                           old, b);
    }
}

