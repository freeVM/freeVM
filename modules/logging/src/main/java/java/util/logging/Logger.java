/* Copyright 2004, 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package java.util.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * Loggers are used to log records to certain outputs, including file, console,
 * etc. They use various handlers to actually do the output-dependent
 * operations.
 * <p>
 * Client applications can get named loggers by calling the methods
 * <code>getLogger</code>. They can also get anonymous loggers by calling the
 * methods <code>getAnonymousLogger</code>. Named loggers are organized in a
 * namespace hierarchy managed by a log manager. The naming convention is
 * usually the same as java package's naming convention, i.e., using
 * dot-separated strings. Anonymous loggers do not belong to any namespace.
 * </p>
 * <p>
 * Loggers "inherit" log level setting from their parent if its own level is set
 * to <code>null</code>. This is also true for the resource bundle. The
 * logger's resource bundle is used to localize the log messages if no resource
 * bundle name is given when a log method is called. If
 * <code>getUseParentHandlers</code> is <code>true</code>, loggers also
 * inherit their parent's handlers. Here "inherit" only means the "behaviors"
 * are inherited. The internal fields value will not change, for example,
 * <code>getLevel()</code> still returns <code>null</code>.
 * </p>
 * <p>
 * When loading a given resource bundle, the logger first tries to use the
 * context classloader. If that fails, it tries the system classloader. And if
 * that still fails, it searches up the class stack and uses each class's
 * classloader to try to locate the resource bundle.
 * </p>
 * <p>
 * Some log methods accept log requests that do not specify the source class and
 * source method. In these cases, the logging framework will automatically infer
 * the calling class and method, but not guaranteed to be accurate.
 * </p>
 * <p>
 * Once a <code>LogRecord</code> object has been passed into the logging
 * framework, it is owned by the logging framework and the client applications
 * should not use it any longer.
 * </p>
 * <p>
 * All methods of this class are thread-safe.
 * </p>
 * 
 * @see LogManager
 */
public class Logger {

    /*
     * -------------------------------------------------------------------
     * Constants
     * -------------------------------------------------------------------
     */

    // message of "entering" series methods
    private final static String MSG_ENTERING = "ENTRY"; //$NON-NLS-1$

    // message of "exiting" series methods
    private final static String MSG_EXITING = "RETURN"; //$NON-NLS-1$

    // message of "throwing" series methods
    private final static String MSG_THROWING = "THROW"; //$NON-NLS-1$

    private final static int OFF_VALUE = Level.OFF.intValue();

    /*
     * --------------------------------------------------------------------
     * Class variables
     * --------------------------------------------------------------------
     */

    /**
     * The global logger is provided as convenience for casual use.
     */
    public final static Logger global = Logger.getLogger("global"); //$NON-NLS-1$

    /*
     * -------------------------------------------------------------------
     * Instance variables
     * -------------------------------------------------------------------
     */

    // the name of this logger
    private volatile String name;

    // the parent logger of this logger
    private Logger parent;

    // the logging level of this logger
    private volatile Level levelObjVal;

    // the logging level as int of this logger
    private volatile int levelIntVal;

    // the filter
    private Filter filter;

    // the name of the resource bundle used to localize logging messages
    private String resBundleName;

    // the loaded resource bundle according to the specified name
    private ResourceBundle resBundle;

    // the handlers attached to this logger
    List<Handler> handlers = null;

    /*
     * flag indicating whether to notify parent's handlers on receiving a log
     * request
     */
    private boolean notifyParentHandlers;

    // flag indicating whether this logger is named or anonymous
    private boolean isNamed;

    private List<Logger> childs;
    
