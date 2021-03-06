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
package org.apache.harmony.test.func.api.java.beans.introspector.useallmethods;

/**
 * Bean has empty BeanInfo. Bean contains simple properties and adder and
 * remover methods of event.
 * 
 */
public class Bean3 {
    private int i;
    private int property8;

    /**
     * getter method for property8.
     */
    public int getProperty8() {
        return property8;
    }

    /**
     * setter method for property8.
     */
    public void setProperty8(int property8) {
        this.property8 = property8;
    }

    /**
     * not-standart getter method for property i.
     */
    public int ggetI() {
        return i;
    }

    /**
     * not-standart setter method for property i.
     */
    public void ssetI(int i) {
        this.i = i;
    }

    public void addFredListener(FredListener fredListener) {
    }

    public void removeFredListener(FredListener fredListener) {
    }
}