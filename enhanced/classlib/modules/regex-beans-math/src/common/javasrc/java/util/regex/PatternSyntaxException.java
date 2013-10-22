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
 * @version $Revision: 1.7.2.2 $
 */
package java.util.regex;

import java.util.Arrays;

/**
 * @com.intel.drl.spec_ref
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.7.2.2 $
 */
public class PatternSyntaxException extends IllegalArgumentException {
    
    private static final long serialVersionUID = -3864639126226059218L;
    
    private String pattern;

    private String message;

    private int index = -1;

    /**
     * @com.intel.drl.spec_ref
     */
    public PatternSyntaxException(String message, String pattern, int index) {
        this.pattern = pattern;
        this.message = message;
        this.index = index;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getMessage() {
        String filler = "";
        if (index >= 1) {
            char[] temp = new char[index];
            Arrays.fill(temp, ' ');
            filler = new String(temp);
        }
        return message
                + ((pattern != null && pattern.length() != 0) ? " near index: "
                        + index + "\n" + pattern + "\n" + filler + "^" : "");
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getDescription() {
        return message;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getIndex() {
        return index;
    }
}
