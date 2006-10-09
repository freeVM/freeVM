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
 * @author Elena V. Sayapina 
 * @version $Revision: 1.4 $ 
 */ 

package javax.print.attribute;

import java.io.Serializable;


public abstract class Size2DSyntax implements Cloneable, Serializable {


    public static final int INCH = 25400;

    public static final int MM = 1000;

    private int x;

    private int y;


    protected Size2DSyntax(int x, int y, int units) {

        if ((x < 0) || (y < 0) || (units < 1)) {
            throw new IllegalArgumentException("Valid values are:" +
                                            "x>=0, y>=0, units>=1");
        }
        this.x = x*units;
        this.y = y*units;

    }

    protected Size2DSyntax(float x, float y, int units) {

        if ( (x < 0.0f) || (y < 0.0f) || (units < 1) ) {
            throw new IllegalArgumentException("Valid values are:" +
                                            "x>=0.0, y>=0.0, units>=1");
        }
        this.x = Math.round(x*units);
        this.y = Math.round(y*units);

    }

    public boolean equals(Object object) {

        if ((object instanceof Size2DSyntax) &&
                (x == ((Size2DSyntax) object).x ) &&
                    (y == ((Size2DSyntax) object).y)) {
                            return true;
        } else {
            return false;
        }
    }

    public float[] getSize(int units) {
        return new float[] { getX(units),
                             getY(units) };
    }

    public float getX(int units) {

        if (units < 1) {
            throw new IllegalArgumentException("units is less than 1");
        }
        return ((float) x)/units;
    }

    public float getY(int units) {

        if (units < 1) {
            throw new IllegalArgumentException("units is less than 1");
        }
        return ((float) y)/units;
    }

    protected int getXMicrometers() {
        return x;
    }

    protected int getYMicrometers() {
        return y;
    }

    public int hashCode() {
        return ( y | (x << 16) );
    }

    public String toString() {
        return (x + "x" + y + " um");
    }

    public String toString(int units, String unitsName) {
        if (unitsName == null) {
            unitsName = "";
        }
        return (getX(units) + "x" + getX(units) + " " + unitsName);
    }

}
