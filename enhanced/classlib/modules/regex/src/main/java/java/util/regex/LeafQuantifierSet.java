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
 * @version $Revision: 1.4.2.2 $
 */
package java.util.regex;

import org.apache.harmony.regex.internal.nls.Messages;

/**
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.4.2.2 $
 */
class LeafQuantifierSet extends QuantifierSet {
   
    protected LeafSet leaf;

    public LeafQuantifierSet(LeafSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
        this.leaf = innerSet;
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int i = 0;
        int shift = 0;

        while (stringIndex + leaf.charCount() <= matchResult.getRightBound()
                && (shift = leaf.accepts(stringIndex, testString)) > 0) {
            stringIndex += shift;
            i++;
        }

        for (; i >= 0; i--) {
            shift = next.matches(stringIndex, testString, matchResult);
            if (shift >= 0) {
                return shift;
            }

            stringIndex--;
        }
        return -1;
    }

    protected String getName() {
        return "<Quant>"; //$NON-NLS-1$
    }

    /**
     * Sets an inner set.
     * @param innerSet
     *            The innerSet to set.
     */
    public void setInnerSet(AbstractSet innerSet) {
        if (!(innerSet instanceof LeafSet))
            throw new RuntimeException(Messages.getString("regex.04")); //$NON-NLS-1$
        super.setInnerSet(innerSet);
        this.leaf = (LeafSet) innerSet;
    }
}
