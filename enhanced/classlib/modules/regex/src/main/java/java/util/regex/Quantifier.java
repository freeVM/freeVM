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
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.6.2.2 $
 */
package java.util.regex;

/**
 * Represents RE quantifier; contains two fields responsible for min and max
 * number of repetitions. Negative value for maximum bumber of repetition
 * represents infinity(i.e. +,*)
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.6.2.2 $
 */
class Quantifier extends SpecialToken implements Cloneable {

    private int min;

    private int max;

    private int counter = 0;

    public Quantifier(int min) {
        this.min = this.max = min;
    }

    public Quantifier(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void resetCounter() {
        counter = 0;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public String toString() {
        return "{" //$NON-NLS-1$
                + min
                + "," //$NON-NLS-1$
                + ((max == Integer.MAX_VALUE) ? "" : new Integer(max) //$NON-NLS-1$
                        .toString()) + "}"; //$NON-NLS-1$
    }

    public int getType() {
        return SpecialToken.TOK_QUANTIFIER;
    }

    public Object clone() {
        return new Quantifier(min, max);
    }
}
