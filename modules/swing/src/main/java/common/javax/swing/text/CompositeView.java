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
 * @author Alexey A. Ivanov
 * @version $Revision$
 */
package javax.swing.text;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.Position.Bias;

import org.apache.harmony.awt.text.TextUtils;
import org.apache.harmony.x.swing.Utilities;


public abstract class CompositeView extends View {
    private View[] children;
    private final Insets insets = new Insets(0, 0, 0, 0);
    private final Rectangle rect = new Rectangle();

    public CompositeView(final Element element) {
        super(element);
        children = new View[0];
    }

    protected abstract void childAllocation(int index, Rectangle rc);
    protected abstract boolean isAfter(int x, int y, Rectangle rc);
    protected abstract boolean isBefore(int x, int y, Rectangle rc);
    protected abstract View getViewAtPoint(int x, int y, Rectangle alloc);

    public Shape getChildAllocation(final int index, final Shape shape) {
        Rectangle rc = getInsideAllocation(shape);
        childAllocation(index, rc);

        return rc;
    }

    public View getView(final int index) {
        return children[index];
    }

    public int getViewIndex(final int pos, final Bias bias) {
        int offset = bias == Bias.Backward ? pos - 1 : pos;
        if (offset < getStartOffset() || offset >= getEndOffset()) {
            return -1;
        }

        return getViewIndexAtPosition(offset);
    }

    public int getViewCount() {
        return children.length;
    }

    public void replace(final int index, final int length, final View[] views) {
        int toAdd = views != null ? views.length : 0;

        for (int i = index; i < index + length; i++) {
            if (children[i].getParent() == this) {
                children[i].setParent(null);
            }
        }

        for (int i = 0; i < toAdd; i++) {
            views[i].setParent(this);
        }

        if (length == toAdd) {
            if (views != null) {
                System.arraycopy(views, 0, children, index, toAdd);
            }
        } else {
            View[] newArray = new View[children.length - length + toAdd];
            System.arraycopy(children, 0, newArray, 0, index);
            if (views != null) {
                System.arraycopy(views, 0, newArray, index, toAdd);
            }
            System.arraycopy(children, index + length, newArray,
                             index + toAdd,
                             children.length - (index + length));

            children = newArray;
        }
    }

    public void setParent(final View parentView) {
        super.setParent(parentView);

        if (parentView != null && getViewCount() == 0) {
            loadChildren(getViewFactory());
        }
    }

    public Shape modelToView(final int pos, final Shape shape, final Bias bias)
            throws BadLocationException {
        int index = getViewIndexAtPosition(bias == Bias.Backward ? pos - 1
                                                                 : pos);
        if (index != -1) {
            return getView(index).modelToView(pos,
                    getChildAllocation(index, shape), bias);
        }

        throw new BadLocationException("View representing " + pos
                                       + " not found", pos);
    }

    public Shape modelToView(final int p1, final Bias b1,
                             final int p2, final Bias b2, final Shape shape)
        throws BadLocationException {

        // TODO should throw IllegalArgumentException for invalid bias arg
        int index1 = getViewIndex(p1, b1);
        if (index1 == -1) {
            throw new BadLocationException("View representing " + p1
                                           + " not found", p1);
        }

        int index2 = getViewIndex(p2, b2);
        if (index2 == -1) {
            throw new BadLocationException("View representing " + p2
                                           + " not found", p2);
        }

        Rectangle rc1;
        Rectangle rc2;
        if (index1 == index2) {
            // Both positions are on the same line

            Rectangle alloc = (Rectangle)getChildAllocation(index1, shape);
            rc1 = (Rectangle)getView(index1).modelToView(p1, alloc, b1);
            rc2 = (Rectangle)getView(index1).modelToView(p2, alloc, b2);
        } else if (Math.abs(index2 - index1) == 1) {
            // Positions are on adjacent lines

            // Get "preferred" span for view @ index1
            Rectangle alloc = (Rectangle)getChildAllocation(index1, shape);
            View view1 = getView(index1);
            rc1 = (Rectangle)view1.modelToView(view1.getStartOffset(), alloc,
                    Bias.Forward);   // first char position
            rc2 = (Rectangle)view1.modelToView(view1.getEndOffset(), alloc,
                    Bias.Forward);   // last char position
            rc1 = rc1.union(rc2);    // first line (painted with text)

            rc2 = (Rectangle)getView(index2).modelToView(p2,
                    getChildAllocation(index2, shape), b2);
        } else {
            // Positions on different lines and the lines are not adjacent

            rc1 = new Rectangle((Rectangle)getChildAllocation(index1, shape));
            rc2 = (Rectangle)getChildAllocation(index2, shape);
        }

        return rc1.union(rc2);
    }

