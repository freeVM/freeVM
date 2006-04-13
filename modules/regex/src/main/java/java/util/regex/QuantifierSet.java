/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.14.2.2 $
 */
package java.util.regex;

/**
 * Base class for quantifiers.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.14.2.2 $
 */
abstract class QuantifierSet extends AbstractSet {
    
    protected AbstractSet innerSet;

    public QuantifierSet(AbstractSet innerSet, AbstractSet next, int type) {
        super(next);
        this.innerSet = innerSet;
        setType(type);
    }

    /**
     * Returns the innerSet.
     */
    public AbstractSet getInnerSet() {
        return innerSet;
    }

    /**
     * Sets an inner set.
     * @param innerSet
     *            The innerSet to set.
     */
    public void setInnerSet(AbstractSet innerSet) {
        this.innerSet = innerSet;
    }

    public boolean first(AbstractSet set) {
        return innerSet.first(set) || next.first(set);
    }

    public boolean hasConsumed(MatchResultImpl mr) {
        return true;
    }
}
