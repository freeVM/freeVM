/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Pavel Dolgov
 * @version $Revision$
 */
package java.awt.im;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.Locale;

public class InputContext {
    protected InputContext() {
    }

    public static InputContext getInstance() {
        return null;
    }

    public void dispatchEvent(AWTEvent event) {
    }

    public void dispose() {
    }

    public void endComposition() {
    }

    public Object getInputMethodControlObject() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public boolean isCompositionEnabled() {
        return false;
    }

    public void reconvert() {
    }

    public void removeNotify(Component client) {
    }

    public boolean selectInputMethod(Locale locale) {
        return false;
    }

    public void setCharacterSubsets(Character.Subset[] subsets) {
    }

    public void setCompositionEnabled(boolean enable) {
    }
}

