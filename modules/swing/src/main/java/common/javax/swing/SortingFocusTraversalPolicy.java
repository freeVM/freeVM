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
 * @author Anton Avtamonov
 * @version $Revision$
 */

package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SortingFocusTraversalPolicy extends InternalFrameFocusTraversalPolicy {
    private Comparator comparator;
    private boolean isImplicitDownCycleTraversal = true;

    private final NextComponentFinder afterComponentFinder = new NextComponentFinder() {
        public Component findNextComponent(final List availableComponents, final int currentComponentIndex) {
            if (isImplicitDownCycleTraversal
                && currentComponent instanceof Container
                && ((Container)currentComponent).isFocusCycleRoot()) {

                Container innerCycleRoot = (Container)currentComponent;
                return innerCycleRoot.getFocusTraversalPolicy()
                                     .getDefaultComponent(innerCycleRoot);
            } else {
                int nextIndex = (currentComponentIndex + 1) % availableComponents.size();
                return (Component)availableComponents.get(nextIndex);
            }
        }
    };

    private final NextComponentFinder beforeComponentFinder = new NextComponentFinder() {
        public Component findNextComponent(final List availableComponents, final int currentComponentIndex) {
            int nextIndex = (currentComponentIndex + availableComponents.size() - 1) % availableComponents.size();
            return (Component)availableComponents.get(nextIndex);
        }
    };

    private final NextComponentFinder firstComponentFinder = new NextComponentFinder() {
        public Component findNextComponent(final List availableComponents, final int currentComponentIndex) {
            return (Component)availableComponents.get(0);
        }
    };

    private final NextComponentFinder lastComponentFinder = new NextComponentFinder() {
        public Component findNextComponent(final List availableComponents, final int currentComponentIndex) {
            return (Component)availableComponents.get(availableComponents.size() - 1);
        }
    };

    public SortingFocusTraversalPolicy(final Comparator comparator) {
        setComparator(comparator);
    }

    protected SortingFocusTraversalPolicy() {
    }


    public Component getComponentBefore(final Container focusCycleRoot, final Component component) {
        beforeComponentFinder.focusCycleRoot = focusCycleRoot;
        beforeComponentFinder.currentComponent = component;

        return getComponentBeforeOrAfter(beforeComponentFinder);
    }

    public Component getComponentAfter(final Container focusCycleRoot, final Component component) {
        afterComponentFinder.focusCycleRoot = focusCycleRoot;
        afterComponentFinder.currentComponent = component;

        return getComponentBeforeOrAfter(afterComponentFinder);
    }

    public Component getLastComponent(final Container focusCycleRoot) {
        lastComponentFinder.focusCycleRoot = focusCycleRoot;

        return getFirstOrLastComponent(lastComponentFinder);
    }

    public Component getFirstComponent(final Container focusCycleRoot) {
        firstComponentFinder.focusCycleRoot = focusCycleRoot;

        return getFirstOrLastComponent(firstComponentFinder);
    }

    public Component getDefaultComponent(final Container focusCycleRoot) {
        return getFirstComponent(focusCycleRoot);
    }

    public void setImplicitDownCycleTraversal(final boolean implicitDownCycleTraversal) {
        isImplicitDownCycleTraversal = implicitDownCycleTraversal;
    }

    public boolean getImplicitDownCycleTraversal() {
        return isImplicitDownCycleTraversal;
    }

    protected void setComparator(final Comparator c) {
        comparator = c;
    }

    protected Comparator getComparator() {
        return comparator;
    }

    protected boolean accept(final Component candidate) {
        return candidate.isVisible()
               && candidate.isDisplayable()
               && candidate.isEnabled()
               && candidate.isFocusable();
    }


    private List getAllAcceptableComponentsSorted(final Container focusCycleRoot,
                                            final Component currentComponent,
                                            final boolean includeCycleRootAndComponent) {
        assert comparator != null;

        List result = new LinkedList();

        if (!focusCycleRoot.isShowing()) {
            return result;
        }

        if (includeCycleRootAndComponent && accept(focusCycleRoot)) {
            result.add(focusCycleRoot);
        }

        collectAllAcceptableComponents(focusCycleRoot, result);
        if (includeCycleRootAndComponent
            && !result.isEmpty()
            && currentComponent != null
            && !result.contains(currentComponent)) {

            result.add(currentComponent);
        }
        Collections.sort(result, comparator);

        return result;
    }

    private List collectAllAcceptableComponents(final Container container, final List result) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (accept(component)) {
                result.add(component);
            }

            if (component.isDisplayable() && component.isVisible() && component instanceof Container && !((Container)component).isFocusCycleRoot()) {
                collectAllAcceptableComponents((Container)component, result);
            }
        }

        return result;
    }

    private Component getComponentBeforeOrAfter(final NextComponentFinder finder) {
        checkCycleRootIsNotNull(finder.focusCycleRoot);
        if (!finder.focusCycleRoot.isFocusCycleRoot()) {
            throw new IllegalArgumentException("Container is not a focus cycle root");
        }

        if (finder.currentComponent == null) {
            throw new IllegalArgumentException("Component must be specified");
        }

        if (finder.currentComponent != finder.focusCycleRoot
            && finder.currentComponent.getFocusCycleRootAncestor() != finder.focusCycleRoot) {

            throw new IllegalArgumentException("Component must be in a cycle root of the specified container");
        }

        List components = getAllAcceptableComponentsSorted(finder.focusCycleRoot, finder.currentComponent, true);
        if (components.size() == 0) {
            return null;
        }

        int componentIndex = components.indexOf(finder.currentComponent);
        return finder.findNextComponent(components, componentIndex);
    }

    private Component getFirstOrLastComponent(final NextComponentFinder finder) {
        checkCycleRootIsNotNull(finder.focusCycleRoot);

        List components = getAllAcceptableComponentsSorted(finder.focusCycleRoot, null, false);
        if (components.size() == 0) {
            return null;
        }

        return finder.findNextComponent(components, -1);
    }

    private static void checkCycleRootIsNotNull(final Container focusCycleRoot) {
        if (focusCycleRoot == null) {
            throw new IllegalArgumentException("Container must be specified");
        }
    }

    private abstract class NextComponentFinder {
        public Container focusCycleRoot;
        public Component currentComponent;
        public abstract Component findNextComponent(List availableComponents, int currentComponentIndex);
    }
}

