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
package org.apache.harmony.pack200;

public class CPMethodOrField extends ConstantPoolEntry implements Comparable {

    private final CPClass className;
    private final CPNameAndType nameAndType;
    private int indexInClass = -1;
    private int indexInClassForConstructor = -1;

    public CPMethodOrField(CPClass className, CPNameAndType nameAndType) {
        this.className = className;
        this.nameAndType = nameAndType;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CPMethodOrField)) {
            return false;
        }
        return ((CPMethodOrField) obj).className.equals(className)
                && ((CPMethodOrField) obj).nameAndType.equals(nameAndType);
    }

    public int hashCode() {
        return className.hashCode() + nameAndType.hashCode();
    }

    public String toString() {
        return className + ": " + nameAndType;
    }

    public int compareTo(Object obj) {
        if (obj instanceof CPMethodOrField) {
            CPMethodOrField mof = (CPMethodOrField) obj;
            int compareName = className.compareTo(mof.className);
            if (compareName == 0) {
                return nameAndType.compareTo(mof.nameAndType);
            } else {
                return compareName;
            }
        }
        return 0;
    }

    public int getClassIndex() {
        return className.getIndex();
    }

    public CPClass getClassName() {
        return className;
    }

    public int getDescIndex() {
        return nameAndType.getIndex();
    }

    public CPNameAndType getDesc() {
        return nameAndType;
    }

    public int getIndexInClass() {
        return indexInClass;
    }

    public void setIndexInClass(int index) {
        indexInClass = index;
    }

    public int getIndexInClassForConstructor() {
        return indexInClassForConstructor;
    }

    public void setIndexInClassForConstructor(int index) {
        indexInClassForConstructor = index;
    }

}