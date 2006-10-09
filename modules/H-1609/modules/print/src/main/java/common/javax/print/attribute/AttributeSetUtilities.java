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
 * @version $Revision: 1.6 $ 
 */ 

package javax.print.attribute;

import java.io.Serializable;

public final class AttributeSetUtilities {


    private static class SynchronizedAttributeSet
        implements AttributeSet, Serializable {


        private AttributeSet aset;

        public SynchronizedAttributeSet(AttributeSet attributeSet) {
            aset = attributeSet;
        }


        public synchronized boolean add (Attribute attribute) {
            return aset.add(attribute);
        }

        public synchronized boolean addAll (AttributeSet attributeSet) {
            return aset.addAll(attributeSet);
        }

        public synchronized void clear() {
            aset.clear();
        }

        public synchronized boolean containsKey (Class attributeCategory) {
            return aset.containsKey(attributeCategory);
        }

        public synchronized boolean containsValue (Attribute attribute) {
            return aset.containsValue(attribute);
        }

        public synchronized boolean equals (Object object) {
            return aset.equals (object);
        }

        public synchronized Attribute get (Class attributeCategory) {
            return aset.get(attributeCategory);
        }

        public synchronized int hashCode() {
            return aset.hashCode();
        }

        public synchronized boolean isEmpty() {
            return aset.isEmpty();
        }

        public synchronized boolean remove (Attribute attribute) {
            return aset.remove(attribute);
        }

        public synchronized boolean remove (Class attributeCategory) {
            return aset.remove(attributeCategory);
        }

        public synchronized int size() {
            return aset.size();
        }

        public synchronized Attribute[] toArray() {
            return aset.toArray();
        }


    }

    private static class SynchronizedDocAttributeSet
        extends SynchronizedAttributeSet
            implements DocAttributeSet, Serializable {

        public SynchronizedDocAttributeSet(DocAttributeSet attributeSet) {
            super(attributeSet);
        }
    }

    private static class SynchronizedPrintJobAttributeSet
        extends SynchronizedAttributeSet
            implements PrintJobAttributeSet, Serializable {

        public SynchronizedPrintJobAttributeSet
                    (PrintJobAttributeSet attributeSet) {
                        super(attributeSet);
        }
    }

    private static class SynchronizedPrintRequestAttributeSet
        extends SynchronizedAttributeSet
            implements PrintRequestAttributeSet, Serializable {

        public SynchronizedPrintRequestAttributeSet
                    (PrintRequestAttributeSet attributeSet) {
                        super(attributeSet);
        }
    }

    private static class SynchronizedPrintServiceAttributeSet
        extends SynchronizedAttributeSet
            implements PrintServiceAttributeSet, Serializable {

        public SynchronizedPrintServiceAttributeSet
                    (PrintServiceAttributeSet attributeSet) {
                        super(attributeSet);
        }
    }

    private static class UnmodifiableAttributeSet
        implements AttributeSet, Serializable {


        private AttributeSet aset;

        public UnmodifiableAttributeSet(AttributeSet attributeSet) {
            aset = attributeSet;
        }


        public boolean add(Attribute attribute) {
            throw new UnmodifiableSetException("Unmodifiable attribute set");
        }

        public boolean addAll(AttributeSet attributeSet) {
            throw new UnmodifiableSetException("Unmodifiable attribute set");
        }

        public void clear() {
            throw new UnmodifiableSetException("Unmodifiable attribute set");
        }

        public boolean containsKey(Class attributeCategory) {
            return aset.containsKey(attributeCategory);
        }

        public boolean containsValue(Attribute attribute) {
            return aset.containsValue(attribute);
        }

        public boolean equals(Object object) {
            return aset.equals(object);
        }

        public Attribute get (Class attributeCategory) {
            return aset.get(attributeCategory);
        }

        public int hashCode() {
            return aset.hashCode();
        }

        public boolean isEmpty() {
            return aset.isEmpty();
        }

        public boolean remove(Attribute attribute) {
            throw new UnmodifiableSetException("Unmodifiable attribute set");
        }

        public synchronized boolean remove (Class attributeCategory) {
            throw new UnmodifiableSetException("Unmodifiable attribute set");
        }

        public int size() {
            return aset.size();
        }

        public Attribute[] toArray() {
            return aset.toArray();
        }


    }

    private static class UnmodifiableDocAttributeSet
        extends UnmodifiableAttributeSet
            implements DocAttributeSet, Serializable {

        public UnmodifiableDocAttributeSet(DocAttributeSet attributeSet) {
            super(attributeSet);
        }
    }

    private static class UnmodifiablePrintJobAttributeSet
        extends UnmodifiableAttributeSet
            implements PrintJobAttributeSet, Serializable {

        public UnmodifiablePrintJobAttributeSet
                    (PrintJobAttributeSet attributeSet) {
                        super (attributeSet);
        }
    }

