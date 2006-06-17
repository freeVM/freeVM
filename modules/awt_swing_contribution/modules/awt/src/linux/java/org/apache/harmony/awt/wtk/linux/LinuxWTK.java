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
package org.apache.harmony.awt.wtk.linux;

import java.awt.GraphicsDevice;
import java.util.HashMap;

import org.apache.harmony.awt.gl.linux.XGraphicsDevice;
import org.apache.harmony.awt.wtk.*;


public final class LinuxWTK extends WTK {

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getGraphicsFactory()
     */
    public GraphicsFactory getGraphicsFactory() {
        return graphicsFactory;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getNativeEventQueue()
     */
    public NativeEventQueue getNativeEventQueue() {
        return windowFactory;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getWindowFactory()
     */
    public WindowFactory getWindowFactory() {
        return windowFactory;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getCursorFactory()
     */
    public CursorFactory getCursorFactory() {
        return cursorFactory;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getNativeMouseInfo()
     */
    public NativeMouseInfo getNativeMouseInfo() {
        return mouseInfo;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getSystemProperties()
     */
    public SystemProperties getSystemProperties() {
        return systemProperties;
    }

    /**
     * @see org.apache.harmony.awt.wtk.WTK#getNativeRobot(java.awt.GraphicsDevice)
     */
    public NativeRobot getNativeRobot(GraphicsDevice screen) {
        XTestRobot robot = (XTestRobot) robots.get(screen);
        if (robot == null) {
            robot = new XTestRobot(windowFactory.getDisplay(),
                                   (XGraphicsDevice) screen);
            robots.put(screen, robot);
        }
        return robot;
    }

    private final LinuxWindowFactory windowFactory = new LinuxWindowFactory();
    private final GraphicsFactory graphicsFactory = new org.apache.harmony.awt.gl.linux.LinuxGraphics2DFactory();
    private final LinuxCursorFactory cursorFactory = new LinuxCursorFactory(windowFactory);
    private final NativeMouseInfo mouseInfo = new LinuxMouseInfo(windowFactory);
    private final LinuxSystemProperties systemProperties = new LinuxSystemProperties(windowFactory);
    private HashMap robots = new HashMap(); //HashMap<GraphicsDevice, XTestRobot>
}
