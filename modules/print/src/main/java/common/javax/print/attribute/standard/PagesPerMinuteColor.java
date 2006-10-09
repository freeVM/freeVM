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
 * @author Elena V. Sayapina 
 * @version $Revision: 1.5 $ 
 */ 

package javax.print.attribute.standard;

import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintServiceAttribute;


public final class PagesPerMinuteColor extends IntegerSyntax 
    implements PrintServiceAttribute {


    public PagesPerMinuteColor(int value) {
        super(value, 0, Integer.MAX_VALUE);
    }


    public boolean equals(Object object) {
        if ( !(object instanceof PagesPerMinuteColor) ) {
            return false;
        }
        return super.equals(object);
    }

    public final Class getCategory() {
    /* 1.5 support requires the following changes
       Class<? extends Attribute> getCategory() { */
        return PagesPerMinuteColor.class;
    }

    public final String getName() {
        return "pages-per-minute-color";
    }


}
