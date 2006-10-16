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
 * @author Denis M. Kishenko
 * @version $Revision$
 */
package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.NoSuchElementException;

public class Area implements Shape, Cloneable {

    /**
     * The source Shape object
     */
    Shape s;

    private static class NullIterator implements PathIterator {

        NullIterator() {
        }

        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        public boolean isDone() {
            return true;
        }

        public void next() {
            // nothing
        }

        public int currentSegment(double[] coords) {
            throw new NoSuchElementException("Iterator out of bounds");
        }

        public int currentSegment(float[] coords) {
            throw new NoSuchElementException("Iterator out of bounds");
        }

    }

    public Area() {
    }

    public Area(Shape s) {
        if (s == null) {
            throw new NullPointerException();
        }
        this.s = s;
    }

    public boolean contains(double x, double y) {
        return s == null ? false : s.contains(x, y);
    }

    public boolean contains(double x, double y, double width, double height) {
        return s == null ? false : s.contains(x, y, width, height);
    }

    public boolean contains(Point2D p) {
        if (p == null) {
            throw new NullPointerException();
        }
        return s == null ? false : s.contains(p);
    }

    public boolean contains(Rectangle2D r) {
        if (r == null) {
            throw new NullPointerException();
        }
        return s == null ? false : s.contains(r);
    }

    public boolean equals(Area obj) {
        throw new RuntimeException("Not implemented");
    }

    public boolean intersects(double x, double y, double width, double height) {
        return s == null ? false : s.intersects(x, y, width, height);
    }

    public boolean intersects(Rectangle2D r) {
        if (r == null) {
            throw new NullPointerException();
        }
        return s == null ? false : s.intersects(r);
    }

    public Rectangle getBounds() {
        return s == null ? new Rectangle() : s.getBounds();
    }

    public Rectangle2D getBounds2D() {
        return s == null ? new Rectangle2D.Double(): s.getBounds2D();
    }

    public PathIterator getPathIterator(AffineTransform t) {
        return s == null ? new NullIterator() : s.getPathIterator(t);
    }

    public PathIterator getPathIterator(AffineTransform t, double flatness) {
        return s == null ? new NullIterator() : s.getPathIterator(t, flatness);
    }

    public void add(Area area) {
        throw new RuntimeException("Not implemented");
    }

    public void exclusiveOr(Area area) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Extract Rectangle2D from the source shape
     * @return a Rectangle2D object if the source shape is rectangle, or null if shape is empty or not rectangle. 
     */
    Rectangle2D extractRectangle() {
        if (s == null) {
            return null;
        }
        float[] points = new float[12];
        int count = 0;
        PathIterator p = s.getPathIterator(null);
        float[] coords = new float[6];
        while(!p.isDone()) {
            int type = p.currentSegment(coords);
            if (count > 12 || type == PathIterator.SEG_QUADTO || type == PathIterator.SEG_CUBICTO) {
                return null;
            }
            points[count++] = coords[0];
            points[count++] = coords[1];
            p.next();
        }
        if (points[0] == points[6] && points[6] == points[8] && points[2] == points[4] &&
            points[1] == points[3] && points[3] == points[9] && points[5] == points[7])
        {
            return new Rectangle2D.Float(points[0], points[1], points[2] - points[0], points[7] - points[1]);
        }
        return null;
    }
    
    public void intersect(Area area) {
        Rectangle2D src1 = extractRectangle();
        Rectangle2D src2 = area.extractRectangle();
        if (src1 != null && src2 != null) {
            Rectangle2D.intersect(src1, src2, (Rectangle2D)s);
        }
    }

    public void subtract(Area area) {
        throw new RuntimeException("Not implemented");
    }

    public boolean isEmpty() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isPolygonal() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isRectangular() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isSingular() {
        throw new RuntimeException("Not implemented");
    }

    public void reset() {
        throw new RuntimeException("Not implemented");
    }

    public void transform(AffineTransform t) {
        s = t.createTransformedShape(s);
    }

    public Area createTransformedArea(AffineTransform t) {
        return new Area(t.createTransformedShape(s));
    }

    @Override
    public Object clone() {
        return new Area(this);
    }

}
