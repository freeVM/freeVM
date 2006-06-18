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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.harmony.awt.wtk.NativeCursor;


public class Cursor implements Serializable {
    private static final long serialVersionUID = 8028237497568985504L;
    public static final int DEFAULT_CURSOR = 0;

    public static final int CROSSHAIR_CURSOR = 1;

    public static final int TEXT_CURSOR = 2;

    public static final int WAIT_CURSOR = 3;

    public static final int SW_RESIZE_CURSOR = 4;

    public static final int SE_RESIZE_CURSOR = 5;

    public static final int NW_RESIZE_CURSOR = 6;

    public static final int NE_RESIZE_CURSOR = 7;

    public static final int N_RESIZE_CURSOR = 8;

    public static final int S_RESIZE_CURSOR = 9;

    public static final int W_RESIZE_CURSOR = 10;

    public static final int E_RESIZE_CURSOR = 11;

    public static final int HAND_CURSOR = 12;

    public static final int MOVE_CURSOR = 13;

    /**
     * A mapping from names to system custom cursors
     */
    static Map/*<String, Cursor>*/ systemCustomCursors;
    static Properties cursorProps;

    static final String[] predefinedNames = {
            "Default", "Crosshair", "Text", "Wait",
            "Southwest Resize", "Southeast Resize",
            "Northwest Resize", "Northeast Resize",
            "North Resize", "South Resize", "West Resize", "East Resize",
            "Hand", "Move"

    };

    protected static Cursor[] predefined = {
            new Cursor(DEFAULT_CURSOR), null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null
    };

    public static final int CUSTOM_CURSOR = -1;

    protected String name;

    private final int type;
    private transient NativeCursor nativeCursor;
    private Point hotSpot;
    private Image image;

    protected Cursor(String name) {
        this(name, null, new Point());
    }

    public Cursor(int type) {
        checkType(type);
        this.type = type;
        if ((type >= 0) && (type < predefinedNames.length)) {
            name = predefinedNames[type] + " Cursor";
        }
    }

    Cursor(String name, Image img, Point hotSpot) {
        this.name = name;
        type = CUSTOM_CURSOR;
        this.hotSpot = hotSpot;
        image = img;
    }

    protected void finalize() throws Throwable {
        if (nativeCursor != null) {
            nativeCursor.destroyCursor();
        }
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getClass().getName() + "[" + name + "]";
    }

    public int getType() {
        return type;
    }

    public static Cursor getPredefinedCursor(int type) {
        checkType(type);
        Cursor cursor = predefined[type];
        if (cursor == null) {
            cursor = new Cursor(type);
            predefined[type] = cursor;
        }
        return cursor;
    }

    public static Cursor getDefaultCursor() {
        return getPredefinedCursor(DEFAULT_CURSOR);
    }

    public static Cursor getSystemCustomCursor(String name)
    throws AWTException, HeadlessException {
        Toolkit.checkHeadless();
        return getSystemCustomCursorFromMap(name);
    }

    private static Cursor getSystemCustomCursorFromMap (String name)
    throws AWTException {
        loadCursorProps();
        if (systemCustomCursors == null) {
            systemCustomCursors = new HashMap();
        }
        Cursor cursor = (Cursor) systemCustomCursors.get(name);
        if (cursor != null) {
            return cursor;
        }
        String exMsg = "failed to parse hotspot property for cursor: " + name;
        String nm = "Cursor." + name;
        String nameStr = cursorProps.getProperty(nm + ".Name");
        String hotSpotStr = cursorProps.getProperty(nm + ".HotSpot");
        String fileStr = cursorProps.getProperty(nm + ".File");
        int idx = hotSpotStr.indexOf(',');
        if (idx < 0) {
            throw new AWTException(exMsg);
        }
        int x, y;
        try {
            x = new Integer(hotSpotStr.substring(0, idx)).intValue();
            y = new Integer(hotSpotStr.substring(idx + 1,
                                                 hotSpotStr.length())).intValue();
        } catch (NumberFormatException nfe) {
            throw new AWTException(exMsg);
        }
        Image img = Toolkit.getDefaultToolkit().createImage(fileStr);
        cursor = new Cursor(nameStr, img, new Point(x, y));
        systemCustomCursors.put(name, cursor);

        return cursor;
    }

    private static void loadCursorProps() throws AWTException {
        if (cursorProps != null) {
            return;
        }
        String sep = File.separator;
        String cursorsDir = "lib" + sep + "images" + sep + "cursors";
        String cursorsAbsDir = System.getProperty("java.home") + sep +
                                cursorsDir;
        String cursorPropsFileName = "cursors.properties";
        String cursorPropsFullFileName = (cursorsAbsDir + sep +
                                          cursorPropsFileName);
        cursorProps = new Properties();
        try {
            cursorProps.load(new FileInputStream(new File(
                    cursorPropsFullFileName)));
        } catch (FileNotFoundException e) {
            throw new AWTException("Exception: class " + e.getClass() + " " +
                      e.getMessage() + "occurred while loading: " +
                      cursorPropsFullFileName);
        } catch (IOException e) {
            throw new AWTException(e.getMessage());
        }

    }

    static void checkType(int type) {
        // can't use predefined array here because it may not have been
        // initialized yet
        if ((type < 0) || (type >= predefinedNames.length)) {
            throw new IllegalArgumentException("illegal cursor type");
        }
    }

    // "lazily" create native cursors:
    NativeCursor getNativeCursor() {
        if (nativeCursor != null) {
            return nativeCursor;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (type != CUSTOM_CURSOR) {
            nativeCursor = toolkit.createNativeCursor(type);
        } else {
            nativeCursor = toolkit.createCustomNativeCursor(image, hotSpot,
                                                            name);
        }
        return nativeCursor;
    }

    void setNativeCursor(NativeCursor nativeCursor) {
        this.nativeCursor = nativeCursor;
    }
}