    public int viewToModel(final float x,
                           final float y,
                           final Shape shape,
                           final Bias[] biasReturn) {
        biasReturn[0] = Bias.Forward;

        Rectangle bounds = getInsideAllocation(shape);

        if (isBefore((int)x, (int)y, bounds)) {
            return getStartOffset();
        } else if (isAfter((int)x, (int)y, bounds)) {
            biasReturn[0] = Bias.Backward;
            return getEndOffset() - 1;
        } else {
            // The point lies within the bounds
            View v = getViewAtPoint((int)x, (int)y, bounds);
            if (v != null) {
                return v.viewToModel(x, y, bounds, biasReturn);
            } else {
                return -1;
            }
        }
    }

    public int getNextVisualPositionFrom(final int pos,
                                         final Bias bias,
                                         final Shape shape,
                                         final int direction,
                                         final Bias[] biasRet)
        throws BadLocationException {

        if (direction == WEST || direction == EAST) {
            return getNextEastWestVisualPositionFrom(pos, bias, shape,
                                                     direction, biasRet);
        } else if (direction == NORTH || direction == SOUTH) {
            return getNextNorthSouthVisualPositionFrom(pos, bias, shape,
                                                       direction, biasRet);
        }

        throw new IllegalArgumentException("Invalid direction");
    }

    protected void loadChildren(final ViewFactory factory) {
        if (factory == null) {
            return;
        }
        
        final int count = getElement().getElementCount();

        View[] views = new View[count];

        for (int i = 0; i < count; i++) {
            views[i] = factory.create(getElement().getElement(i));
        }

        replace(0, children.length, views);
    }

    protected int getViewIndexAtPosition(final int pos) {
        if (pos >= getEndOffset()) {
            return getViewCount() - 1;
        }
        if (pos < getStartOffset()) {
            return getViewCount() > 0 ? 0 : -1;
        }

        final int count = getViewCount();
        for (int i = 0; i < count; i++) {
            View view = getView(i);
            if (view.getStartOffset() <= pos
                && pos < view.getEndOffset()) {

                return i;
            }
        }

        return -1;
    }

    protected View getViewAtPosition(final int pos, final Rectangle alloc) {
        int index = getViewIndexAtPosition(pos);
        if (index != -1) {
            childAllocation(index, alloc);
            return getView(index);
        }
        return null;
    }

    protected short getTopInset() {
        return (short)insets.top;
    }

    protected short getLeftInset() {
        return (short)insets.left;
    }

    protected short getBottomInset() {
        return (short)insets.bottom;
    }

    protected short getRightInset() {
        return (short)insets.right;
    }

    protected void setInsets(final short top,
                             final short left,
                             final short bottom,
                             final short right) {
        insets.top    = top;
        insets.left   = left;
        insets.bottom = bottom;
        insets.right  = right;
    }

    protected void setParagraphInsets(final AttributeSet attrs) {
        setInsets((short)StyleConstants.getSpaceAbove(attrs),
                  (short)StyleConstants.getLeftIndent(attrs),
                  (short)StyleConstants.getSpaceBelow(attrs),
                  (short)StyleConstants.getRightIndent(attrs));
    }

    protected Rectangle getInsideAllocation(final Shape shape) {
        if (shape == null) {
            return null;
        }

        rect.setBounds(shape.getBounds());
        return Utilities.subtractInsets(rect, insets);
    }

    protected int getNextEastWestVisualPositionFrom(final int pos,
                                                    final Position.Bias bias,
                                                    final Shape shape,
                                                    final int direction,
                                                    final Bias[] biasRet)
        throws BadLocationException {

        if (direction == WEST || direction == EAST) {
            return TextUtils.getNextVisualPositionFrom(getTextKit(), this,
                                                       pos, bias,
                                                       shape, direction,
                                                       biasRet);
        }

        throw new IllegalArgumentException("Invalid direction");
    }

    protected int getNextNorthSouthVisualPositionFrom(final int pos,
                                                      final Position.Bias bias,
                                                      final Shape shape,
                                                      final int direction,
                                                      final Bias[] biasRet)
        throws BadLocationException {

        if (direction == NORTH || direction == SOUTH) {
            return TextUtils.getNextVisualPositionFrom(getTextKit(), this,
                                                       pos, bias,
                                                       shape, direction,
                                                       biasRet);
        }

        throw new IllegalArgumentException("Invalid direction");
    }

    protected boolean flipEastAndWestAtEnds(final int position,
                                            final Bias bias) {
        return false;
    }
}
