/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
package org.apache.harmony.awt.wtk.linux;

import org.apache.harmony.awt.nativebridge.linux.X11;

class XServerConnection {

    private long display;

    private int screen;

    private final X11 x11;

    public XServerConnection(X11 x11) {
        this.x11 = x11;
        display = x11.XOpenDisplay(0); //0 - we use default display only
        if (display == 0) {
            String name = System.getProperty("DISPLAY");
            throw new InternalError("Cannot open display '" + (name != null ? name : "") + "'");
        }
        screen = x11.XDefaultScreen(display);
    }

    public void close() {
        x11.XCloseDisplay(display);
    }

    public long getDisplay() {
        return display;
    }

    public int getScreen() {
        return screen;
    }
}
