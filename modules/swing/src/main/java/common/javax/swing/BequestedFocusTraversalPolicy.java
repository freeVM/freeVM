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
 * @author Alexander T. Simbirtsev
 * @version $Revision$
 * Created on 07.07.2005
 *
 */
package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

class BequestedFocusTraversalPolicy extends FocusTraversalPolicy {

    private final FocusTraversalPolicy ancestor;
    private final Component fixedComponent;
    private final Component fixedNextComponent;

    /**
     * Creates <code>FocusTraversalPolicy</code> that inherits all values
     * returned by existing <code>FocusTraversalPolicy</code> and overlaps
     * only two of them: value returned by <code>getComponentAfter()</code> for
     * <code>fixedComponent</code> and <code>getComponentBefore()</code> for
     * <code>fixedNextComponent</code>.
     * @throws <code>IllegalArgumentException</code> if <code>ancestor</code> is <code>null</code>
     */
    public BequestedFocusTraversalPolicy(final FocusTraversalPolicy ancestor,
                                         final Component fixedComponent,
                                         final Component fixedNextComponent) {
        super();
        this.ancestor = ancestor;
        if (this.ancestor == null) {
            throw new IllegalArgumentException("Ancestor shouldn't be null");
        }
        this.fixedComponent = fixedComponent;
        this.fixedNextComponent = fixedNextComponent;
    }

    /**
     * returns <code>fixedNextComponent</code> for <code>fixedComponent</code> or
     * delegates call to <code>ancestor</code>
     */
    public Component getComponentAfter(final Container container, final Component c) {
        if (c == fixedComponent) {
            return fixedNextComponent;
        }
        return ancestor.getComponentAfter(container, c);
    }

    /**
     * returns <code>fixedComponent</code> for <code>fixedNextComponent</code> or
     * delegates call to <code>ancestor</code>
     */
    public Component getComponentBefore(final Container container, final Component c) {
        if (c == fixedNextComponent) {
            return fixedComponent;
        }
        return ancestor.getComponentBefore(container, c);
    }

    /**
     * delegates call to <code>ancestor</code>
     */
    public Component getDefaultComponent(final Container container) {
        return ancestor.getDefaultComponent(container);
    }

    /**
     * delegates call to <code>ancestor</code>
     */
    public Component getFirstComponent(final Container container) {
        return ancestor.getFirstComponent(container);
    }

    /**
     * delegates call to <code>ancestor</code>
     */
    public Component getLastComponent(final Container container) {
        return ancestor.getLastComponent(container);
    }
}
