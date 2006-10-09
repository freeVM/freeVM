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
 * @version $Revision: 1.6 $ 
 */ 

package javax.print.attribute;

/*
 * @author esayapin
 */

public interface AttributeSet {

    boolean add(Attribute attribute);
    
    boolean addAll(AttributeSet attributeSet);

    void clear();
    
    boolean containsKey(Class attributeCategory);
    //1.5 support requires the following changes 
    //boolean containsKey(Class<?> attributeCategory);
    
    boolean containsValue(Attribute attribute);
    
    boolean equals(Object object);
    
    Attribute get(Class attributeCategory);
    //1.5 support requires the following changes 
    //Attribute get(Class<?> attributeCategory);
    
    int hashCode();
    
    boolean isEmpty();
    
    boolean remove(Attribute attribute);
    
    boolean remove(Class attributeCategory);
    //1.5 support requires the following changes 
    //boolean remove(Class<?> attributeCategory);

    int size();
    
    Attribute[] toArray();
    
}
