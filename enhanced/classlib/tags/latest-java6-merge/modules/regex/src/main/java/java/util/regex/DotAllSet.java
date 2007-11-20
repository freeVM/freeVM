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
 * @version $Revision: 1.6.2.2 $
 */
package java.util.regex;

/**
 * Node accepting any character including line terminators.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.6.2.2 $
 */
class DotAllSet extends JointSet {

	public int matches(int stringIndex, CharSequence testString,
	    MatchResultImpl matchResult) {
	    int strLength = matchResult.getRightBound();
	        
	    if (stringIndex + 1 > strLength) {
	        matchResult.hitEnd = true;
	        return -1;
	    }
	        
	    char high = testString.charAt(stringIndex);
	            
	    if (Character.isHighSurrogate(high) && (stringIndex + 2 <= strLength)) {
	        char low = testString.charAt(stringIndex + 1);
	            
	        if (Character.isSurrogatePair(high, low)) {
	            return next.matches(stringIndex + 2, testString, matchResult);
	        }
	    }    
	    return next.matches(stringIndex + 1, testString, matchResult);
	}        

    protected String getName() {
        return "DotAll"; //$NON-NLS-1$
    }

        
    public AbstractSet getNext() {
        return this.next;
    }
        
    public void setNext(AbstractSet next) {
        this.next = next;
    }
    
    public int getType() {
        return AbstractSet.TYPE_DOTSET;
    }
    
        
    public boolean hasConsumed(MatchResultImpl matchResult) {         
        return true;
    }
}
