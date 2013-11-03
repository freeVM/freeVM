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

package java.util.regex;

import java.util.ArrayList;

import org.apache.harmony.regex.internal.nls.Messages;

/**
 * Provides a means of matching regular expressions against a given input,
 * finding occurrences of regular expressions in a given input, or replacing
 * parts of a given input. A {@code Matcher} instance has an associated {@link
 * Pattern} instance and an input text. A typical use case is to
 * iteratively find all occurrences of the {@code Pattern}, until the end of
 * the input is reached, as the following example illustrates:
 *
 * <p/>
 *
 * <pre>
 * Pattern p = Pattern.compile("[A-Za-z]+");
 *
 * Matcher m = p.matcher("Hello, Android!");
 * while (m.find()) {
 *     System.out.println(m.group()); // prints "Hello" and "Android"
 * }
 * </pre>
 *
 * <p/>
 *
 * The {@code Matcher} has a state that results from the previous operations.
 * For example, it knows whether the most recent attempt to find the
 * {@code Pattern} was successful and at which position the next attempt would
 * resume the search. Depending on the application's needs, it may become
 * necessary to explicitly {@link #reset()} this state from time to time.
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
     * Appends a literal part of the input plus a replacement for the current
     * match to a given {@link StringBuffer}. The literal part is exactly the
     * part of the input between the previous match and the current match. The
     * method can be used in conjunction with {@link #find()} and
     * {@link #appendTail(StringBuffer)} to walk through the input and replace
     * all occurrences of the {@code Pattern} with something else.
     *
     * @param buffer
     *            the {@code StringBuffer} to append to.
     * @param replacement
     *            the replacement text.
     * @return the {@code Matcher} itself.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public Matcher appendReplacement(StringBuffer buffer, String replacement) {
        processedRepl = processReplacement(replacement);
        buffer.append(string.subSequence(appendPos, start()));
        buffer.append(processedRepl);
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
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < replacementParts.size(); i++) {
                    sb.append(replacementParts.get(i));
                }

                return sb.toString();
            }
        } else {
            this.replacement = replacement;
            char[] repl = replacement.toCharArray();
            StringBuilder res = new StringBuilder();
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

                            replacementParts.add(new Object() {  //$NON-LOCK-1$
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
                            throw new IllegalArgumentException(
                                    Messages.getString("regex.00")); //$NON-NLS-1$
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
     * Provides a new input and resets the {@code Matcher}. This results in the
     * region being set to the whole input. Results of a previous find get lost.
     * The next attempt to find an occurrence of the {@link Pattern} in the
     * string will start at the beginning of the input.
     *
     * @param input
     *            the new input sequence.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher reset(CharSequence input) {
        if (input == null) {
            throw new NullPointerException(Messages.getString("regex.01")); //$NON-NLS-1$
        }
        this.string = input;
        return reset();
    }

    /**
     * Resets the {@code Matcher}. This results in the region being set to the
     * whole input. Results of a previous find get lost. The next attempt to
     * find an occurrence of the {@link Pattern} in the string will start at the
     * beginning of the input.
     *
     * @return the {@code Matcher} itself.
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
     * Resets this matcher and sets a region. Only characters inside the region
     * are considered for a match.
     *
     * @param start
     *            the first character of the region.
     * @param end
     *            the first character after the end of the region.
     * @return the {@code Matcher} itself.
     */
    public Matcher region(int start, int end) {

        if (start > end || start < 0 || end < 0
                || start > string.length() || end > string.length()) {
            throw new IndexOutOfBoundsException( Messages.getString("regex.02", //$NON-NLS-1$
                            Integer.toString(start), Integer.toString(end)));
        }

        this.leftBound = start;
        this.rightBound = end;
        matchResult.reset(null, start, end);
        appendPos = 0;
        replacement = null;

        return this;
    }

    /**
     * Appends the (unmatched) remainder of the input to the given
     * {@link StringBuffer}. The method can be used in conjunction with
     * {@link #find()} and {@link #appendReplacement(StringBuffer, String)} to
     * walk through the input and replace all matches of the {@code Pattern}
     * with something else.
     *
     * @param buffer
     *            the {@code StringBuffer} to append to.
     * @return the {@code StringBuffer}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public StringBuffer appendTail(StringBuffer buffer) {
        return buffer.append(string.subSequence(appendPos, string.length()));
    }

    /**
     * Replaces the first occurrence of this matcher's pattern in the input with
     * a given string.
     *
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
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
     * Replaces all occurrences of this matcher's pattern in the input with a
     * given string.
     *
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
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
     * Returns the {@link Pattern} instance used inside this matcher.
     *
     * @return the {@code Pattern} instance.
     */
    public Pattern pattern() {
        return pat;
    }

    /**
     * Returns the text that matched a given group of the regular expression.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the text that matched the group.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public String group(int group) {
        return matchResult.group(group);
    }

    /**
     * Returns the text that matched the whole regular expression.
     *
     * @return the text.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public String group() {
        return group(0);
    }

    /**
     * Returns the next occurrence of the {@link Pattern} in the input. The
     * method starts the search from the given character in the input.
     *
     * @param start
     *            The index in the input at which the find operation is to
     *            begin. If this is less than the start of the region, it is
     *            automatically adjusted to that value. If it is beyond the end
     *            of the region, the method will fail.
     * @return true if (and only if) a match has been found.
     */
    public boolean find(int start) {
        int stringLength = string.length();
        if (start < 0 || start > stringLength) {
            throw new IndexOutOfBoundsException(Messages.getString("regex.03", //$NON-NLS-1$ 
                    Integer.valueOf(start)));
        }

        start = findAt(start);
        if (start >= 0 && matchResult.isValid()) {
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
        int foundIndex = start.find(startIndex, string, matchResult);
        if (foundIndex == -1) {
            matchResult.hitEnd = true;
        }
        return foundIndex;
    }

    /**
     * Returns the next occurrence of the {@link Pattern} in the input. If a
     * previous match was successful, the method continues the search from the
     * first character following that match in the input. Otherwise it searches
     * either from the region start (if one has been set), or from position 0.
     *
     * @return true if (and only if) a match has been found.
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
     * Returns the index of the first character of the text that matched a given
     * group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int start(int group) {
        return matchResult.start(group);
    }

    /**
     * Returns the index of the first character following the text that matched
     * a given group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int end(int group) {
        return matchResult.end(group);
    }

    /**
     * Tries to match the {@link Pattern} against the entire region (or the
     * entire input, if no region has been set).
     *
     * @return true if (and only if) the {@code Pattern} matches the entire
     *         region.
     */
    public boolean matches() {
        return lookingAt(leftBound, Matcher.MODE_MATCH);
    }

    /**
     * Returns a replacement string for the given one that has all backslashes
     * and dollar signs escaped.
     *
     * @param s
     *            the input string.
     * @return the input string, with all backslashes and dollar signs having
     *         been escaped.
     */
    public static String quoteReplacement(String s) {
        // first check whether we have smth to quote
        if (s.indexOf('\\') < 0 && s.indexOf('$') < 0)
            return s;
        StringBuilder res = new StringBuilder(s.length() * 2);
        char ch;
        int len = s.length();

        for (int i = 0; i < len; i++) {

            switch (ch = s.charAt(i)) {
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
     * Tries to match the {@link Pattern}, starting from the beginning of the
     * region (or the beginning of the input, if no region has been set).
     * Doesn't require the {@code Pattern} to match against the whole region.
     *
     * @return true if (and only if) the {@code Pattern} matches.
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
     * Returns the index of the first character of the text that matched the
     * whole regular expression.
     *
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int start() {
        return start(0);
    }

    /**
     * Returns the number of groups in the results, which is always equal to
     * the number of groups in the original regular expression.
     *
     * @return the number of groups.
     */
    public int groupCount() {
        return matchResult.groupCount();
    }

    /**
     * Returns the index of the first character following the text that matched
     * the whole regular expression.
     *
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int end() {
        return end(0);
    }

    /**
     * Converts the current match into a separate {@link MatchResult} instance
     * that is independent from this matcher. The new object is unaffected when
     * the state of this matcher changes.
     *
     * @return the new {@code MatchResult}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public MatchResult toMatchResult() {
        return this.matchResult.cloneImpl();
    }

    /**
     * Determines whether this matcher has anchoring bounds enabled or not. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     *
     * @param value
     *            the new value for anchoring bounds.
     * @return the {@code Matcher} itself.
     */
    public Matcher useAnchoringBounds(boolean value) {
        matchResult.useAnchoringBounds(value);
        return this;
    }

    /**
     * Indicates whether this matcher has anchoring bounds enabled. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     *
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
     */
    public boolean hasAnchoringBounds() {
        return matchResult.hasAnchoringBounds();
    }

    /**
     * Determines whether this matcher has transparent bounds enabled or not.
     * When transparent bounds are enabled, the parts of the input outside the
     * region are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     *
     * @param value
     *            the new value for transparent bounds.
     * @return the {@code Matcher} itself.
     */
    public Matcher useTransparentBounds(boolean value) {
        matchResult.useTransparentBounds(value);
        return this;
    }

    /**
     * Indicates whether this matcher has transparent bounds enabled. When
     * transparent bounds are enabled, the parts of the input outside the region
     * are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     *
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
     */
    public boolean hasTransparentBounds() {
        return matchResult.hasTransparentBounds();
    }

    /**
     * Returns this matcher's region start, that is, the first character that is
     * considered for a match.
     *
     * @return the start of the region.
     */
    public int regionStart() {
        return matchResult.getLeftBound();
    }

    /**
     * Returns this matcher's region end, that is, the first character that is
     * not considered for a match.
     *
     * @return the end of the region.
     */
    public int regionEnd() {
        return matchResult.getRightBound();
    }

    /**
     * Indicates whether more input might change a successful match into an
     * unsuccessful one.
     *
     * @return true if (and only if) more input might change a successful match
     *         into an unsuccessful one.
     */
    public boolean requireEnd() {
        return matchResult.requireEnd;
    }

    /**
     * Indicates whether the last match hit the end of the input.
     *
     * @return true if (and only if) the last match hit the end of the input.
     */
    public boolean hitEnd() {
        return matchResult.hitEnd;
    }

    /**
     * Sets a new pattern for the {@code Matcher}. Results of a previous find
     * get lost. The next attempt to find an occurrence of the {@link Pattern}
     * in the string will start at the beginning of the input.
     *
     * @param pattern
     *            the new {@code Pattern}.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher usePattern(Pattern pattern) {
    	if (pattern == null) {
    		throw new IllegalArgumentException(Messages.getString("regex.1B"));
    	}
        int startIndex = matchResult.getPreviousMatchEnd();
        int mode = matchResult.mode();
        this.pat = pattern;
        this.start = pattern.start;
        matchResult = new MatchResultImpl(this.string, leftBound, rightBound,
                pattern.groupCount(), pattern.compCount(), pattern.consCount());
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

    @Override
    public String toString() {
        String lastMatch = "";
        try {
            lastMatch = Integer.toString(start());
        } catch (IllegalStateException e) {
        }
        return getClass().getCanonicalName() + "[pattern=" + pat + " region="
                + matchResult.getLeftBound() + ","
                + matchResult.getRightBound() + " lastmatch=" + lastMatch + "]";
    }
}
