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
package org.apache.harmony.awt.wtk.windows;

import org.apache.harmony.awt.wtk.NativeCursor;

public class WinCursor implements NativeCursor {
    final long hCursor;

  /*is this a system cursor?(such cursors are shared and can't be destroyed
          by user*/
    final boolean system;

    WinCursor(final long handle, final boolean system) {
        hCursor = handle;
        this.system = system;
    }

    WinCursor(final long handle) {
        this(handle, true); //create system(predefined) cursors by default
    }

    /**
     * @see org.apache.harmony.awt.wtk.NativeCursor#setCursor()
     */
    public void setCursor(long winID) {
        WinEventQueue.win32.SetCursor(hCursor);
    }

    /**
     * @see org.apache.harmony.awt.wtk.NativeCursor#destroyCursor()
     */
    public void destroyCursor() {
        if (!system) {
            WinEventQueue.win32.DestroyCursor(hCursor);
        }
    }

    /**
     * @see org.apache.harmony.awt.wtk.NativeCursor#getId()
     */
    public long getId() {
        return hCursor;
    }

}
