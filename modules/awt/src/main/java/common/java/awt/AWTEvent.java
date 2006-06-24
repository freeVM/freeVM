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
 * @author Dmitry A. Durnev, Michael Danilov
 * @version $Revision$
 */
package java.awt;

import java.util.EventObject;
import java.util.Hashtable;

import java.awt.event.*;

public abstract class AWTEvent extends EventObject {
    private static final long serialVersionUID = -1825314779160409405L;

    public static final long COMPONENT_EVENT_MASK = 1;

    public static final long CONTAINER_EVENT_MASK = 2;

    public static final long FOCUS_EVENT_MASK = 4;

    public static final long KEY_EVENT_MASK = 8;

    public static final long MOUSE_EVENT_MASK = 16;

    public static final long MOUSE_MOTION_EVENT_MASK = 32;

    public static final long WINDOW_EVENT_MASK = 64;

    public static final long ACTION_EVENT_MASK = 128;

    public static final long ADJUSTMENT_EVENT_MASK = 256;

    public static final long ITEM_EVENT_MASK = 512;

    public static final long TEXT_EVENT_MASK = 1024;

    public static final long INPUT_METHOD_EVENT_MASK = 2048;

    public static final long PAINT_EVENT_MASK = 8192;

    public static final long INVOCATION_EVENT_MASK = 16384;

    public static final long HIERARCHY_EVENT_MASK = 32768;

    public static final long HIERARCHY_BOUNDS_EVENT_MASK = 65536;

    public static final long MOUSE_WHEEL_EVENT_MASK = 131072;

    public static final long WINDOW_STATE_EVENT_MASK = 262144;

    public static final long WINDOW_FOCUS_EVENT_MASK = 524288;

    public static final int RESERVED_ID_MAX = 1999;

    private static final Hashtable<Integer, EventDescriptor> eventsMap = new Hashtable<Integer, EventDescriptor>();

    private static EventConverter converter;

    protected int id;

    protected boolean consumed;

    boolean dispatchedByKFM;

