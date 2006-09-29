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
 * @author Sergey Burlak
 * @version $Revision$
 */

package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.border.Border;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.plaf.ComponentUI;

import org.apache.harmony.x.swing.Utilities;


public class UIManager implements Serializable {

    public static class LookAndFeelInfo {
        private String name;
        private String className;

        public LookAndFeelInfo(final String name, final String className) {
            this.name = name;
            this.className = className;
        }

        public String toString() {
            return this.getClass().getName() + "[" + name + " " + className + "]";
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

        public boolean equals(final Object object) {
            if (object == null) {
                return false;
            }
            if (!(object instanceof LookAndFeelInfo)) {
                return false;
            }
            LookAndFeelInfo info = (LookAndFeelInfo)object;

            return getName() != null && getName().equals(info.getName())
                   && getClassName() != null && getClassName().equals(info.getClassName());
        }
    }

    private static final String PROPERTIES_FILE_PATH = System.getProperty("java.home")
                                               + File.separator + "lib"
                                               + File.separator + "swing.properties";

    private static final String METAL_LOOK_AND_FEEL_CLASS = "javax.swing.plaf.metal.MetalLookAndFeel";

    private static final String DEFAULT_LAF = "swing.defaultlaf";
    private static final String SYSTEM_LAF = "swing.systemlaf";
    private static final String CROSSPLATFORM_LAF = "swing.crossplatformlaf";
    private static final String INSTALLED_LAFS = "swing.installedlafs";
    private static final String INSTALLED_LAF = "swing.installedlaf";

    private static final String LOOK_AND_FEEL_PROPERTY = "lookAndFeel";

    private static final LookAndFeelInfo[] ALL_LFS = new LookAndFeelInfo[] {
            new LookAndFeelInfo("Metal", METAL_LOOK_AND_FEEL_CLASS)};

    private static LookAndFeel lookAndFeel;
    private static UIDefaults uiDefaults;
    private static Properties props;
    private static List installedLFs;
    private static SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(UIManager.class);
    private static UIDefaults userUIDefaults;

    static {
        initialize();
    }


    public static Border getBorder(final Object obj, final Locale locale) {
        return getDefaults().getBorder(obj, locale);
    }

    public static Border getBorder(final Object obj) {
        return getDefaults().getBorder(obj);
    }

    public static Icon getIcon(final Object obj, final Locale locale) {
        return getDefaults().getIcon(obj, locale);
    }

    public static Icon getIcon(final Object obj) {
        return getDefaults().getIcon(obj);
    }

    public static String getString(final Object obj) {
        return getDefaults().getString(obj);
    }

    public static String getString(final Object obj, final Locale locale) {
        return getDefaults().getString(obj, locale);
    }

    public static Font getFont(final Object obj, final Locale locale) {
        return getDefaults().getFont(obj, locale);
    }

    public static Font getFont(final Object obj) {
        return getDefaults().getFont(obj);
    }

    public static Color getColor(final Object obj, final Locale locale) {
        return getDefaults().getColor(obj, locale);
    }

    public static Color getColor(final Object obj) {
        return getDefaults().getColor(obj);
    }

    public static int getInt(final Object obj, final Locale locale) {
        return getDefaults().getInt(obj, locale);
    }

    public static int getInt(final Object obj) {
        return getDefaults().getInt(obj);
    }

    public static Insets getInsets(final Object obj) {
        return getDefaults().getInsets(obj);
    }

    public static Insets getInsets(final Object obj, final Locale locale) {
        return getDefaults().getInsets(obj, locale);
    }

    public static Dimension getDimension(final Object obj) {
        return getDefaults().getDimension(obj);
    }

    public static Dimension getDimension(final Object obj, final Locale locale) {
        return getDefaults().getDimension(obj, locale);
    }

    public static boolean getBoolean(final Object obj) {
        return getDefaults().getBoolean(obj);
    }

    public static boolean getBoolean(final Object obj, final Locale locale) {
        return getDefaults().getBoolean(obj, locale);
    }

    public static Object get(final Object obj, final Locale locale) {
        return getDefaults().get(obj, locale);
    }

    public static Object put(final Object key, final Object obj) {
        return getUserUIDefaults().put(key, obj);
    }

    public static ComponentUI getUI(final JComponent comp) {
        return getDefaults().getUI(comp);
    }

    public static Object get(final Object obj) {
        return getDefaults().get(obj);
    }

    public static void installLookAndFeel(final String name, final String className) {
        installLookAndFeel(new LookAndFeelInfo(name, className));
    }

    public static void setInstalledLookAndFeels(final LookAndFeelInfo[] lfs) {
        installedLFs = new LinkedList(Arrays.asList(lfs));
    }

    public static void installLookAndFeel(final LookAndFeelInfo lfInfo) {
        if (installedLFs == null) {
            installedLFs = new LinkedList();
        }
        installedLFs.add(lfInfo);
    }

    public static LookAndFeelInfo[] getInstalledLookAndFeels() {
        return (LookAndFeelInfo[])installedLFs.toArray(new LookAndFeelInfo[installedLFs.size()]);
    }

    public static UIDefaults getLookAndFeelDefaults() {
        return lookAndFeel.getDefaults();
    }

    public static UIDefaults getDefaults() {
        return uiDefaults;
    }

    public static void setLookAndFeel(final String str) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        setLookAndFeel((LookAndFeel)Class.forName(str, false, ClassLoader.getSystemClassLoader()).newInstance());
    }

