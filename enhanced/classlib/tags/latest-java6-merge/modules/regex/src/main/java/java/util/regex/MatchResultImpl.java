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
 */
package java.util.regex;

import java.util.Arrays;

import org.apache.harmony.regex.internal.nls.Messages;

/**
 * Match result implementation
 * Note: probably it might make sense to combine this class with Matcher.
 *  
 * @author Nikolay A. Kuznetsov
 */
class MatchResultImpl implements MatchResult {

    private int[] groupBounds = null;

    private int[] consumers = null;

    private int[] compQuantCounters = null;

    private CharSequence string = null;

    private int groupCount = 0;

    private boolean valid = false;

    private int leftBound;

    private int rightBound;

    int startIndex;

    private boolean transparentBounds = false;

    private boolean anchoringBounds = false;

    boolean hitEnd = false;

    boolean requireEnd = false;

    int previousMatch = -1;

    private int mode;

    MatchResultImpl(CharSequence string, int leftBound, int rightBound,
            int groupCount, int compQuantCount, int consumersCount) {
        this.groupCount = ++groupCount;
        this.groupBounds = new int[groupCount * 2];

        this.consumers = new int[consumersCount];
        Arrays.fill(consumers, -1);

        if (compQuantCount > 0)
            this.compQuantCounters = new int[compQuantCount];
        Arrays.fill(groupBounds, -1);
        reset(string, leftBound, rightBound);
    }

    MatchResult cloneImpl() {
        MatchResultImpl res = new MatchResultImpl(this.string, this.leftBound,
                this.rightBound, this.groupCount - 1, 0, 0);

        res.valid = valid;
        if (valid) {
            System.arraycopy(groupBounds, 0, res.groupBounds, 0,
                    this.groupBounds.length);
        }
        return res;
    }

    public void setConsumed(int counter, int value) {
        this.consumers[counter] = value;
    }

    public int getConsumed(int counter) {
        return this.consumers[counter];
    }

    public int end() {
        return end(0);
    }

    public int end(int group) {
        checkGroup(group);
        return groupBounds[group * 2 + 1];
    }

    void setStart(int group, int offset) {
        groupBounds[group * 2] = offset;
    }

    void setEnd(int group, int offset) {
        groupBounds[group * 2 + 1] = offset;
    }

    int getStart(int group) {
        return groupBounds[group * 2];
    }

    int getEnd(int group) {
        return groupBounds[group * 2 + 1];
    }

    public String group() {
        return group(0);
    }

    public String group(int group) {
        if (start(group) < 0)
            return null;
        return string.subSequence(start(group), end(group)).toString();
    }

    String getGroupNoCheck(int group) {
        int st = getStart(group);
        int end = getEnd(group);
        if ((end | st | (end - st)) < 0 || end > string.length())
            return null;

        return string.subSequence(st, end).toString();
    }

    public int groupCount() {
        return groupCount - 1;
    }

    public int start() {
        return start(0);
    }

    public int start(int group) {
        checkGroup(group);
        return groupBounds[group * 2];
    }

    /*
     * This method being called after any successful match; For now it's being
     * used to check zero group for empty match;
     */
    public void finalizeMatch() {
        if (this.groupBounds[0] == -1) {
            this.groupBounds[0] = this.startIndex;
            this.groupBounds[1] = this.startIndex;
        }

        previousMatch = end();
    }

    public int getEnterCounter(int setCounter) {
        return compQuantCounters[setCounter];
    }

    public void setEnterCounter(int setCounter, int value) {
        compQuantCounters[setCounter] = value;
    }

    private void checkGroup(int group) {
        if (!valid) {
            throw new IllegalStateException(Messages.getString("regex.05")); //$NON-NLS-1$
        }

        if (group < 0 || group > groupCount) {
            throw new IndexOutOfBoundsException(
                    Messages.getString("regex.06", group)); //$NON-NLS-1$
        }
    }

    void updateGroup(int index, int srtOffset, int endOffset) {
        checkGroup(index);
        groupBounds[index * 2] = srtOffset;
        groupBounds[index * 2 + 1] = endOffset;
    }

    protected void setValid() {
        this.valid = true;
    }

    protected boolean isValid() {
        return this.valid;
    }

    protected void reset(CharSequence newSequence, int leftBound, int rightBound) {
        valid = false;
        mode = Matcher.MODE_MATCH;
        Arrays.fill(groupBounds, -1);
        Arrays.fill(consumers, -1);

        if (newSequence != null)
            this.string = newSequence;
        if (leftBound >= 0)
            this.setBounds(leftBound, rightBound);
        this.startIndex = this.leftBound;
    }

    protected void reset() {
        reset(null, -1, -1);
    }

    private void setBounds(int leftBound, int rightBound) {
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    protected void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        previousMatch = previousMatch >= 0 ? previousMatch : startIndex;
    }

    public int getLeftBound() {
        return this.leftBound;
    }

    public int getRightBound() {
        return this.rightBound;
    }

    protected void setMode(int mode) {
        this.mode = mode;
    }

    protected int mode() {
        return mode;
    }

    protected void useAnchoringBounds(boolean value) {
        this.anchoringBounds = value;
    }

    protected boolean hasAnchoringBounds() {
        return this.anchoringBounds;
    }

    protected void useTransparentBounds(boolean value) {
        this.transparentBounds = value;
    }

    protected boolean hasTransparentBounds() {
        return this.transparentBounds;
    }

    int getPreviousMatchEnd() {
        return previousMatch;
    }
}