    LogManager manager = null;

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs a <code>Logger</code> object with the supplied name and
     * resource bundle name.
     * 
     * @param name
     *            the name of this logger, may be null for anonymous loggers
     * @param resourceBundleName
     *            the name of the resource bundle used to localize logging
     *            messages, may be null
     * @throws MissingResourceException
     *             If the specified resource bundle can not be loaded.
     */
    protected Logger(String name, String resourceBundleName) {
        // try to load the specified resource bundle first
        if (null == resourceBundleName) {
            this.resBundleName = null;
            this.resBundle = null;
        } else {
            this.resBundle = loadResourceBundle(resourceBundleName);
            this.resBundleName = resourceBundleName;
        }
        this.name = name;
        this.parent = null;
        this.filter = null;
        this.childs = new ArrayList<Logger>();
        this.notifyParentHandlers = true;
        // any logger is not anonymous by default
        this.isNamed = true;

        //-- 'null' means that level will be inherited from parent (see getLevel)
        //-- Level.INFO is default level if we don't set it. It will be
        //-- changed to parent level or to configLevel after adding to the
        //-- family tree. As of this, actually, setting to Level.INFO is
        //-- not needed here.
        this.levelObjVal = null;
        this.levelIntVal = Level.INFO.intValue();
    }

    //-- should be called under the lm lock
    private void setLevelImpl(Level newLevel) {
        // update levels for the whole hierarchy
        int oldVal = levelIntVal;
        levelObjVal = newLevel;
        if (null == newLevel) {
            levelIntVal = null != parent
                    ? parent.levelIntVal
                    : Level.INFO.intValue();
        } else {
            levelIntVal = newLevel.intValue();
        }
        if (oldVal != levelIntVal) {
            forceChildsToInherit();
        }
    }