    public static void setLookAndFeel(LookAndFeel laf) throws UnsupportedLookAndFeelException {
        if (laf == lookAndFeel) {
            return;
        }
        if (!laf.isSupportedLookAndFeel()) {
            throw new UnsupportedLookAndFeelException(laf + " is not supported on this platform.");
        }
        LookAndFeel oldValue = lookAndFeel;
        if (lookAndFeel != null) {
            lookAndFeel.uninitialize();
        }
        lookAndFeel = laf;
        lookAndFeel.initialize();

        final UIDefaults uiDefs = lookAndFeel.getDefaults();
        uiDefaults = new UIDefaults() {
            public Object get(final Object key, final Locale locale) {
                Object result = getUserUIDefaults().get(key, locale);
                if (result != null) {
                    return result;
                }
                result = uiDefs.get(key, locale);
                if (result != null) {
                    return result;
                }

                return super.get(key, locale);
            }

            public Object put(final Object key, final Object value) {
                return UIManager.put(key, value);
            }

            public synchronized void clear() {
                getUserUIDefaults().clear();
            }
        };

        propertyChangeSupport.firePropertyChange(LOOK_AND_FEEL_PROPERTY, oldValue, laf);
    }

    public static void addAuxiliaryLookAndFeel(final LookAndFeel lf) {
    }

    public static boolean removeAuxiliaryLookAndFeel(final LookAndFeel lf) {
        return false;
    }

    public static LookAndFeel[] getAuxiliaryLookAndFeels() {
        return new LookAndFeel[] {};
    }

    public static LookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    public static String getSystemLookAndFeelClassName() {
        String result = (String)getProperty(SYSTEM_LAF, null);
        if (result != null) {
            return result;
        }

        String lfClass = getSystemLFClassFromCustomList();
        if (lfClass != null) {
            return lfClass;
        }

        return getCrossPlatformLookAndFeelClassName();
    }

    public static String getCrossPlatformLookAndFeelClassName() {
        return (String)getProperty(CROSSPLATFORM_LAF, METAL_LOOK_AND_FEEL_CLASS);
    }

    public static void removePropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public static void addPropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public static PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    /**
     * Load properties from the swing.properties file
     * @param String filePath
     * @return Properties props
     */
    private static Properties loadSwingProperties() {
        Properties result = new Properties();
        try {
            result.load(new FileInputStream(PROPERTIES_FILE_PATH));
        } catch (final IOException ignored) {
        }

        return result;
    }

    private static void initialize() {
        props = loadSwingProperties();
        setDefaultLookAndFeel();
        installedLFs = new ArrayList(getDefaultInstalledLFs());
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .setDefaultFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
    }

    private static void setDefaultLookAndFeel() {
        try {
            setLookAndFeel((String)getProperty(DEFAULT_LAF, METAL_LOOK_AND_FEEL_CLASS));
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static Object getProperty(final String name, final String defaultValue) {
        String property = System.getProperty(name);
        if (property != null) {
            return property;
        } else if ((props != null) && (props.containsKey(name))) {
            return props.getProperty(name);
        }

        return defaultValue;
    }

    private static List getDefaultInstalledLFs() {
        if (getProperty(INSTALLED_LAFS, null) != null) {
            return getInstalledLFsFromPropertyFile();
        }
        List result = new LinkedList();
        for (int i = 0; i < ALL_LFS.length; i++) {
            try {
                if (((LookAndFeel)Class.forName(ALL_LFS[i].getClassName()).newInstance()).isSupportedLookAndFeel()) {
                    result.add(ALL_LFS[i]);
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * Return the array of the installed properties from the swing.properties file
     * Format:
     * swing.installedlafs = <LF name0>,<LF name1>
     * swing.installedlaf.<LF name0>.name = <LF name>
     * swing.installedlaf.<LF name0>.class = <LF class>
     * ...
     * @return LookAndFeelInfo[]
     */
    private static List getInstalledLFsFromPropertyFile() {
        String lfNames = props.getProperty(INSTALLED_LAFS);
        if (lfNames == null) {
            return new LinkedList();
        }
        String[] names = lfNames.split(",");
        List result = new LinkedList();
        for (int i = 0; i < names.length; i++) {
            String token = names[i];
            String lfNameProperty = new StringBuffer(INSTALLED_LAF).append(".")
                .append(token)
                .append(".name").toString();
            String lfClassProperty = new StringBuffer(INSTALLED_LAF).append(".")
                .append(token)
                .append(".class").toString();

            String lfName = props.getProperty(lfNameProperty);
            String lfClass = props.getProperty(lfClassProperty);

            if (!Utilities.isEmptyString(lfName) && !Utilities.isEmptyString(lfClass)) {
                result.add(new LookAndFeelInfo(lfName, lfClass));
            }
        }

        return result;
    }

    private static String getSystemLFClassFromCustomList() {
        LookAndFeelInfo[] installedLookAndFeels = getInstalledLookAndFeels();
        for (int i = 0; i < installedLookAndFeels.length; i++) {
            try {
                String className = installedLookAndFeels[i].className;
                LookAndFeel lf = (LookAndFeel)Class.forName(className).newInstance();
                if (lf.isNativeLookAndFeel()) {
                    return className;
                }
            } catch (final Exception ignored) {
            }
        }

        return null;
    }

    /**
     * Default values specified by user
     * @return UIDefaults userUIDefaults
     */
    private static UIDefaults getUserUIDefaults() {
        if (userUIDefaults == null) {
            userUIDefaults = new UIDefaults();
        }

        return userUIDefaults;
    }
}