    private static class UnmodifiablePrintRequestAttributeSet
        extends UnmodifiableAttributeSet
            implements PrintRequestAttributeSet, Serializable {

        public UnmodifiablePrintRequestAttributeSet
                    (PrintRequestAttributeSet attributeSet) {
                        super(attributeSet);
        }
    }

    private static class UnmodifiablePrintServiceAttributeSet
        extends UnmodifiableAttributeSet
            implements PrintServiceAttributeSet, Serializable {

        public UnmodifiablePrintServiceAttributeSet
                    (PrintServiceAttributeSet attributeSet) {
                        super(attributeSet);
        }
    }

    
    private AttributeSetUtilities() {

    }


    public static AttributeSet 
        synchronizedView (AttributeSet attributeSet) {
            if (attributeSet == null) {
                throw new NullPointerException("Null attribute set");
            }
            return new SynchronizedAttributeSet(attributeSet);
    }
  
    public static DocAttributeSet 
        synchronizedView (DocAttributeSet attributeSet) {
            if (attributeSet == null) {
                throw new NullPointerException("Null attribute set");
            }
            return new SynchronizedDocAttributeSet(attributeSet);
    }
    
    public static PrintRequestAttributeSet 
        synchronizedView(PrintRequestAttributeSet attributeSet) {
            if (attributeSet == null) {
                throw new NullPointerException("Null attribute set");
            }
            return new SynchronizedPrintRequestAttributeSet(attributeSet);
    }

    public static PrintJobAttributeSet
        synchronizedView(PrintJobAttributeSet attributeSet) {
            if (attributeSet == null) {
                throw new NullPointerException("Null attribute set");
            }
            return new SynchronizedPrintJobAttributeSet(attributeSet);
    }

    public static PrintServiceAttributeSet
        synchronizedView(PrintServiceAttributeSet attributeSet) {
            if (attributeSet == null) {
                throw new NullPointerException("Null attribute set");
            }
            return new SynchronizedPrintServiceAttributeSet(attributeSet);
    }

    
    public static AttributeSet unmodifiableView (AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException("Null attribute set");
        }
        return new UnmodifiableAttributeSet(attributeSet);
    }
    
    public static DocAttributeSet 
                    unmodifiableView (DocAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException("Null attribute set");
        }
        return new UnmodifiableDocAttributeSet(attributeSet);
    }
    
    public static PrintJobAttributeSet
                    unmodifiableView(PrintJobAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException("Null attribute set");
        }
        return new UnmodifiablePrintJobAttributeSet(attributeSet);
    }
    
    public static PrintRequestAttributeSet 
                    unmodifiableView(PrintRequestAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException("Null attribute set");
        }
        return new UnmodifiablePrintRequestAttributeSet(attributeSet);
    }
      
    public static PrintServiceAttributeSet
                        unmodifiableView(PrintServiceAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException("Null attribute set");
        }
        return new UnmodifiablePrintServiceAttributeSet (attributeSet);
    }

    
    public static Class
        verifyAttributeCategory(Object object, Class interfaceName) {

    //1.5 support requires the following changes 
    //public static Class<?> verifyAttributeCategory(Object object,
    //Class<?> interfaceName);   

        if ( !(Attribute.class).isAssignableFrom(interfaceName) ){
        throw new ClassCastException(interfaceName.getName()+" is not " +
                            "interface Attribute or it's subinterface");
        } else if ( interfaceName.isAssignableFrom((Class) object) ) {
            return (Class) object;
        } else {
            throw new ClassCastException(object.getClass().getName()+
                    "doesn't implement"+interfaceName.getName());
        }
    }
 
    public static Attribute 
        verifyAttributeValue(Object attribute, Class interfaceName) {
    
    //1.5 support requires the following changes 
    //public static Attribute verifyAttributeValue(Object object,
    //Class<?> interfaceName);
        
        if (attribute == null) {
            throw new NullPointerException("Null attribute");
        } else if ( interfaceName.isInstance(attribute) ) {
            return (Attribute) attribute;
        } else {
            throw new ClassCastException("Object is not an instance of "
                            + interfaceName.getName());
        }
    }
    
    public static void 
        verifyCategoryForValue(Class attributeCategory, Attribute attribute) {
    
    //1.5 support requires the following changes 
    //public static void verifyCategoryForValue(Class<?> category,
    //Attribute attribute);
        
        if (!attributeCategory.equals (attribute.getCategory())) {
            throw new IllegalArgumentException(attributeCategory.getName()+
                            "is not equal to the category of the attribute"+
                                attribute.getCategory().getName());
        }

    }

}
