/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Aleksey V. Yantsen
 * @version $Revision: 1.2 $
 */

/**
 * Created on 10.25.2004
 */
package org.apache.harmony.jpda.tests.framework.jdwp;

import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants;

/**
 * This class represents tagged-objectID value in JDWP packet.
 */
public class TaggedObject {
    public byte tag;
    public long objectID;

    /**
     * Creates new value with empty tag.
     */
    public TaggedObject() {
        tag = JDWPConstants.Tag.NO_TAG;
        objectID = 0;
    }

    /**
     * Creates new value with specified data.
     */
    public TaggedObject(byte tag, long objectID) {
        this.tag = tag;
        this.objectID = objectID;
    }
}