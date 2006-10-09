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
 * @version $Revision: 1.4 $ 
 */ 

package javax.print.attribute;

import java.io.Serializable;

public class HashDocAttributeSet 
    extends HashAttributeSet implements Serializable, DocAttributeSet {

    
    public HashDocAttributeSet() {
        super(DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttribute attribute) {
        super(attribute, DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttribute[] attributes) {
        super(attributes, DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttributeSet attributeSet) {
        super(attributeSet, DocAttribute.class);
    }

}
