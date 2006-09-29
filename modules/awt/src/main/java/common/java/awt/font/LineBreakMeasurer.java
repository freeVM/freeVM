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
/*
 * @author Oleg V. Khaschansky
 * @version $Revision$
 */

package java.awt.font;

import java.text.AttributedCharacterIterator;
import java.text.BreakIterator;

public final class LineBreakMeasurer {
    private TextMeasurer tm = null;
    private BreakIterator bi = null;
    private int position = 0;
    int maxpos = 0;

    public LineBreakMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
        this(text, BreakIterator.getLineInstance(), frc);
    }

    public LineBreakMeasurer(
            AttributedCharacterIterator text,
            BreakIterator bi,
            FontRenderContext frc
    ) {
        tm = new TextMeasurer(text, frc);
        this.bi = bi;
        this.bi.setText(text);
        position = text.getBeginIndex();
        maxpos = tm.aci.getEndIndex();
    }

    public void deleteChar(AttributedCharacterIterator newText, int pos) {
        tm.deleteChar(newText, pos);
        bi.setText(newText);

        position = newText.getBeginIndex();

        maxpos--;
    }

    public int getPosition() {
        return position;
    }

    public void insertChar(AttributedCharacterIterator newText, int pos) {
        tm.insertChar(newText, pos);
        bi.setText(newText);

        position = newText.getBeginIndex();

        maxpos++;
    }

    public TextLayout nextLayout(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
        if (position == maxpos) {
            return null;
        }

        int nextPosition = nextOffset(wrappingWidth, offsetLimit, requireNextWord);

        if (nextPosition == position) {
            return null;
        }
        TextLayout layout = tm.getLayout(position, nextPosition);
        position = nextPosition;
        return layout;
    }

    public TextLayout nextLayout(float wrappingWidth) {
        return nextLayout(wrappingWidth, maxpos, false);
    }

    public int nextOffset(float wrappingWidth) {
        return nextOffset(wrappingWidth, maxpos, false);
    }

    public int nextOffset(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
        if (offsetLimit <= position) {
            throw new IllegalArgumentException(
                    "Offset limit should be greater than current position."
            );
        }

        if (position == maxpos) {
            return position;
        }

        int breakPos = tm.getLineBreakIndex(position, wrappingWidth);
        int correctedPos = breakPos;

        // This check is required because bi.preceding(maxpos) throws an exception
        if (breakPos == maxpos) {
            correctedPos = maxpos;
        } else if (Character.isWhitespace(bi.getText().setIndex(breakPos))) {
            correctedPos = bi.following(breakPos);
        } else {
            correctedPos = bi.preceding(breakPos);
        }

        if (position >= correctedPos) {
            if (requireNextWord) {
                correctedPos = position;
            } else {
                correctedPos = Math.max(position+1, breakPos);
            }
        }

        return Math.min(correctedPos, offsetLimit);
    }

    public void setPosition(int pos) {
        if (tm.aci.getBeginIndex() > pos || maxpos < pos) {
            throw new IllegalArgumentException("Index is out of range");
        }
        position = pos;
    }
}

