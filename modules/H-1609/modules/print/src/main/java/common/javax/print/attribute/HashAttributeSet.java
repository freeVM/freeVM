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

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class HashAttributeSet implements Serializable, AttributeSet {


    private Class attributeInterfaceName;

    private transient HashMap attributesMap = new HashMap();

    public HashAttributeSet(){

        this(Attribute.class);
    }
    
    public HashAttributeSet(Attribute attribute){

        this(attribute, Attribute.class);
    }
    
    public HashAttributeSet(Attribute[] attributes) {

        this(attributes, Attribute.class);
    }
  
    public HashAttributeSet(AttributeSet attributeSet) {

        this(attributeSet, Attribute.class);
    }
    
    protected  HashAttributeSet(Class interfaceName) {
    //1.5 support requires the following changes
    //protected HashAttributeSet(Class<?> interfaceName) {

        if (interfaceName == null) {
            throw new NullPointerException("Null attribute interface");
        }
        attributeInterfaceName = interfaceName;
    }
    
    protected  HashAttributeSet(Attribute attribute, Class interfaceName) {
    //1.5 support requires the following changes
    //protected HashAttributeSet(Attribute attribute, Class<?> interfaceName) {

        if (interfaceName == null) {
            throw new NullPointerException("Null attribute interface");
        }
        attributeInterfaceName = interfaceName;
        add(attribute);
    }

    protected  HashAttributeSet(Attribute[] attributes, Class interfaceName) {
    //1.5 support requires the following changes
    //protected HashAttributeSet(Attribute[] attributes,
    //Class<?> interfaceName) {

        if (interfaceName == null) {
            throw new NullPointerException("Null attribute interface");
        }
        attributeInterfaceName = interfaceName;
        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
            add(attributes[i]);
            }
        }
    }

    protected  HashAttributeSet(AttributeSet attributeSet, Class interfaceName) {
    //1.5 support requires the following changes
    //protected HashAttributeSet(AttributeSet attributeSet,
    //Class<?> interfaceName) {
        
        attributeInterfaceName = interfaceName;
        if (attributeSet != null) {
            Attribute[] attributes = attributeSet.toArray();
            for (int i = 0; i < attributes.length; i++) {
                add(attributes[i]);
            }
        }
    }


    private void readObject (ObjectInputStream ois)
        throws ClassNotFoundException, IOException {

        ois.defaultReadObject();
        Attribute attribute;
        attributesMap = new HashMap();
        int n = ois.readInt();
        for (int i = 0; i < n; i++) {
            attribute = (Attribute) ois.readObject();
            add(attribute);
        }

    }

    private void writeObject (ObjectOutputStream oos) throws IOException {

        oos.defaultWriteObject();
        Attribute[] attributes = toArray();
        int n = attributes.length;
        oos.writeInt(n);
        for (int i = 0; i < n; i++) {
            oos.writeObject(attributes[i]);
        }
    }


    public boolean add(Attribute attribute) {

        Attribute newValue =
            AttributeSetUtilities.verifyAttributeValue(attribute,
                    attributeInterfaceName);
        Object oldValue = attributesMap.put(attribute.getCategory(), newValue);
        return  !attribute.equals(oldValue);
    }

    public boolean addAll(AttributeSet attributeSet) {

        boolean outcome = true;
        Attribute[] attributes = attributeSet.toArray();
        for (int i = 0; i < attributes.length; i++) {
            if ( !add(attributes[i])) {
                outcome = false;
            }
        }
        return outcome;
    }

    public void clear() {
        attributesMap.clear();
    }

    public boolean containsKey(Class attributeCategory) {
    //1.5 support requires the following changes
    //public boolean containsKey(Class<?> attributeCategory) {

        if (attributeCategory == null) {
            return false;
        }
        return attributesMap.containsKey(attributeCategory);
    }

    public boolean containsValue(Attribute attribute) {

        if (attribute == null){
            return false;
        }
        Object curValue = attributesMap.get(attribute.getCategory());
        return attribute.equals(curValue);
    }

    public boolean equals(Object object) {

        if ( !(object instanceof AttributeSet) ||
                ((AttributeSet) object).size() != size() ) {
            return false;
        }
        Attribute[] attributes = toArray();
        for (int i = 0; i < attributes.length; i++) {
            if ( !((AttributeSet) object).containsValue(attributes[i]) ) {
                return false;
            }
        }
        return true;
    }

    public Attribute get(Class attributeCategory) {
    //1.5 support requires the following changes
    //public Attribute get(Class<?> attributeCategory) {

        AttributeSetUtilities.
            verifyAttributeCategory(attributeCategory, Attribute.class);
        return (Attribute) attributesMap.get(attributeCategory);
    }

    public int hashCode() {
        return attributesMap.hashCode();
    }

    public boolean isEmpty() {
        return attributesMap.isEmpty();
    }

    public boolean remove(Attribute attribute) {

        if ( (attribute == null) ||
                (attributesMap.remove(attribute.getCategory()) == null) ) {
            return false;
        }else {
            return true;
        }
    }

    public boolean remove(Class attributeCategory) {
    //1.5 support requires the following changes
    //public boolean remove(Class<?> attributeCategory) {

        if ((attributeCategory == null) ||
                (attributesMap.remove(attributeCategory) == null) ) {
                return false;
        } else {
            return true;
        }
    }

    public int size() {
        return attributesMap.size();
    }

    public Attribute[] toArray() {

        Attribute[] attributes = new Attribute[size()];
        attributesMap.values().toArray(attributes);
        return attributes;
    }


}
