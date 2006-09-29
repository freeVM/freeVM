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
 * @author Pavel Dolgov, Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import org.apache.harmony.awt.wtk.NativeRobot;


public class Robot {

    private int autoDelay;
    private boolean autoWaitForIdle;

    private final NativeRobot nativeRobot;

    public Robot() throws AWTException {
        this(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    public Robot(GraphicsDevice screen) throws AWTException {
        Toolkit.checkHeadless();
        if (screen.getType() != GraphicsDevice.TYPE_RASTER_SCREEN) {
            throw new IllegalArgumentException("not a screen device");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission("createRobot"));
        }
        // create(or get) native robot instance
        // for the specified screen
        Toolkit tk = Toolkit.getDefaultToolkit();
        nativeRobot = tk.getWTK().getNativeRobot(screen);
    }

    @Override
    public String toString() {
        /* The format is based on 1.5 release behavior 
         * which can be revealed by the following code:
         * System.out.println(new Robot());
         */

        return getClass().getName() + "[" + "autoDelay = " + autoDelay +
        ", autoWaitForIdle = " + autoWaitForIdle + "]";
    }

    public BufferedImage createScreenCapture(Rectangle screenRect) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission("readDisplayPixels"));
        }
        if (screenRect.isEmpty()) {
            throw new IllegalArgumentException("Rectangle width" +
                    " and height must be > 0");
        }

        return nativeRobot.createScreenCapture(screenRect);
    }

    public void delay(int ms) {
        checkDelay(ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public int getAutoDelay() {
        return autoDelay;
    }

    public Color getPixelColor(int x, int y) {
        return nativeRobot.getPixel(x, y);
    }

    public boolean isAutoWaitForIdle() {
        return autoWaitForIdle;
    }

    public void keyPress(int keycode) {
        nativeRobot.keyEvent(keycode, true);
        doWait();
    }

    public void keyRelease(int keycode) {
        nativeRobot.keyEvent(keycode, false);
        doWait();
    }

    public void mouseMove(int x, int y) {
        nativeRobot.mouseMove(x, y);
        doWait();
    }

    public void mousePress(int buttons) {
        checkButtons(buttons);
        nativeRobot.mouseButton(buttons, true);
        doWait();
    }

    public void mouseRelease(int buttons) {
        checkButtons(buttons);
        nativeRobot.mouseButton(buttons, false);
        doWait();
    }

    public void mouseWheel(int wheelAmt) {
        nativeRobot.mouseWheel(wheelAmt);
        doWait();
    }

    public void setAutoDelay(int ms) {
        checkDelay(ms);
        autoDelay = ms;
    }

    public void setAutoWaitForIdle(boolean isOn) {
        autoWaitForIdle = isOn;
    }

    public void waitForIdle() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("Cannot call method " +
                    "from the event dispatcher thread");
        }
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    // just do nothing
                }

            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void checkDelay(int ms) {
        if ((ms < 0) || (ms > 60000)) {
            throw new IllegalArgumentException("Delay must be to 0 to 60,000ms");
        }
    }

    private void checkButtons(int buttons) {
        int mask = (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK |
                    InputEvent.BUTTON3_MASK);
        if ((buttons & mask) != buttons) {
            throw new IllegalArgumentException("Invalid combination of button flags");
        }
    }

    private void doWait() {
        // first wait for idle if necessary:
        if (isAutoWaitForIdle()) {
            waitForIdle();
        }
        // now sleep if autoDelay is > 0
        int delay = getAutoDelay();
        if (delay > 0) {
            delay(delay);
        }
    }
}

