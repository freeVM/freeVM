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
 * @version $Revision: 1.7.2.2 $
 */
package java.util.regex;

import java.util.Arrays;

import org.apache.harmony.regex.internal.nls.Messages;

/**
 * Encapsulates a syntax error that occurred during the compilation of a
 * {@link Pattern}. Might include a detailed description, the original regular
 * expression, and the index at which the error occurred.
 *
 * @see Pattern#compile(String)
 * @see Pattern#compile(java.lang.String,int)
 *
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.7.2.2 $
 */
public class PatternSyntaxException extends IllegalArgumentException {
    
    private static final long serialVersionUID = -3864639126226059218L;

    /**
     * Holds the description of the syntax error, or null if the description is
     * not known.
     */
    private String desc;

    /**
     * Holds the syntactically incorrect regular expression, or null if the
     * regular expression is not known.
     */
    private String pattern;

    /**
     * Holds the index around which the error occured, or -1, in case it is
     * unknown.
     */
    private int index = -1;

    /**
     * Creates a new PatternSyntaxException for a given message, pattern, and
     * error index.
     *
     * @param description
     *            the description of the syntax error, or {@code null} if the
     *            description is not known.
     * @param pattern
     *            the syntactically incorrect regular expression, or
     *            {@code null} if the regular expression is not known.
     * @param index
     *            the character index around which the error occurred, or -1 if
     *            the index is not known.
     */
    public PatternSyntaxException(String description, String pattern, int index) {
        this.desc = description;
        this.pattern = pattern;
        this.index = index;
    }

    /**
     * Returns the syntactically incorrect regular expression.
     *
     * @return the regular expression.
     *
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns a detailed error message for the exception. The message is
     * potentially multi-line, and it might include a detailed description, the
     * original regular expression, and the index at which the error occured.
     *
     * @return the error message.
     */
    public String getMessage() {
        String filler = ""; //$NON-NLS-1$
        if (index >= 1) {
            char[] temp = new char[index];
            Arrays.fill(temp, ' ');
            filler = new String(temp);
        }
        return desc
                + ((pattern != null && pattern.length() != 0) ? Messages.getString("regex.07", //$NON-NLS-1$
                        new Object[]{index, pattern, filler}) : ""); //$NON-NLS-1$
    }

    /**
     * Returns the description of the syntax error, or {@code null} if the
     * description is not known.
     *
     * @return the description.
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Returns the character index around which the error occurred, or -1 if the
     * index is not known.
     *
     * @return the index.
     *
     */
    public int getIndex() {
        return index;
    }
}