    //-- should be called under the lm lock
    private void forceChildsToInherit() {
        for (Logger child : childs) {
            if (null == child.levelObjVal) { // should inherit
                child.setLevelImpl(null);
            }
        }
    }

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Load the specified resource bundle, use privileged code.
     * 
     * @param resourceBundleName
     *            the name of the resource bundle to load, cannot be null
     * @return the loaded resource bundle.
     * @throws MissingResourceException
     *             If the specified resource bundle can not be loaded.
     */
    static ResourceBundle loadResourceBundle(String resourceBundleName) {
        // try context class loader to load the resource
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using context classloader, ignore
            }
        }
        // try system class loader to load the resource
        cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return ClassLoader.getSystemClassLoader();
                    }
                });
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using system classloader, ignore
            }
        }
        // try all class loaders up the class stack
        final Class[] classes = (new PrivateSecurityManager())
                .privateGetClassContext();
        // the first class, which is PrivateSecurityManager, is skipped
        for (int i = 1; i < classes.length; i++) {
            final int index = i;
            try {
                cl = AccessController.doPrivileged(
                        new PrivilegedAction<ClassLoader>() {
                            public ClassLoader run() {
                                return classes[index].getClassLoader();
                            }
                        });
                if (null == cl) {
                    continue;
                }
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using the current class's classloader, ignore
            }
        }
        throw new MissingResourceException(
                "Failed to load the specified resource bundle \"" //$NON-NLS-1$
                        + resourceBundleName + "\".", resourceBundleName, null); //$NON-NLS-1$
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings
     * so that they can be used in applets.
     * <p>
     * Anonymous loggers' parent is set to be the root logger. This enables
     * them to inherit default logging level and handlers from the root logger.
     * </p>
     * 
     * @return a new instance of anonymous logger
     */
    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings
     * so that they can be used in applets.
     * <p>
     * Anonymous loggers' parent is set to be the root logger. This enables
     * them to inherit default logging level and handlers from the root logger.
     * </p>
     * 
     * @param resourceBundleName
     *            the name of the resource bundle used to localize log messages
     * @return a new instance of anonymous logger
     * @throws MissingResourceException
     *             If the specified resource bundle can not be loaded.
     */
    public static Logger getAnonymousLogger(String resourceBundleName) {
        final Logger l = new Logger(null, resourceBundleName);
        l.isNamed = false;
        l.internalSetParent(LogManager.getLogManager().getLogger("")); //$NON-NLS-1$
        return l;
    }

    /*
     * Check whether the same resource bundle has been specified.
     * Synchronize to ensure the consistency between resource bundle
     * and its name.
     */
    private static void updateResourceBundle(Logger l, String resourceBundleName) {
        synchronized (l) {
            if (null == l.getResourceBundleName()) {
                /*
                 * load the resource bundle if none is specified
                 * before
                 */
                l.resBundle = loadResourceBundle(resourceBundleName);
                l.resBundleName = resourceBundleName;
            } else if (!l.getResourceBundleName().equals(resourceBundleName)) {
                /*
                 * throw exception if the specified resource bundles
                 * are inconsistent with each other, i.e., different
                 * names
                 */
                throw new IllegalArgumentException(
                        "The specified resource bundle name \"" //$NON-NLS-1$
                                + resourceBundleName
                                + "\" is inconsistent with the existing one \"" //$NON-NLS-1$
                                + l.getResourceBundleName() + "\"."); //$NON-NLS-1$
            }
        }
    }

    /*
     * Gets a named logger associated with the supplied resource bundle. This
     * method accepts null resource bundle name. The method body is synchronized
     * on the instance of the LogManager to insure the consistency of the whole
     * operation.
     */
    private static Logger getLoggerWithRes(String name,
            String resourceBundleName, boolean hasResourceName) {
        LogManager man = LogManager.getLogManager();
        synchronized (man) {
            // Try to find an existing logger with the specified name
            Logger l = man.getLogger(name);
            // If no existing logger with the same name, create a new one
            if (null == l) {
                l = new Logger(name, resourceBundleName);
                man.addLogger(l);
            } else if (hasResourceName) {
                updateResourceBundle(l, resourceBundleName);
            }
            return l;
        }
    }

    /**
     * Gets a named logger. The returned logger may already exist, or may be
     * newly created. If the latter, its level will be set to the configured
     * level according to the <code>LogManager</code>'s properties if any.
     * 
     * @param name
     *            the name of the logger to get, cannot be null
     * @return a named logger
     * @throws MissingResourceException
     *             If the specified resource bundle can not be loaded.
     */
    public static Logger getLogger(String name) {
        return getLoggerWithRes(name, null, false);
    }

    /**
     * Gets a named logger associated with the supplied resource bundle. The
     * resource bundle will be used to localize logging messages.
     * 
     * @param name
     *            the name of the logger to get, cannot be null
     * @param resourceBundleName
     *            the name of the resource bundle, may be null
     * @return a named logger
     */
    public static Logger getLogger(String name, String resourceBundleName) {
        return getLoggerWithRes(name, resourceBundleName, true);
    }

    /**
     * Adds a handler to this logger. The handler will be fed with log records
     * received by this logger.
     * 
     * @param handler
     *            the handler object to add, cannot be null
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public synchronized void addHandler(Handler handler) {
        if (null == handler) {
            throw new NullPointerException("The 'handler' parameter is null."); //$NON-NLS-1$
        }
        // Anonymous loggers can always add handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        initHandler();
        this.handlers.add(handler);
    }
    
    private void initHandler() {
      if(handlers == null){
          if(manager == null){
              handlers = new ArrayList<Handler>();
              return;
          }
          handlers = new ArrayList<Handler>();
          String handlerStr = manager.getProperty("".equals(name)?"handlers":name+".handlers"); //$NON-NLS-1$
          if (null == handlerStr) {
              return;
          }
          StringTokenizer st = new StringTokenizer(handlerStr, " "); //$NON-NLS-1$
          while (st.hasMoreTokens()) {
              String handlerName = st.nextToken();
              Handler handler = (Handler)LogManager.getInstanceByClass(handlerName);
              handlers.add(handler);
              String level = manager.getProperty(handlerName + ".level"); //$NON-NLS-1$
              if (null != level) {
                  handler.setLevel(Level.parse(level));
              }
          }
      }
    }

    /**
     * Gets all the handlers associated with this logger.
     * 
     * @return an array of all the handlers associated with this logger
     */
    public synchronized Handler[] getHandlers() {
        initHandler();
        return handlers.toArray(new Handler[handlers.size()]);
    }

    /**
     * Removes a handler for this logger. If the specified handler does not
     * exist, this method has no effect.
     * 
     * @param handler
     *            the handler to be removed, cannot be null
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public synchronized void removeHandler(Handler handler) {
        // Anonymous loggers can always remove handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        if (null == handler) {
            return;
        }
        initHandler();
        this.handlers.remove(handler);
    }

    /**
     * Gets the filter used by this logger.
     * 
     * @return the filter used by this logger
     */
    public synchronized Filter getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter used by this logger.
     * 
     * @param newFilter
     *            the filter to set
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public synchronized void setFilter(Filter newFilter) {
        // Anonymous loggers can always set the filter
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        this.filter = newFilter;
    }

    /**
     * Gets the logging level of this logger.
     * 
     * @return the logging level of this logger
     */
    public Level getLevel() {
        return levelObjVal;
    }

    /**
     * Sets the logging level for this logger. A <code>null</code> level
     * indicates this logger will inherit its parent's level.
     * 
     * @param newLevel
     *            the logging level to set
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public void setLevel(Level newLevel) {
        // Anonymous loggers can always set the level
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        LogManager lm = LogManager.getLogManager();

        synchronized (lm) {
            setLevelImpl(newLevel);
        }
    }

    /**
     * Gets the flag which indicates whether to use parent's handlers to publish
     * incoming log records, potentially recursively up the namespace.
     * 
     * @return <code>true</code> if set to use parent's handlers, otherwise
     *         <code>false</code>
     */
    public synchronized boolean getUseParentHandlers() {
        return this.notifyParentHandlers;
    }

    /**
     * Sets the flag which indicates whether to use parent's handlers to publish
     * incoming log records, potentially recursively up the namespace.
     * 
     * @param notifyParentHandlers
     *            the flag whether to use parent's handlers
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public synchronized void setUseParentHandlers(boolean notifyParentHandlers) {
        // Anonymous loggers can always set the useParentHandlers flag
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        this.notifyParentHandlers = notifyParentHandlers;
    }

    /**
     * Gets the parent of this logger in the namespace.
     * 
     * @return the parent of this logger in the namespace
     */
    public synchronized Logger getParent() {
        return this.parent;
    }

    /**
     * Sets the parent of this logger in the namespace. This method should
     * usually be used by the <code>LogManager</code> object only. This
     * method does not check security.
     * 
     * @param newParent
     *            the parent logger to set
     */
    synchronized void internalSetParent(Logger newParent) {
        this.parent = newParent;
        //-- update level after setting a parent.
        //-- if level == null we should inherit the parent's level
        if (null == levelObjVal) {
            setLevelImpl(levelObjVal);
        }
        newParent.addChild(this);
    }

    /**
     * Sets the parent of this logger in the namespace. This method should
     * usually be used by the <code>LogManager</code> object only.
     * 
     * @param parent
     *            the parent logger to set
     * @throws SecurityException
     *             If a security manager determines that the caller does not
     *             have the required permission.
     */
    public synchronized void setParent(Logger parent) {
        if (null == parent) {
            throw new NullPointerException("The 'parent' parameter is null."); //$NON-NLS-1$
        }
        // even anonymous loggers are checked
        LogManager.getLogManager().checkAccess();
        internalSetParent(parent);
    }

    final void addChild(Logger logger) {
        childs.add(logger);
    }

    final void removeChild(Logger child) {
        childs.remove(child);
    }


    /**
     * Gets the name of this logger.
     * 
     * @return the name of this logger
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the loaded resource bundle used by this logger to localize logging
     * messages. If it's null, the parent's resource bundle will be inherited.
     * 
     * @return the loaded resource bundle used by this logger
     */
    public synchronized ResourceBundle getResourceBundle() {
        return this.resBundle;
    }

    /**
     * Gets the name of the loaded resource bundle used by this logger to
     * localize logging messages. If it's null, the parent's resource bundle
     * name will be inherited.
     * 
     * @return the name of the loaded resource bundle used by this logger
     */
    public synchronized String getResourceBundleName() {
        return this.resBundleName;
    }

    /**
     * This method is for compatibility. Tests written to the reference 
     * implementation API imply that the isLoggable() method is not called 
     * directly. This behavior is important because subclass may override 
     * isLoggable() method, so that affect the result of log methods.
     */
    private boolean internalIsLoggable(Level l) {
        int effectiveLevel = levelIntVal;
        if (effectiveLevel == OFF_VALUE) {
            // always return false if the effective level is off
            return false;
        } else {
            return l.intValue() >= effectiveLevel;
        }
    }

    /**
     * Determines whether this logger will actually log messages of the
     * specified level. The effective level used to do the determination may be
     * inherited from its parent. The default level is <code>Level.INFO</code>.
     * 
     * @param l
     *            the level to check
     * @return <code>true</code> if this logger will actually log this level,
     *         otherwise <code>false</code>
     */
    public boolean isLoggable(Level l) {
        return internalIsLoggable(l);
    }

    /*
     * Sets the resource bundle and its name for a supplied LogRecord object.
     * This method first tries to use this logger's resource bundle if any,
     * otherwise try to inherit from this logger's parent, recursively up the
     * namespace. Synchronize to ensure the consistency between resource bundle
     * and its name.
     */
    private synchronized void setResourceBundle(LogRecord record) {
        if (null != this.resBundleName) {
            record.setResourceBundle(this.resBundle);
            record.setResourceBundleName(this.resBundleName);
        } else {
            Logger anyParent = this.parent;
            while (null != anyParent) {
                /*
                 * Synchronize to ensure the consistency between resource bundle
                 * and its name.
                 */
                synchronized (anyParent) {
                    if (null != anyParent.resBundleName) {
                        record.setResourceBundle(anyParent.resBundle);
                        record.setResourceBundleName(anyParent.resBundleName);
                        return;
                    }
                }
                anyParent = anyParent.parent;
            }
        }
    }

    /**
     * Logs a message indicating entering a method. A log record with log level
     * <code>Level.FINER</code>, log message "ENTRY", and the specified
     * source class name and source method name is submitted for logging.
     * 
     * @param sourceClass
     *            the calling class name
     * @param sourceMethod
     *            the method name
     */
    public void entering(String sourceClass, String sourceMethod) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, MSG_ENTERING);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating entering a method. A log record with log level
     * <code>Level.FINER</code>, log message "ENTRY", and the specified
     * source class name and source method name and one parameter is submitted
     * for logging.
     * 
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param param
     *            the parameter for the method call
     */
    public void entering(String sourceClass, String sourceMethod, Object param) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, MSG_ENTERING + " {0}"); //$NON-NLS-1$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating entering a method. A log record with log level
     * <code>Level.FINER</code>, log message "ENTRY", and the specified
     * source class name and source method name and parameters is submitted for
     * logging.
     * 
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param params
     *            an array of parameters for the method call
     */
    public void entering(String sourceClass, String sourceMethod,
            Object[] params) {
        if (internalIsLoggable(Level.FINER)) {
            StringBuffer msg = new StringBuffer(MSG_ENTERING);
            for (int i = 0; i < params.length; i++) {
                msg.append(" {" + i + "}"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            LogRecord record = new LogRecord(Level.FINER, msg.toString());
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating existing a method. A log record with log level
     * <code>Level.FINER</code>, log message "RETURN", and the specified
     * source class name and source method name is submitted for logging.
     * 
     * @param sourceClass
     *            the calling class name
     * @param sourceMethod
     *            the method name
     */
    public void exiting(String sourceClass, String sourceMethod) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, MSG_EXITING);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating exiting a method. A log record with log level
     * <code>Level.FINER</code>, log message "RETURN", and the specified
     * source class name and source method name and return value is submitted
     * for logging.
     * 
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param result
     *            the return value of the method call
     */
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, MSG_EXITING + " {0}"); //$NON-NLS-1$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { result });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating throwing an exception. A log record with log
     * level <code>Level.FINER</code>, log message "THROW", and the specified
     * source class name and source method name and <code>Throwable</code>
     * object is submitted for logging.
     * 
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param thrown
     *            the <code>Throwable</code> object
     */
    public void throwing(String sourceClass, String sourceMethod,
            Throwable thrown) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, MSG_THROWING);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.SEVERE</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void severe(String msg) {
        if (internalIsLoggable(Level.SEVERE)) {
            LogRecord record = new LogRecord(Level.SEVERE, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.WARNING</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void warning(String msg) {
        if (internalIsLoggable(Level.WARNING)) {
            LogRecord record = new LogRecord(Level.WARNING, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.INFO</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void info(String msg) {
        if (internalIsLoggable(Level.INFO)) {
            LogRecord record = new LogRecord(Level.INFO, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.CONFIG</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void config(String msg) {
        if (internalIsLoggable(Level.CONFIG)) {
            LogRecord record = new LogRecord(Level.CONFIG, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.FINE</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void fine(String msg) {
        if (internalIsLoggable(Level.FINE)) {
            LogRecord record = new LogRecord(Level.FINE, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.FINER</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void finer(String msg) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level <code>Level.FINEST</code>.
     * 
     * @param msg
     *            the message to log
     */
    public void finest(String msg) {
        if (internalIsLoggable(Level.FINEST)) {
            LogRecord record = new LogRecord(Level.FINEST, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level.
     * 
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log
     */
    public void log(Level logLevel, String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied parameter.
     * 
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log
     * @param param
     *            the parameter associated with the event that need to be logged
     */
    public void log(Level logLevel, String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied parameter array.
     * 
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log
     * @param params
     *            the parameter array associated with the event that need to be
     *            logged
     */
    public void log(Level logLevel, String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied
     * <code>Throwable</code> object.
     * 
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log
     * @param thrown
     *            the <code>Throwable</code> object associated with the event
     *            that need to be logged
     */
    public void log(Level logLevel, String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a given log record. Only those with a logging level no lower than
     * this logger's level will be submitted to this logger's handlers for
     * logging. If <code>getUseParentHandlers()</code> is <code>true</code>,
     * the log record will also be submitted to the parent logger's handlers,
     * potentially recursively up the namespace.
     * <p>
     * Since all other log methods call this method to actually perform the
     * logging action, subclasses of this class can override this method to
     * catch all logging activities.
     * </p>
     * 
     * @param record
     *            the log record to be logged
     */
    public synchronized void log(LogRecord record) {
        /*
         * This method is synchronized so that all other log methods are
         * synchronized because they all call log(LogRecord) to actually perform
         * the action.
         * 
         * Also note all other logging methods checks whether to do the logging.
         * Since they all call log(LogRecord) to actually perform the action,
         * this check is done twice. This leaves some room for performance
         * improvement. Possible ways: 1. Use an instance variable
         * "lastLoggableRecord" to save the record that passed the first check,
         * so that the second check may be skipped if log(LogRecord) detects the
         * supplied record is the same as "lastLoggableRecord". This field is
         * then reset. 2. Use the field "millis" of LogRecord, negative its
         * value to indicate it's been checked, then revert its value. Both
         * approaches are ugly but might be more efficient.
         */
        if (internalIsLoggable(record.getLevel())) {
            // apply the filter if any
            if (null != this.filter) {
                if (!this.filter.isLoggable(record)) {
                    return;
                }
            }
            /*
             * call the handlers of this logger, throw any exception that
             * occurs
             */
            initHandler();
            for (Handler element : handlers) {
                element.publish(record);
            }
            // call the parent's handlers if set useParentHandlers
            Logger temp = this;
            while (temp.parent != null && temp.getUseParentHandlers()) {
                Logger anyParent = temp.parent;
                Handler[] ha = anyParent.getHandlers();
                for (Handler element : ha) {
                    element.publish(record);
                }
                temp = anyParent;   
            }
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and parameter.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     * @param param
     *            the parameter associated with the event that need to be logged
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and parameter array.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     * @param params
     *            the parameter array associated with the event that need to be
     *            logged
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and <code>Throwable</code> object.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     * @param thrown
     *            the <code>Throwable</code> object
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name, using the given resource bundle to localize the
     * message.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle, used to localize the message
     * @param msg
     *            the message to be logged
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and parameter, using the given resource bundle to
     * localize the message.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle, used to localize the message
     * @param msg
     *            the message to be logged
     * @param param
     *            the parameter associated with the event that need to be logged
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and parameter array, using the given resource
     * bundle to localize the message.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle, used to localize the message
     * @param msg
     *            the message to be logged
     * @param params
     *            the parameter array associated with the event that need to be
     *            logged
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name and <code>Throwable</code> object, using the
     * given resource bundle to localize the message.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle, used to localize the message
     * @param msg
     *            the message to be logged
     * @param thrown
     *            the <code>Throwable</code> object
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            log(record);
        }
    }

    /*
     * This security manager is used to access the class context.
     */
    private static class PrivateSecurityManager extends SecurityManager {
        public Class[] privateGetClassContext() {
            return super.getClassContext();
        }
    }

}

