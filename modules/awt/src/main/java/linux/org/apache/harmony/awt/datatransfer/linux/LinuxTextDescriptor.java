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
 * @author Michael Danilov
 * @version $Revision$
 */
package org.apache.harmony.awt.datatransfer.linux;

import org.apache.harmony.awt.datatransfer.NativeTextDescriptor;

public final class LinuxTextDescriptor implements NativeTextDescriptor {

    private static final String[] textNatives =
        new String[] {LinuxFlavorMap.UTF8_STRING, LinuxFlavorMap.STRING};

    public String getDefaultCharset() {
        return "iso-10646-ucs-2";
    }

    public boolean isTextNative(String nat) {
        return (nat.equals(LinuxFlavorMap.UTF8_STRING) || nat.equals(LinuxFlavorMap.STRING));
    }

    public String[] getTextNatives() {
        return textNatives;
    }

}
