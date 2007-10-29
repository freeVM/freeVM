/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Anton Avtamonov
 * @version $Revision: $
 */
package org.apache.harmony.x.swing;

import java.awt.Font;

public abstract class AbstractExtendedListElement implements ExtendedListElement {
    private final Object value;
    private final Font font;

    private boolean enabled = true;
    private boolean choosable = true;
    private String toolTipText;
    private int indentationLevel;

    public AbstractExtendedListElement(final Object value, final Font font) {
        this.value = value;
        this.font = font;
    }

    public Font getFont() {
        return font;
    }

    public void setChoosable(final boolean choosable) {
        this.choosable = choosable;
    }

    public boolean isChoosable() {
        return choosable && isEnabled();
    }

    public void setIndentationLevel(final int level) {
        indentationLevel = level;
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setToolTipText(final String toolTip) {
        toolTipText = toolTip;
    }

    public String getToolTipText() {
        return toolTipText;
    }

    public String toString() {
        return value != null ? value.toString() : "";
    }
}