    static {
        eventsMap.put(Integer.valueOf(KeyEvent.KEY_TYPED),
                new EventDescriptor(KEY_EVENT_MASK, KeyListener.class));
        eventsMap.put(Integer.valueOf(KeyEvent.KEY_PRESSED),
                new EventDescriptor(KEY_EVENT_MASK, KeyListener.class));
        eventsMap.put(Integer.valueOf(KeyEvent.KEY_RELEASED),
                new EventDescriptor(KEY_EVENT_MASK, KeyListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_CLICKED),
                new EventDescriptor(MOUSE_EVENT_MASK, MouseListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_PRESSED),
                new EventDescriptor(MOUSE_EVENT_MASK, MouseListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_RELEASED),
                new EventDescriptor(MOUSE_EVENT_MASK, MouseListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_MOVED),
                new EventDescriptor(MOUSE_MOTION_EVENT_MASK, MouseMotionListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_ENTERED),
                new EventDescriptor(MOUSE_EVENT_MASK, MouseListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_EXITED),
                new EventDescriptor(MOUSE_EVENT_MASK, MouseListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_DRAGGED),
                new EventDescriptor(MOUSE_MOTION_EVENT_MASK, MouseMotionListener.class));
        eventsMap.put(Integer.valueOf(MouseEvent.MOUSE_WHEEL),
                new EventDescriptor(MOUSE_WHEEL_EVENT_MASK, MouseWheelListener.class));
        eventsMap.put(Integer.valueOf(ComponentEvent.COMPONENT_MOVED),
                new EventDescriptor(COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(Integer.valueOf(ComponentEvent.COMPONENT_RESIZED),
                new EventDescriptor(COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(Integer.valueOf(ComponentEvent.COMPONENT_SHOWN),
                new EventDescriptor(COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(Integer.valueOf(ComponentEvent.COMPONENT_HIDDEN),
                new EventDescriptor(COMPONENT_EVENT_MASK, ComponentListener.class));
        eventsMap.put(Integer.valueOf(FocusEvent.FOCUS_GAINED),
                new EventDescriptor(FOCUS_EVENT_MASK, FocusListener.class));
        eventsMap.put(Integer.valueOf(FocusEvent.FOCUS_LOST),
                new EventDescriptor(FOCUS_EVENT_MASK, FocusListener.class));
        eventsMap.put(Integer.valueOf(PaintEvent.PAINT),
                new EventDescriptor(PAINT_EVENT_MASK, null));
        eventsMap.put(Integer.valueOf(PaintEvent.UPDATE),
                new EventDescriptor(PAINT_EVENT_MASK, null));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_OPENED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_CLOSING),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_CLOSED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_DEICONIFIED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_ICONIFIED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_STATE_CHANGED),
                new EventDescriptor(WINDOW_STATE_EVENT_MASK, WindowStateListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_LOST_FOCUS),
                new EventDescriptor(WINDOW_FOCUS_EVENT_MASK, WindowFocusListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_GAINED_FOCUS),
                new EventDescriptor(WINDOW_FOCUS_EVENT_MASK, WindowFocusListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_DEACTIVATED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(WindowEvent.WINDOW_ACTIVATED),
                new EventDescriptor(WINDOW_EVENT_MASK, WindowListener.class));
        eventsMap.put(Integer.valueOf(HierarchyEvent.HIERARCHY_CHANGED),
                new EventDescriptor(HIERARCHY_EVENT_MASK, HierarchyListener.class));
        eventsMap.put(Integer.valueOf(HierarchyEvent.ANCESTOR_MOVED),
                new EventDescriptor(HIERARCHY_BOUNDS_EVENT_MASK, HierarchyBoundsListener.class));
        eventsMap.put(Integer.valueOf(HierarchyEvent.ANCESTOR_RESIZED),
                new EventDescriptor(HIERARCHY_BOUNDS_EVENT_MASK, HierarchyBoundsListener.class));
        eventsMap.put(Integer.valueOf(ContainerEvent.COMPONENT_ADDED),
                new EventDescriptor(CONTAINER_EVENT_MASK, ContainerListener.class));
        eventsMap.put(Integer.valueOf(ContainerEvent.COMPONENT_REMOVED),
                new EventDescriptor(CONTAINER_EVENT_MASK, ContainerListener.class));
        eventsMap.put(Integer.valueOf(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED),
                new EventDescriptor(INPUT_METHOD_EVENT_MASK, InputMethodListener.class));
        eventsMap.put(Integer.valueOf(InputMethodEvent.CARET_POSITION_CHANGED),
                new EventDescriptor(INPUT_METHOD_EVENT_MASK, InputMethodListener.class));
        eventsMap.put(Integer.valueOf(InvocationEvent.INVOCATION_DEFAULT),
                new EventDescriptor(INVOCATION_EVENT_MASK, null));
        eventsMap.put(Integer.valueOf(ItemEvent.ITEM_STATE_CHANGED),
                new EventDescriptor(ITEM_EVENT_MASK, ItemListener.class));
        eventsMap.put(Integer.valueOf(TextEvent.TEXT_VALUE_CHANGED),
                new EventDescriptor(TEXT_EVENT_MASK, TextListener.class));
        eventsMap.put(Integer.valueOf(ActionEvent.ACTION_PERFORMED),
                new EventDescriptor(ACTION_EVENT_MASK, ActionListener.class));
        eventsMap.put(Integer.valueOf(AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED),
                new EventDescriptor(ADJUSTMENT_EVENT_MASK, AdjustmentListener.class));
        converter = new EventConverter();
    }

    public AWTEvent(Event event) {
        this(event.target, event.id);
    }

    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        consumed = false;
    }

    public int getID() {
        return id;
    }

    public void setSource(Object newSource) {
        source = newSource;
    }

    public String toString() {
        /* The format is based on 1.5 release behavior 
         * which can be revealed by the following code:
         * 
         * AWTEvent event = new AWTEvent(new Component(){}, 1){};
         * System.out.println(event);
         */
        String name = "";
        if (source instanceof Component && (source != null)) {
            Component comp = (Component) getSource();
            name = comp.getName();
            if (name == null) {
                name = "";
            }
        }
        return (getClass().getName() + "[" + paramString() + "]"
                + " on " + (name.length() > 0 ? name : source));
    }

    public String paramString() {
        //nothing to implement: all event types must override this method
        return "";
    }

    protected boolean isConsumed() {
        return consumed;
    }

    protected void consume() {
       consumed = true;
    }

    /**
     * Convert AWTEvent object to deprecated Event object
     *
     * @return new Event object which is a converted AWTEvent object or null
     *         if the conversion is not possible
     */
    Event getEvent() {

        if (id == ActionEvent.ACTION_PERFORMED) {
            ActionEvent ae = (ActionEvent) this;
            return converter.convertActionEvent(ae);

        } else if (id == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED) {
            AdjustmentEvent ae = (AdjustmentEvent) this;
            return converter.convertAdjustmentEvent(ae);

        } else if (id == ComponentEvent.COMPONENT_MOVED
                && source instanceof Window) {
            //the only type of Component events is COMPONENT_MOVED on window
            ComponentEvent ce = (ComponentEvent) this;
            return converter.convertComponentEvent(ce);

        } else if (id >= FocusEvent.FOCUS_FIRST && id <= FocusEvent.FOCUS_LAST) {
            //nothing to convert
        } else if (id == ItemEvent.ITEM_STATE_CHANGED) {
            ItemEvent ie = (ItemEvent) this;
            return converter.convertItemEvent(ie);

        } else if (id == KeyEvent.KEY_PRESSED || id == KeyEvent.KEY_RELEASED) {
            KeyEvent ke = (KeyEvent) this;
            return converter.convertKeyEvent(ke);
        } else if (id >= MouseEvent.MOUSE_FIRST && id <= MouseEvent.MOUSE_LAST) {
            MouseEvent me = (MouseEvent) this;
            return converter.convertMouseEvent(me);
        } else if (id == WindowEvent.WINDOW_CLOSING
                || id == WindowEvent.WINDOW_ICONIFIED
                || id == WindowEvent.WINDOW_DEICONIFIED) {
            //nothing to convert
        } else {
            return null;
        }

        return new Event(source, id, null);
    }


    static final class EventDescriptor {

        final long eventMask;

        final Class<?> listenerType;

        EventDescriptor(long eventMask, Class<?> listenerType) {
            this.eventMask = eventMask;
            this.listenerType = listenerType;
        }

    }
    static final class EventTypeLookup<T> {
        private AWTEvent lastEvent;
        private EventDescriptor lastEventDescriptor;

        EventDescriptor getEventDescriptor(AWTEvent event) {
            synchronized (this) {
                if (event != lastEvent) {
                    lastEvent = event;
                    lastEventDescriptor = eventsMap.get(Integer.valueOf(event.id));
                }

                return lastEventDescriptor;
            }
        }

        long getEventMask(AWTEvent event) {
            return getEventDescriptor(event).eventMask;
        }
    }

    static final class EventConverter {
        static final int OLD_MOD_MASK = Event.ALT_MASK | Event.CTRL_MASK
        | Event.META_MASK | Event.SHIFT_MASK;

        Event convertActionEvent(ActionEvent ae) {
            Event evt = new Event(ae.getSource(), ae.getID(), ae.getActionCommand());
            evt.when = ae.getWhen();
            evt.modifiers = ae.getModifiers() & OLD_MOD_MASK;

           /* if (source instanceof Button) {
                arg = ((Button) source).getLabel();
            } else if (source instanceof Checkbox) {
                arg = new Boolean(((Checkbox) source).getState());
            } else if (source instanceof CheckboxMenuItem) {
                arg = ((CheckboxMenuItem) source).getLabel();
            } else if (source instanceof Choice) {
                arg = ((Choice) source).getSelectedItem();
            } else if (source instanceof List) {
                arg = ((List) source).getSelectedItem();
            } else if (source instanceof MenuItem) {
                arg = ((MenuItem) source).getLabel();
            } else if (source instanceof TextField) {
                arg = ((TextField) source).getText();
            }
*/
            return evt;
        }

        Event convertAdjustmentEvent(AdjustmentEvent ae) {
            //TODO: Event.SCROLL_BEGIN/SCROLL_END
            return new Event(ae.source, ae.id + ae.getAdjustmentType() - 1,
                    Integer.valueOf(ae.getValue()));
        }

        Event convertComponentEvent(ComponentEvent ce) {
            Component comp = ce.getComponent();
            Event evt = new Event(comp, Event.WINDOW_MOVED, null);
            evt.x = comp.getX();
            evt.y = comp.getY();
            return evt;
        }

        Event convertItemEvent(ItemEvent ie) {
            int oldId = ie.id + ie.getStateChange() - 1;
            Object source = ie.source;
            int idx = -1;
            if (source instanceof List) {
                List list = (List) source;
                idx = list.getSelectedIndex();
            }
            else if (source instanceof Choice) {
                Choice choice = (Choice) source;
                idx = choice.getSelectedIndex();
            }
            Object arg = idx >= 0 ? Integer.valueOf(idx) : null;
            return new Event(source, oldId, arg);
        }

        Event convertKeyEvent(KeyEvent ke) {
            int oldId = ke.id;
            //leave only old Event's modifiers

            int mod = ke.getModifiers() & OLD_MOD_MASK;
            Component comp = ke.getComponent();
            char keyChar = ke.getKeyChar();
            int keyCode = ke.getKeyCode();
            int key = convertKey(keyChar, keyCode);
            if (key >= Event.HOME && key <= Event.INSERT) {
                oldId += 2; //non-ASCII key -> action key
            }
            return new Event(comp, ke.getWhen(), oldId, 0, 0, key, mod);
        }

        Event convertMouseEvent(MouseEvent me) {
            int id = me.id;
            if (id != MouseEvent.MOUSE_CLICKED) {
                Event evt = new Event(me.source, id, null);
                evt.x = me.getX();
                evt.y = me.getY();
                int mod = me.getModifiers();
                //in Event modifiers mean button number for mouse events:
                evt.modifiers = mod & (Event.ALT_MASK | Event.META_MASK);
                if (id == MouseEvent.MOUSE_PRESSED) {
                    evt.clickCount = me.getClickCount();
                }
                return evt;
            }
            return null;
        }

        int convertKey(char keyChar, int keyCode) {
            int key;
            //F1 - F12
            if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
                key = Event.F1 + keyCode - KeyEvent.VK_F1;
            } else {
                switch (keyCode) {
                default: //non-action key
                    key = keyChar;
                    break;
                //action keys:
                case KeyEvent.VK_HOME:
                    key = Event.HOME;
                    break;
                case KeyEvent.VK_END:
                    key = Event.END;
                    break;
                case KeyEvent.VK_PAGE_UP:
                    key = Event.PGUP;
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    key = Event.PGDN;
                    break;
                case KeyEvent.VK_UP:
                    key = Event.UP;
                    break;
                case KeyEvent.VK_DOWN:
                    key = Event.DOWN;
                    break;
                case KeyEvent.VK_LEFT:
                    key = Event.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
                    key = Event.RIGHT;
                    break;
                case KeyEvent.VK_PRINTSCREEN:
                    key = Event.PRINT_SCREEN;
                    break;
                case KeyEvent.VK_SCROLL_LOCK:
                    key = Event.SCROLL_LOCK;
                    break;
                case KeyEvent.VK_CAPS_LOCK:
                    key = Event.CAPS_LOCK;
                    break;
                case KeyEvent.VK_NUM_LOCK:
                    key = Event.NUM_LOCK;
                    break;
                case KeyEvent.VK_PAUSE:
                    key = Event.PAUSE;
                    break;
                case KeyEvent.VK_INSERT:
                    key = Event.INSERT;
                    break;
                }
            }
            return key;
        }

    }

}
