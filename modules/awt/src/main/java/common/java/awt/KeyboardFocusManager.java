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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.harmony.awt.ListenerList;


public abstract class KeyboardFocusManager
implements KeyEventDispatcher, KeyEventPostProcessor {

    public static final int FORWARD_TRAVERSAL_KEYS = 0;

    public static final int BACKWARD_TRAVERSAL_KEYS = 1;

    public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;

    public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;

    final static int[] compTraversalIDs = { FORWARD_TRAVERSAL_KEYS,
                                           BACKWARD_TRAVERSAL_KEYS,
                                           UP_CYCLE_TRAVERSAL_KEYS };

    final static int[] contTraversalIDs = { FORWARD_TRAVERSAL_KEYS,
                                           BACKWARD_TRAVERSAL_KEYS,
                                           UP_CYCLE_TRAVERSAL_KEYS,
                                           DOWN_CYCLE_TRAVERSAL_KEYS };

    private final Map<Integer, Set<AWTKeyStroke>> defaultFocusTraversalKeys =
        new HashMap<Integer, Set<AWTKeyStroke>>();

    private FocusTraversalPolicy defaultFocusTraversalPolicy =
        new DefaultFocusTraversalPolicy();

    // focus state is static, i. e. 1 per class loader:

    static Component focusOwner;

    static Component actualFocusOwner;

    private static Component actualPrevFocusOwner;

    private static Component permanentFocusOwner;

    private static Container currentFocusCycleRoot;

    static Window activeWindow;

    private static Window actualActiveWindow;

    static Window focusedWindow;

    static Window actualFocusedWindow;

    private static Window prevFocusedWindow;

    final ListenerList<KeyEventDispatcher> keyEventDispatchers = new ListenerList<KeyEventDispatcher>();

    final ListenerList<KeyEventPostProcessor> keyEventPostProcessors = new ListenerList<KeyEventPostProcessor>();

    private PropertyChangeSupport propertyChangeSupport;

    private VetoableChangeSupport vetoableChangeSupport;


//    private final Toolkit toolkit = Toolkit.getDefaultToolkit();

    public KeyboardFocusManager() {
        for (int element : contTraversalIDs) {
            defaultFocusTraversalKeys.put(new Integer(element),
                    null);
        }
    }

    public void addKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        keyEventDispatchers.addUserListener(dispatcher);
    }

    public void addKeyEventPostProcessor(KeyEventPostProcessor processor) {
        keyEventPostProcessors.addUserListener(processor);
    }

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(propertyName,
                listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        getVetoableChangeSupport().addVetoableChangeListener(listener);
    }

    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener listener) {
        getVetoableChangeSupport().addVetoableChangeListener(propertyName,
                listener);
    }

    public void clearGlobalFocusOwner() {
        if (focusOwner != null) {
            setFocus(focusOwner, focusOwner.getWindowAncestor(),
                                    false, null, false, true);
        }
    }

    protected abstract void dequeueKeyEvents(long a0, Component a1);

    protected abstract void discardKeyEvents(Component a0);

    public abstract boolean dispatchEvent(AWTEvent a0);

    public abstract boolean dispatchKeyEvent(KeyEvent a0);

    public final void downFocusCycle() {
        if (focusOwner instanceof Container) {
            Container root = (Container) focusOwner;
            downFocusCycle(root);
        }
    }

    public abstract void downFocusCycle(Container a0);

    protected abstract void enqueueKeyEvents(long a0, Component a1);

    protected void firePropertyChange(String propertyName, Object oldValue,
                                      Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue,
                                                      newValue);
    }

    protected void fireVetoableChange(String propertyName, Object oldValue,
                                      Object newValue)
            throws PropertyVetoException {
        getVetoableChangeSupport().fireVetoableChange(propertyName, oldValue,
                                                      newValue);
    }

    public final void focusNextComponent() {
        focusNextComponent(getFocusOwner());
    }

    public abstract void focusNextComponent(Component a0);

    public final void focusPreviousComponent() {
        focusPreviousComponent(getFocusOwner());
    }

    public abstract void focusPreviousComponent(Component a0);

    public Window getActiveWindow() {
        return activeWindow;
    }

    public Container getCurrentFocusCycleRoot() {
        return currentFocusCycleRoot;
    }

    public static KeyboardFocusManager getCurrentKeyboardFocusManager() {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.lockAWT();
        try {
            if (toolkit.currentKeyboardFocusManager == null) {
                setCurrentKeyboardFocusManager(null);
            }
            return toolkit.currentKeyboardFocusManager;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public Set<AWTKeyStroke> getDefaultFocusTraversalKeys(int id) {
        Integer kId = Integer.valueOf(id);
        checkTraversalKeysID(defaultFocusTraversalKeys, kId);
        return defaultFocusTraversalKeys.get(kId);
    }

    public FocusTraversalPolicy getDefaultFocusTraversalPolicy() {
        return defaultFocusTraversalPolicy;
    }

    public Component getFocusOwner() {
        return focusOwner;
    }

    public Window getFocusedWindow() {
        return focusedWindow;
    }

    protected Window getGlobalActiveWindow() throws SecurityException {
        checkInstance();
        // TODO: get global active window somehow
        // (not the active window from the current class loader)
        return activeWindow;
    }

    protected Container getGlobalCurrentFocusCycleRoot() throws SecurityException {
        checkInstance();
        // TODO: get global current focus cycle root somehow
        // (not from the current class loader)
        return currentFocusCycleRoot;
    }

    protected Component getGlobalFocusOwner() throws SecurityException {
        checkInstance();
        // TODO: get global focus owner somehow
        // (not from the current class loader)
        return focusOwner;
    }

    /**
     * This method will throw a SecurityException if this KeyboardFocusManager
     * is not the current KeyboardFocusManager for the calling thread's context.
     *
     * @throws SecurityException
     */
    private void checkInstance() throws SecurityException {
        if (getCurrentKeyboardFocusManager() != this) {
            throw new SecurityException(
                    "this KeyboardFocusManager is not installed "
                            + "in the current thread's context");
        }
    }

    protected Window getGlobalFocusedWindow() throws SecurityException {
        checkInstance();
        // TODO: get global focused window somehow
        // (not from the current class loader)
        return focusedWindow;
    }

    protected Component getGlobalPermanentFocusOwner()
        throws SecurityException {

        checkInstance();
        // TODO: get global permanent focus owner somehow
        // (not from the current class loader)

        return permanentFocusOwner;
    }

    protected List<KeyEventDispatcher> getKeyEventDispatchers() {
        return keyEventDispatchers.getUserListeners();
    }

    protected List<KeyEventPostProcessor> getKeyEventPostProcessors() {
        return keyEventPostProcessors.getUserListeners();
    }

    public Component getPermanentFocusOwner() {
        // TODO: return null if the permanent focus owner is not a member of the
        // calling thread's context
        return permanentFocusOwner;
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return getPropertyChangeSupport().getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return getPropertyChangeSupport().getPropertyChangeListeners(propertyName);
    }

    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return getVetoableChangeSupport().getVetoableChangeListeners(propertyName);
    }

    public VetoableChangeListener[] getVetoableChangeListeners() {
        return getVetoableChangeSupport().getVetoableChangeListeners();
    }

    public abstract boolean postProcessKeyEvent(KeyEvent a0);

    public abstract void processKeyEvent(Component a0, KeyEvent a1);

    public final void redispatchEvent(Component target, AWTEvent e) {
        e.dispatchedByKFM = true;
        target.dispatchEvent(e);
    }

    public void removeKeyEventDispatcher(KeyEventDispatcher dispatcher) {
       keyEventDispatchers.removeUserListener(dispatcher);
    }

    public void removeKeyEventPostProcessor(KeyEventPostProcessor processor) {
        keyEventPostProcessors.removeUserListener(processor);
    }

    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(propertyName,
                                                                listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        getVetoableChangeSupport().removeVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener listener) {
        getVetoableChangeSupport().removeVetoableChangeListener(propertyName,
                                                                listener);
    }

    public static void setCurrentKeyboardFocusManager(KeyboardFocusManager newManager)
            throws SecurityException {
        KeyboardFocusManager oldManager;
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.lockAWT();
        try {
            SecurityManager secMan = System.getSecurityManager();
            String permission = "replaceKeyboardFocusManager";
            if (secMan != null) {
                secMan.checkPermission(new AWTPermission(permission));
            }
            oldManager = toolkit.currentKeyboardFocusManager;
            toolkit.currentKeyboardFocusManager =
                ((newManager != null) ? newManager :
                 new DefaultKeyboardFocusManager());
        } finally {
            toolkit.unlockAWT();
        }
        if (oldManager == newManager) {
            return;
        }
        String propName = "managingFocus";
        if (oldManager != null) {
            oldManager.firePropertyChange(propName, Boolean.TRUE, Boolean.FALSE);
        }
        newManager.firePropertyChange(propName, Boolean.FALSE, Boolean.TRUE);
    }

    public void setDefaultFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
        Integer kId = Integer.valueOf(id);
        Set<AWTKeyStroke> oldKeyStrokes = defaultFocusTraversalKeys.get(kId);
        checkTraversalKeysID(defaultFocusTraversalKeys, kId);
        checkKeyStrokes(contTraversalIDs, defaultFocusTraversalKeys, kId,
                        (Set<AWTKeyStroke>) keystrokes);
        defaultFocusTraversalKeys.put(kId, (Set<AWTKeyStroke>) keystrokes);
        String propName = null;
        switch (id) {
        case FORWARD_TRAVERSAL_KEYS:
            propName = "forwardDefaultFocusTraversalKeys";
            break;
        case BACKWARD_TRAVERSAL_KEYS:
            propName = "backwardDefaultFocusTraversalKeys";
            break;
        case UP_CYCLE_TRAVERSAL_KEYS:
            propName = "upCycleDefaultFocusTraversalKeys";
            break;
        case DOWN_CYCLE_TRAVERSAL_KEYS:
            propName = "downCycleDefaultFocusTraversalKeys";
            break;
        }
        firePropertyChange(propName, oldKeyStrokes, keystrokes);
    }

    public void setDefaultFocusTraversalPolicy(FocusTraversalPolicy defaultPolicy) {
        if (defaultPolicy == null) {
            throw new IllegalArgumentException("default focus traversal policy" +
                    " cannot be null");
        }
        FocusTraversalPolicy oldPolicy = defaultFocusTraversalPolicy;
        defaultFocusTraversalPolicy = defaultPolicy;
        firePropertyChange("defaultFocusTraversalPolicy", oldPolicy,
                           defaultFocusTraversalPolicy);
    }

    protected void setGlobalActiveWindow(Window activeWindow) {
        String propName = "activeWindow";
        // fire Vetoable change[before it is reflected in Java focus state],
        // catch veto exception
        try {
            fireVetoableChange(propName, KeyboardFocusManager.activeWindow,
                               activeWindow);
        } catch (PropertyVetoException e) {
            // abort the change, i. e.
            // don't reflect the change in KFM, don't report to property change
            // listeners
            return;
        }
        Window oldActiveWindow = KeyboardFocusManager.activeWindow;
        KeyboardFocusManager.activeWindow = activeWindow;
        firePropertyChange(propName, oldActiveWindow,
                           KeyboardFocusManager.activeWindow);
    }

    public void setGlobalCurrentFocusCycleRoot(Container newFocusCycleRoot) {
        Container oldFocusCycleRoot = currentFocusCycleRoot;
        currentFocusCycleRoot = newFocusCycleRoot;
        firePropertyChange("currentFocusCycleRoot", oldFocusCycleRoot,
                           currentFocusCycleRoot);
    }

    protected void setGlobalFocusOwner(Component focusOwner) {
        String propName = "focusOwner";

        // fire Vetoable change[before it is reflected in Java focus state],
        // catch veto exception

        try {
            fireVetoableChange(propName, KeyboardFocusManager.focusOwner,
                               focusOwner);
        } catch (PropertyVetoException e) {
            // abort the change, i. e.
            // don't reflect the change in KFM, don't report to property change
            // listeners
            return;
        }
        Component oldFocusOwner = KeyboardFocusManager.focusOwner;
        if ((focusOwner == null) || focusOwner.isFocusable()) {
            KeyboardFocusManager.focusOwner = focusOwner;
            if ((focusOwner != null) && focusOwner != getCurrentFocusCycleRoot()) {
                //don't clear current focus cycle root every time a component
                //is losing focus
                //TODO: do it[clear] somewhere else(maybe Window.dispose()??)
                Container root =
                     ( (focusOwner instanceof Window) ? (Window)focusOwner :
                        focusOwner.getFocusCycleRootAncestor() );

                if (root == null || root.isFocusCycleRoot()) {
                    setGlobalCurrentFocusCycleRoot(root);
                }
            }
        }

        firePropertyChange(propName, oldFocusOwner,
                           KeyboardFocusManager.focusOwner);
    }

    protected void setGlobalFocusedWindow(Window focusedWindow) {
        String propName = "focusedWindow";

        // fire Vetoable change[before it is reflected in Java focus state],
        // catch veto exception

        try {
            fireVetoableChange(propName, KeyboardFocusManager.focusedWindow,
                               focusedWindow);
        } catch (PropertyVetoException e) {
            // abort the change, i. e.
            // don't reflect the change in KFM, don't report to property change
            // listeners
            return;
        }

        Window oldFocusedWindow = KeyboardFocusManager.focusedWindow;
        if (focusedWindow == null || focusedWindow.isFocusableWindow()) {
            KeyboardFocusManager.focusedWindow = focusedWindow;
        }

        firePropertyChange(propName, oldFocusedWindow,
                           KeyboardFocusManager.focusedWindow);

    }

    protected void setGlobalPermanentFocusOwner(Component permanentFocusOwner) {
        String propName = "permanentFocusOwner";

        // fire Vetoable change[before it is reflected in Java focus state],
        // catch veto exception

        try {
            fireVetoableChange(propName, KeyboardFocusManager.permanentFocusOwner,
                               permanentFocusOwner);
        } catch (PropertyVetoException e) {
            // abort the change, i. e.
            // don't reflect the change in KFM,
            // don't report to property change listeners
            return;
        }
        Component oldPermanentFocusOwner =
            KeyboardFocusManager.permanentFocusOwner;
        if ((permanentFocusOwner == null) ||
             permanentFocusOwner.isFocusable()) {
            KeyboardFocusManager.permanentFocusOwner = permanentFocusOwner;
            setGlobalFocusOwner(permanentFocusOwner);
        }
        firePropertyChange(propName, oldPermanentFocusOwner,
                           KeyboardFocusManager.permanentFocusOwner);
    }

    public final void upFocusCycle() {
        upFocusCycle(getFocusOwner());
    }

    public abstract void upFocusCycle(Component a0);

    static void checkTraversalKeysID(Map<?, ?> keysMap, Integer id) {
        if (!keysMap.containsKey(id)) {
            throw new IllegalArgumentException(
                    "invalid focus traversal key identifier");
        }
    }

    static void checkKeyStrokes(int[] traversalIDs, Map<Integer, Set<AWTKeyStroke>> traversalKeys,
                                Integer kId, Set<AWTKeyStroke> keystrokes) {
        if (keystrokes == null) {
            return;
        }

        Iterator<AWTKeyStroke> i = keystrokes.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            AWTKeyStroke key = o instanceof AWTKeyStroke ? (AWTKeyStroke) o
                                                        : null;
            if (key == null) {
                throw new IllegalArgumentException(
                        o == null ? "cannot set null focus traversal key"
                                 : "cannot set focus traversal key " +
                                        "to non-AWTKeyStroke");
            } // actually throw ClassCastException ??
            if (key.getKeyEventType() == KeyEvent.KEY_TYPED) {
                throw new IllegalArgumentException(
                        "focus traversal keys cannot map to KEY_TYPED events");
            }

            // throw exception if such a KeyStroke is already present for
            // another id

            for (int element : traversalIDs) {
                Integer theID = new Integer(element);
                Object val = traversalKeys.get(theID);
                if ((!theID.equals(kId)) &&
                    (val instanceof Set) &&
                    ((Set)val).contains(key)) {

                    throw new IllegalArgumentException(
                            "focus traversal keys must be unique" +
                            " for a Component");
                }
            }
        }

    }

    boolean requestFocus(Component c, boolean temporary,
                         boolean crossWindow, boolean callCB) {
        Window wnd = ((c != null) ? c.getWindowAncestor() : null);
        return requestFocus(c, wnd, temporary, crossWindow, callCB);

    }

    /**
     * internal "requestFocus": posts all necessary Focus & focus-related Window
     * events to eventQueue and updates internal focus state. When called from
     * Component's request focus callCB is set to true, when called directly
     * from native event dispatching code - to false.
     */
    boolean requestFocus(Component c, Window wnd, boolean temporary,
                         boolean crossWindow, boolean callCB) {

        Window focusedWnd = actualFocusedWindow;
        Window activeWnd = actualActiveWindow;

        // don't take focus from other applications:
        // change active window from null only if native
        // event is received(i. e. callCB is false)
        if (callCB && activeWnd == null) {
            if (crossWindow && (wnd != null) && (c != null)) {
                // remember the request to grant it when
                // window is later focused by the user
                wnd.setRequestedFocus(c);
            }
            return false;
        }

        if (callCB && !checkWindow(wnd)) {
            return false;
        }

        setFocus(actualFocusOwner, wnd, false, c, temporary, callCB);

        // in case of cross-window focus transfer
        // remember that this component had requested focus in that window

        if (crossWindow && (focusedWnd != wnd)) {
            wnd.setRequestedFocus(c);
            wnd.behaviour.setFocus(true, focusedWnd); // try to change
            // focusedWindow(?)
        }

        //        if (!wnd.isFocused()) {
        //            return false; //async focus - wait until window actually gains
        //            // focus
        //        }
        wnd.setRequestedFocus(c);
        setFocus(c, wnd, true, actualPrevFocusOwner, temporary, callCB);

        if (wnd != null) {
            wnd.setFocusOwner(c);
            wnd.setRequestedFocus(null);
        }
        return true;
    }

    /**
     * Perform additional checks to determine if a Window can
     * become focused
     * @param wnd
     * @return
     */
    private boolean checkWindow(Window wnd) {
        if (wnd == null) {
            return false;
        }

        if (wnd instanceof EmbeddedWindow) {
            // TODO: query state of EmbeddedWindow's owner
            return true;
        }

        // FIXME: explicitly deny focus requests for components
        // inside iconified/shaded windows:
        if ((getOwningFrame(wnd).getExtendedState() & Frame.ICONIFIED) != 0) {
            return false;
        }
        return true;
    }

    Frame getOwningFrame(Window w) {
        Window wnd;
        for (wnd = w; (wnd != null) && !(wnd instanceof Frame);
             wnd = wnd.getOwner()) {
            ;
        }
        return (Frame)wnd;
    }

    /**
     * all focus related events are posted to EventQueue and internal(non-Java)
     * focus state is updated to be able to post some events correctly As
     * opposed to focus spec user-defined KeyboardFocusManager doesn't
     * have to take care about proper event ordering: events are posted in
     * proper order
     */
    void setFocus(Component c, Window wnd, boolean focus, Component opposite,
                  boolean temporary, boolean callCB) {
        Window focusedWnd = actualFocusedWindow;


        Window oppositeAncestorWnd =
            ( (opposite != null) ? opposite.getWindowAncestor() :
              (focus ? focusedWnd : null) );
        Window ancestorWnd = (!focus ? focusedWnd : wnd);

        if (!focus && (ancestorWnd == null)) {
            ancestorWnd = actualActiveWindow;
        }

        if (focus) {
            postWindowEvent(ancestorWnd, oppositeAncestorWnd, focus);
        }

        if (c != null) {
            // when losing focus to some component in other window
            // post temporary event:
            if (!focus && (opposite != null) && !callCB) {
                temporary =
                    (c.getWindowAncestor() != opposite.getWindowAncestor());
            }
            FocusEvent newEvent = new FocusEvent(c,
                    focus ? FocusEvent.FOCUS_GAINED : FocusEvent.FOCUS_LOST,
                    temporary, opposite);

            // remember previous focus owner to be able to post it as opposite
            // later
            // [when opposite component gains focus]
            // but clear it if application loses focus
            if (!focus) {
                actualPrevFocusOwner =
                    ( (opposite != null) ? actualFocusOwner : null );
            }
            actualFocusOwner = ( focus ? c :
                ((c == actualFocusOwner) ? null : actualFocusOwner) );

            c.postEvent(newEvent);
        }

        // post window events when losing focus only if
        // there's opposite component or
        // events come from native layer[if opposite is null],
        // i. e. don't post them if called from clearGlobalFocusOwner()
        if (!focus && ((opposite != null) || !callCB)) {
            prevFocusedWindow = (c == null ? actualFocusedWindow : null);
            postWindowEvent(ancestorWnd, oppositeAncestorWnd, focus);
        }
        if (focus && callCB) {
            c.behaviour.setFocus(focus, opposite);
        }

    }

    /**
     * set focus to the appropriate child Component of the given Window
     * as if it is the focused Window
     */
    boolean requestFocusInWindow(Window wnd, boolean callCB) {

        if (wnd == null) {
            return false;
        }
        
        Component lastReqFocus = wnd.getRequestedFocus();
        if ((lastReqFocus != null) && (lastReqFocus.getWindowAncestor() != wnd)) {
            lastReqFocus = null;
            wnd.setRequestedFocus(null);
        }
        Component lastFocusOwner = wnd.getMostRecentFocusOwner();
        if ((lastFocusOwner != null) && lastFocusOwner.getWindowAncestor() != wnd) {
            lastFocusOwner = null;
        }
        Component compToFocus = ((lastReqFocus != null) ? lastReqFocus :
                                                          lastFocusOwner);
                

        if (compToFocus != null) {
            return requestFocus(compToFocus, wnd, false, false, callCB);
        }

        // even if there's no component to focus
        // we can try to focus window itself
        return requestFocus(wnd, wnd, false, false, callCB);

    }

    /**
     * all focus related WindowEvents are posted to EventQueue
     * and internal(non-Java) focus state is immediately updated
     * (Java focus state is updated only right before actually
     * dispatching these events to components)
     * Activation events are also posted from here, so
     * KeyboardFocusManager(if replaced by user) doesn't have to care about
     * "synthesizing" them, as opposed to focus spec.
     * @return - true if focused Window changed
     */
    boolean postWindowEvent(Window wnd, Window opposite, boolean focus) {
        Window focusedWnd = actualFocusedWindow;

        Window decorWnd = ((wnd != null) ? wnd.getFrameDialogOwner() : null);
        int focusEventId =
            (focus ? WindowEvent.WINDOW_GAINED_FOCUS :
             WindowEvent.WINDOW_LOST_FOCUS);
        int activationEventId =
            (focus ? WindowEvent.WINDOW_ACTIVATED :
             WindowEvent.WINDOW_DEACTIVATED);

        if ((opposite == null) && (prevFocusedWindow != null)) {
            opposite = prevFocusedWindow;
        }
        Window oppositeDecorWnd = ((opposite != null) ?
                                   opposite.getFrameDialogOwner() :
                                   null);

        boolean focusedWindowChanged =
            ( (wnd != null) &&
              (focus ? focusedWnd != wnd :
               opposite != wnd) );
        boolean activeWindowChanged =
            ( (decorWnd != null) &&
              (focus ? actualActiveWindow != decorWnd :
               oppositeDecorWnd != decorWnd) );
        WindowEvent activationEvent =
            ( activeWindowChanged ?
              new WindowEvent(decorWnd,
                              activationEventId,
                              oppositeDecorWnd) :
              null );

        if (activeWindowChanged && focus) {
            decorWnd.postEvent(activationEvent);
            actualActiveWindow = decorWnd;
        }

        if (focusedWindowChanged) {
            wnd.postEvent(new WindowEvent(wnd, focusEventId, opposite));
            actualFocusedWindow = (focus ? wnd : null);
        }

        if (activeWindowChanged && !focus) {
            decorWnd.postEvent(activationEvent);
            actualActiveWindow = null;
        }
        return focusedWindowChanged;
    }

    private PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    private VetoableChangeSupport getVetoableChangeSupport() {
        if (vetoableChangeSupport == null) {
            vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        return vetoableChangeSupport;
    }
}