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
 * @version $Revision: 1.12.2.2 $
 */
package java.util.regex;

import java.util.ArrayList;

/**
 * Represents group, which is alternation of other subexpression.
 * One should think about "group" in this model as JointSet opening
 * group and corresponding FSet closing group.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.12.2.2 $
 */
class JointSet extends AbstractSet {
    
    protected ArrayList children;

    protected AbstractSet fSet;

    protected int groupIndex;

    protected JointSet() {
    }

    public JointSet(ArrayList children, FSet fSet) {
        this.children = children;
        this.fSet = fSet;
        this.groupIndex = fSet.getGroupIndex();
    }

    /**
     * Returns stringIndex+shift, the next position to match
     */
    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int start = matchResult.getStart(groupIndex);
        matchResult.setStart(groupIndex, stringIndex);
        int size = children.size();
        for (int i = 0; i < size; i++) {
            AbstractSet e = (AbstractSet) children.get(i);
            int shift = e.matches(stringIndex, testString, matchResult);
            if (shift >= 0) {
                return shift;
            }
        }
        matchResult.setStart(groupIndex, start);
        return -1;
    }

    public void setNext(AbstractSet next) {
        fSet.setNext(next);
    }

    public AbstractSet getNext() {
        return fSet.getNext();
    }

    protected String getName() {
        return "JointSet";
    }

    public int getGroup() {
        return groupIndex;
    }

    public boolean first(AbstractSet set) {
        for (java.util.Iterator i = children.iterator(); i.hasNext();) {
            if (((AbstractSet) i.next()).first(set)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasConsumed(MatchResultImpl matchResult) {
        return !(matchResult.getEnd(groupIndex) >= 0 && matchResult
                .getStart(groupIndex) == matchResult.getEnd(groupIndex));
    }
    
    /**
     * This method is used for traversing nodes after the 
     * first stage of compilation.
     */
    public void processSecondPass() {
    	this.isSecondPassVisited = true;
    	
    	if (fSet != null && !fSet.isSecondPassVisited) {
    		    
    		/*
    	     * Add here code to do during the pass
             */
    	        
   	        /*
    	     * End code to do during the pass
             */
    		fSet.processSecondPass();
    	}
        
    	if (children != null) {
    		int childrenSize = children.size();
    		
    		for (int i = 0; i < childrenSize; i++) {
    			AbstractSet child = (AbstractSet) children.get(i);
    			
    			/*
        	     * Add here code to do during the pass
                 */
    			
    			JointSet set = child.processBackRefReplacement();
    			
    			if (set != null) {
    				child.isSecondPassVisited = true;
    				children.remove(i);
    				children.add(i, set);
    			    child = (AbstractSet) set;
    			}
    		
    			/*
        	     * End code to do during the pass
                 */
    			if (!child.isSecondPassVisited) {
    				child.processSecondPass();
    			}
    		}
    	}
    	
    	if (next != null) {
    		super.processSecondPass();
    	}
    }
}
