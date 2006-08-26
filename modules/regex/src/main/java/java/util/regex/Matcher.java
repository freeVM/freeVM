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
 * @version $Revision: 1.22.2.2 $
 */
package java.util.regex;

import java.util.ArrayList;

/**
 * @com.intel.drl.spec_ref
 * 
 * Note: main functionality of this class is hidden into nodes match methods. 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.22.2.2 $
 */
public final class Matcher implements MatchResult {

    static int MODE_FIND = 1 << 0;

    static int MODE_MATCH = 1 << 1;

    private Pattern pat = null;

    private AbstractSet start = null;

    private CharSequence string = null;

    private MatchResultImpl matchResult = null;

    // bounds
    private int leftBound = -1;

    private int rightBound = -1;

    // replacements
    private int appendPos = 0;

    private String replacement = null;

    private String processedRepl = null;

    private ArrayList replacementParts = null;

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {
        processedRepl = processReplacement(replacement);
        sb.append(string.subSequence(appendPos, start()));
        sb.append(processedRepl);
        appendPos = end();
        return this;
    }

    /**
     * Parses replacement string and creates pattern
     */
    private String processReplacement(String replacement) {
        if (this.replacement != null 
                && this.replacement.equals(replacement)) {
            if (replacementParts == null) {
                return processedRepl;
            } else {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < replacementParts.size(); i++) {
                    sb.append(replacementParts.get(i));
                }

                return sb.toString();
            }
        } else {
            this.replacement = replacement;
            char[] repl = replacement.toCharArray();
            StringBuffer res = new StringBuffer();
            replacementParts = null;

            int index = 0;
            int replacementPos = 0;
            boolean nextBackSlashed = false;

            while (index < repl.length) {
                
                if (repl[index] == '\\' && !nextBackSlashed) {
                    nextBackSlashed = true;
                    index++;
                } 
               
                if (nextBackSlashed) {
                    res.append(repl[index]);
                    nextBackSlashed = false;
                } else {
                    if (repl[index] == '$') {
                        if (replacementParts == null) {
                            replacementParts = new ArrayList();
                        }
                        try {
                            final int gr = Integer.parseInt(new String(
                                    repl, ++index, 1));

                            if (replacementPos != res.length()) {
                                replacementParts.add(res.subSequence(
                                        replacementPos, res.length()));
                                replacementPos = res.length();
                            }

                            replacementParts.add(new Object() {
                                private final int grN = gr;

                                public String toString() {
                                    return group(grN);
                                }
                            });
                            String group = group(gr);
                            replacementPos += group.length();
                            res.append(group);

                        } catch (IndexOutOfBoundsException iob) {
                            throw iob;
                        } catch (Exception e) {
                            throw new IllegalArgumentException(I18n
                                    .getMessage("Illegal group reference"));
                        }
                    } else {
                        res.append(repl[index]);
                    }
                }

                index++;
            }

            if (replacementParts != null && replacementPos != res.length()) {
                replacementParts.add(res.subSequence(replacementPos, res
                        .length()));
            }
            return res.toString();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher reset(CharSequence newSequence) {
        if (newSequence == null) {
            throw new NullPointerException(I18n.getMessage("Incorrect input"));
        }
        this.string = newSequence;
        return reset();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher reset() {
        this.leftBound = 0;
        this.rightBound = string.length();
        matchResult.reset(string, leftBound, rightBound);
        appendPos = 0;
        replacement = null;
        matchResult.previousMatch = -1;
        return this;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher region(int leftBound, int rightBound) {

        if (leftBound > rightBound || leftBound < 0 || rightBound < 0
                || leftBound > string.length() || rightBound > string.length()) {
            throw new IndexOutOfBoundsException(I18n.getFormattedMessage(
                    "Region indices out of bounds: " + "from {0} to {1}",
                    Integer.toString(leftBound), Integer.toString(rightBound)));
        }

        this.leftBound = leftBound;
        this.rightBound = rightBound;
        matchResult.reset(null, leftBound, rightBound);
        appendPos = 0;
        replacement = null;

        return this;
    }

	/**
	 * TODO: appendTail(StringBuffer) javadoc
	 * 
	 * @param sb
	 * @return
	 */
    public StringBuffer appendTail(StringBuffer sb) {
        return sb.append(string.subSequence(appendPos, string.length()));
    }

	/**
	 * This is very similar to replaceAll except only the first occurrence of a
	 * sequence matching the pattern is replaced.
	 * 
	 * @param replacement
	 *            A string to replace occurrences of character sequences
	 *            matching the pattern.
	 * @return A new string with replacements inserted
	 */
    public String replaceFirst(String replacement) {
        reset();
        if (find()) {
            StringBuffer sb = new StringBuffer();
            appendReplacement(sb, replacement);
            return appendTail(sb).toString();
        }

        return string.toString();

    }

	/**
	 * Replace all occurrences of character sequences which match the pattern
	 * with the given replacement string. The replacement string may refer to
	 * capturing groups using the syntax "$<group number>".
	 * 
	 * @param replacement
	 *            A string to replace occurrences of character sequences
	 *            matching the pattern.
	 * @return A new string with replacements inserted
	 */
    public String replaceAll(String replacement) {
        StringBuffer sb = new StringBuffer();
        reset();
        while (find()) {
            appendReplacement(sb, replacement);
        }

        return appendTail(sb).toString();
    }

	/**
	 * Return a reference to the pattern used by this Matcher.
	 * 
	 * @return A reference to the pattern used by this Matcher.
	 */
    public Pattern pattern() {
        return pat;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String group(int groupIndex) {
        return matchResult.group(groupIndex);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String group() {
        return group(0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean find(int startIndex) {
        int stringLength = string.length();
        if (startIndex < 0 || startIndex > stringLength)
            throw new IndexOutOfBoundsException(I18n.getFormattedMessage(
                    "Start index " + "out of bounds: {0}", new Integer(
                            startIndex)));

        startIndex = findAt(startIndex);
        if (startIndex >= 0 && matchResult.isValid()) {
            matchResult.finalizeMatch();
            return true;
        }
        matchResult.startIndex = -1;
        return false;
    }

    private int findAt(int startIndex) {
        matchResult.reset();
        matchResult.setMode(Matcher.MODE_FIND);
        matchResult.setStartIndex(startIndex);
        return start.find(startIndex, string, matchResult);
    }

	/**
	 * The find() method matches the pattern against the character sequence
	 * beginning at the character after the last match or at the beginning of
	 * the sequence if called immediately after reset(). The method returns true
	 * if and only if a match is found.
	 * 
	 * @return A boolean indicating if the pattern was matched.
	 */
    public boolean find() {
        int length = string.length();
        if (!hasTransparentBounds())
            length = rightBound;
        if (matchResult.startIndex >= 0
                && matchResult.mode() == Matcher.MODE_FIND) {
            matchResult.startIndex = matchResult.end();
            if (matchResult.end() == matchResult.start()) {
                matchResult.startIndex++;
            }

            return matchResult.startIndex <= length ? find(matchResult.startIndex)
                    : false;
        } else {
            return find(leftBound);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int start(int groupIndex) {
        return matchResult.start(groupIndex);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int end(int groupIndex) {
        return matchResult.end(groupIndex);
    }

	/**
	 * This method is identical in function to the Pattern.matches() method. It
	 * returns true if and only if the regular expression pattern matches the
	 * entire input character sequence.
	 * 
	 * @return A boolean indicating if the pattern matches the entire input
	 *         character sequence.
	 */
    public boolean matches() {
        return lookingAt(leftBound, Matcher.MODE_MATCH);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static String quoteReplacement(String string) {
        // first check whether we have smth to quote
        if (string.indexOf('\\') < 0 && string.indexOf('$') < 0)
            return string;
        StringBuffer res = new StringBuffer(string.length() * 2);
        char ch;
        int len = string.length();

        for (int i = 0; i < len; i++) {

            switch (ch = string.charAt(i)) {
            case '$':
                res.append('\\');
                res.append('$');
                break;
            case '\\':
                res.append('\\');
                res.append('\\');
                break;
            default:
                res.append(ch);
            }
        }

        return res.toString();
    }

    /**
     * Runs match starting from <code>set</code> specified against input
     * sequence starting at <code>index</code> specified; Result of the match
     * will be stored into matchResult instance;
     */
    private boolean runMatch(AbstractSet set, int index,
            MatchResultImpl matchResult) {

        if (set.matches(index, string, matchResult) >= 0) {
            matchResult.finalizeMatch();
            return true;
        }

        return false;
    }

	/**
	 * This method attempts to match the pattern against the character sequence
	 * starting at the beginning. If the pattern matches even a prefix of the
	 * input character sequence, lookingAt() will return true. Otherwise it will
	 * return false.
	 * 
	 * @return A boolean indicating if the pattern matches a prefix of the input
	 *         character sequence.
	 */
    public boolean lookingAt() {
        return lookingAt(leftBound, Matcher.MODE_FIND);
    }

    private boolean lookingAt(int startIndex, int mode) {
        matchResult.reset();
        matchResult.setMode(mode);
        matchResult.setStartIndex(startIndex);
        return runMatch(start, startIndex, matchResult);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int start() {
        return start(0);
    }

	/**
	 * Return the number of capturing groups in the pattern.
	 * 
	 * @return The number of capturing groups in the pattern.
	 */
    public int groupCount() {
        return matchResult.groupCount();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int end() {
        return end(0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public MatchResult toMatchResult() {
        return this.matchResult.cloneImpl();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher useAnchoringBounds(boolean value) {
        matchResult.useAnchoringBounds(value);
        return this;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean hasAnchoringBounds() {
        return matchResult.hasAnchoringBounds();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher useTransparentBounds(boolean value) {
        matchResult.useTransparentBounds(value);
        return this;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean hasTransparentBounds() {
        return matchResult.hasTransparentBounds();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int regionStart() {
        return matchResult.getLeftBound();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int regionEnd() {
        return matchResult.getRightBound();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean requireEnd() {
        return matchResult.requireEnd;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean hitEnd() {
        return matchResult.hitEnd;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Matcher usePattern(Pattern pat) {
        int startIndex = matchResult.getPreviousMatchEnd();
        int mode = matchResult.mode();
        this.pat = pat;
        this.start = pat.start;
        matchResult = new MatchResultImpl(this.string, leftBound, rightBound,
                pat.groupCount(), pat.compCount(), pat.consCount());
        matchResult.setStartIndex(startIndex);
        matchResult.setMode(mode);
        return this;
    }

    Matcher(Pattern pat, CharSequence cs) {
        this.pat = pat;
        this.start = pat.start;
        this.string = cs;
        this.leftBound = 0;
        this.rightBound = string.length();
        matchResult = new MatchResultImpl(cs, leftBound, rightBound, pat
                .groupCount(), pat.compCount(), pat.consCount());
    }
}
