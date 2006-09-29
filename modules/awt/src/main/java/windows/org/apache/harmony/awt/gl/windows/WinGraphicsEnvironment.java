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
 * @author Alexey A. Petrenko
 * @version $Revision$
 */
package org.apache.harmony.awt.gl.windows;

import java.awt.GraphicsDevice;
import java.awt.HeadlessException;

import org.apache.harmony.awt.gl.CommonGraphicsEnvironment;
import org.apache.harmony.awt.wtk.WindowFactory;


/**
 * Windows GraphicsEnvironment implementation
 *
 */
public class WinGraphicsEnvironment extends CommonGraphicsEnvironment {
    WinGraphicsDevice defaultDevice = null;
    WinGraphicsDevice []devices = null;

    static {
        System.loadLibrary("gl");
    }

    public WinGraphicsEnvironment(WindowFactory wf) {
    }

    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
        if (defaultDevice == null) {
            WinGraphicsDevice []dvcs = (WinGraphicsDevice [])getScreenDevices();
            for (int i = 0; i < dvcs.length; i++)
                if (dvcs[i].isDefaultDevice()) {
                    defaultDevice = dvcs[i];
                    break;
                }
        }

        return defaultDevice;
    }

    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
        if (devices == null) {
            devices = enumerateDisplayDevices();
        }

        return devices;
    }

    /**
     * Enumerates system displays
     * 
     * @return Array of WinGraphicsDevice objects representing system displays
     */
    private native WinGraphicsDevice []enumerateDisplayDevices();
}
