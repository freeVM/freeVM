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
 * @author Evgeniya G. Maenkova
 * @version $Revision$
 */
package javax.swing.text;

import org.apache.harmony.awt.text.RootViewContext;
import org.apache.harmony.awt.text.TextCaret;
import org.apache.harmony.awt.text.TextFactory;

final class TextFactoryImpl extends TextFactory {
    public RootViewContext createRootView(final Element e) {
        return new RootView(e).rootViewContext;
    }

    public View createPlainView(final Element e) {
        return new PlainView(e);
    }

    public View createWrappedPlainView(final Element e) {
        return new WrappedPlainView(e, true);
    }

    public View createFieldView(final Element e) {
        return new FieldView(e);
    }

    public View createPasswordView(final Element e) {
        return new PasswordView(e);
    }

    public TextCaret createCaret() {
        return new AWTCaret();
    }
}