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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;


public class DefaultFocusTraversalPolicy
        extends ContainerOrderFocusTraversalPolicy {
    private static final long serialVersionUID = 8876966522510157497L;

    public DefaultFocusTraversalPolicy() {
    }

    @Override
    protected boolean accept(Component comp) {
        toolkit.lockAWT();
        try {
            // accept only if accepted by super.accept()
            // and focusability was explicitly set or "peer is focusable"
            boolean accepted = super.accept(comp);
            return (accepted && (comp.isFocusabilityExplicitlySet() ||
                    comp.isPeerFocusable()));
        } finally {
            toolkit.unlockAWT();
        }
    }

}

