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
 * @author Denis M. Kishenko
 * @version $Revision$
 */
package java.awt.geom;

import java.awt.Point;

public class Line2DFloatTest extends GeomTestCase {

    Line2D.Float l;

    public Line2DFloatTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        l = new Line2D.Float(1, 2, 3, 4);
    }

    protected void tearDown() throws Exception {
        l = null;
        super.tearDown();
    }

    public void testCreate1() {
        assertEquals(new Line2D.Float(0, 0, 0, 0), new Line2D.Float());
    }

    public void testCreate2() {
        assertEquals(new Line2D.Float(1, 2, 3, 4), new Line2D.Float(new Point(1, 2), new Point(3, 4)));
    }

    public void testGetX1() {
        assertEquals(1.0, l.getX1(), 0.0);
    }

    public void testGetY1() {
        assertEquals(2.0, l.getY1(), 0.0);
    }

    public void testGetX2() {
        assertEquals(3.0, l.getX2(), 0.0);
    }

    public void testGetY2() {
        assertEquals(4.0, l.getY2(), 0.0);
    }

    public void testGetP1() {
        assertEquals(new Point2D.Float(1, 2), l.getP1());
    }

    public void testGetP2() {
        assertEquals(new Point2D.Float(3, 4), l.getP2());
    }

    public void testSetLine1() {
        l.setLine(5.0, 6.0, 7.0, 8.0);
        assertEquals(new Line2D.Float(5, 6, 7, 8), l);
    }

    public void testSetLine2() {
        l.setLine(5.0f, 6.0f, 7.0f, 8.0f);
        assertEquals(new Line2D.Float(5, 6, 7, 8), l);
    }

    public void testGetBounds2D() {
        for(int i = 0; i < Line2DTest.bounds.length; i++) {
            assertEquals(
                    new Rectangle2D.Float(
                            Line2DTest.bounds[i][1][0],
                            Line2DTest.bounds[i][1][1],
                            Line2DTest.bounds[i][1][2],
                            Line2DTest.bounds[i][1][3]),
                    new Line2D.Float(
                            Line2DTest.bounds[i][0][0],
                            Line2DTest.bounds[i][0][1],
                            Line2DTest.bounds[i][0][2],
                            Line2DTest.bounds[i][0][3]).getBounds2D());
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Line2DFloatTest.class);
    }

}
