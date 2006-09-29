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
 * @version $Revision$
 */
package javax.swing.text;

import javax.swing.Action;
import javax.swing.KeyStroke;

public interface Keymap {
    String getName();
    Action getDefaultAction();
    void setDefaultAction(Action a);
    Action getAction(KeyStroke key);
    KeyStroke[] getBoundKeyStrokes();
    Action[] getBoundActions();
    KeyStroke[] getKeyStrokesForAction(Action a);
    boolean isLocallyDefined(KeyStroke key);
    void addActionForKeyStroke(KeyStroke key, Action a);
    void removeKeyStrokeBinding(KeyStroke keys);
    void removeBindings();
    Keymap getResolveParent();
    void setResolveParent(Keymap parent);
}
