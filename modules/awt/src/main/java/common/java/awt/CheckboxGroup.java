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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.io.Serializable;

public class CheckboxGroup implements Serializable {
    private static final long serialVersionUID = 3729780091441768983L;

    private final Toolkit toolkit = Toolkit.getDefaultToolkit();

    private Checkbox current = null;

    public CheckboxGroup() {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }

    @Override
    public String toString() {
        /* The format is based on 1.5 release behavior 
         * which can be revealed by the following code:
         * System.out.println(new CheckboxGroup());
         */

        toolkit.lockAWT();
        try {
            return (getClass().getName() + "[" +
                    "selectedCheckbox=" + current + "]");
        } finally {
            toolkit.unlockAWT();
        }
    }

    public Checkbox getSelectedCheckbox() {
        toolkit.lockAWT();
        try {
            return current;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setSelectedCheckbox(Checkbox box) {
        toolkit.lockAWT();
        try {
            if ( (box != null) && (box.getCheckboxGroup() != this)) {
                return;
            }

            if (current != null) {
                current.setChecked(false);
            }

            if (box != null) {
                box.setChecked(true);
            }

            current = box;
        } finally {
            toolkit.unlockAWT();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Checkbox getCurrent() {
        toolkit.lockAWT();
        try {
            return getSelectedCheckbox();
        } finally {
            toolkit.unlockAWT();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setCurrent(Checkbox box) {
        toolkit.lockAWT();
        try {
            setSelectedCheckbox(box);
        } finally {
            toolkit.unlockAWT();
        }
    }
}

