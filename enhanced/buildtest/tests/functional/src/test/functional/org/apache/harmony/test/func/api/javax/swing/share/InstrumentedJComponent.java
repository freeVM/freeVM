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
 */

package org.apache.harmony.test.func.api.javax.swing.share;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.EventListener;
import java.util.Locale;
import java.util.Set;

import javax.accessibility.AccessibleContext;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComponentUI;

public class InstrumentedJComponent extends JComponent {
    public void addAncestorListener(AncestorListener arg0) {
        InstrumentedUILog.add("1");
        super.addAncestorListener(arg0);
    }

    public void addNotify() {
        InstrumentedUILog.add(new Object[] { "addNotify" });
        super.addNotify();
    }

    public synchronized void addPropertyChangeListener(
            PropertyChangeListener arg0) {
        InstrumentedUILog.add("3");
        super.addPropertyChangeListener(arg0);
    }

    public synchronized void addPropertyChangeListener(String arg0,
            PropertyChangeListener arg1) {
        InstrumentedUILog.add("4");
        super.addPropertyChangeListener(arg0, arg1);
    }

    public synchronized void addVetoableChangeListener(
            VetoableChangeListener arg0) {
        InstrumentedUILog.add("5");
        super.addVetoableChangeListener(arg0);
    }

    public void computeVisibleRect(Rectangle arg0) {
        InstrumentedUILog.add("6");
        super.computeVisibleRect(arg0);
    }

    public boolean contains(int arg0, int arg1) {
        InstrumentedUILog
                .add(new Object[] { "contains", "" + arg0 + " " + arg1 });
        return super.contains(arg0, arg1);
    }

    public JToolTip createToolTip() {
        InstrumentedUILog.add("8");
        return super.createToolTip();
    }

    public boolean isEnabled() {
        InstrumentedUILog.add(new Object[] { "isEnabled" });
        return super.isEnabled();
    }

    public void disable() {
        InstrumentedUILog.add(new Object[] { "disable" });
        super.disable();
    }

    public void enable() {
        InstrumentedUILog.add(new Object[] { "enable" });
        super.enable();
    }

    public void firePropertyChange(String arg0, boolean arg1, boolean arg2) {
        InstrumentedUILog.add(new Object[] { "firePropertyChangeBoolean", arg0,
                "" + arg1, "" + arg2 });
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, byte arg1, byte arg2) {
        InstrumentedUILog.add("12");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, char arg1, char arg2) {
        InstrumentedUILog.add("13");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, double arg1, double arg2) {
        InstrumentedUILog.add("14");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, float arg1, float arg2) {
        InstrumentedUILog.add("15");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, int arg1, int arg2) {
        InstrumentedUILog.add("16");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, long arg1, long arg2) {
        InstrumentedUILog.add("17");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    protected void firePropertyChange(String arg0, Object arg1, Object arg2) {
        InstrumentedUILog.add(new Object[] { "firePropertyChange", arg0, arg1,
                arg2 });
        super.firePropertyChange(arg0, arg1, arg2);
    }

    public void firePropertyChange(String arg0, short arg1, short arg2) {
        InstrumentedUILog.add("19");
        super.firePropertyChange(arg0, arg1, arg2);
    }

    protected void fireVetoableChange(String arg0, Object arg1, Object arg2)
            throws PropertyVetoException {
        InstrumentedUILog.add("20");
        super.fireVetoableChange(arg0, arg1, arg2);
    }

    public AccessibleContext getAccessibleContext() {
        InstrumentedUILog.add("21");
        return super.getAccessibleContext();
    }

    public ActionListener getActionForKeyStroke(KeyStroke arg0) {
        InstrumentedUILog.add("22");
        return super.getActionForKeyStroke(arg0);
    }

    public float getAlignmentX() {
        InstrumentedUILog.add(new Object[] { "getAlignmentX" });
        return super.getAlignmentX();
    }

    public float getAlignmentY() {
        InstrumentedUILog.add(new Object[] { "getAlignmentY" });
        return super.getAlignmentY();
    }

    public AncestorListener[] getAncestorListeners() {
        InstrumentedUILog.add("25");
        return super.getAncestorListeners();
    }

    public boolean getAutoscrolls() {
        InstrumentedUILog.add("26");
        return super.getAutoscrolls();
    }

    public Border getBorder() {
        InstrumentedUILog.add(new Object[] { "getBorder" });
        return super.getBorder();
    }

    public Rectangle getBounds(Rectangle arg0) {
        InstrumentedUILog.add("28");
        return super.getBounds(arg0);
    }

    protected Graphics getComponentGraphics(Graphics arg0) {
        InstrumentedUILog.add("29");
        return super.getComponentGraphics(arg0);
    }

    public int getConditionForKeyStroke(KeyStroke arg0) {
        InstrumentedUILog.add("30");
        return super.getConditionForKeyStroke(arg0);
    }

    public int getDebugGraphicsOptions() {
        InstrumentedUILog.add("31");
        return super.getDebugGraphicsOptions();
    }

    public Graphics getGraphics() {
        InstrumentedUILog.add("32");
        return super.getGraphics();
    }

    public int getHeight() {
        InstrumentedUILog.add(new Object[] { "getHeight" });
        return super.getHeight();
    }

    public InputVerifier getInputVerifier() {
        InstrumentedUILog.add(new Object[] { "getInputVerifier" });
        return super.getInputVerifier();
    }

    public Insets getInsets() {
        InstrumentedUILog.add("35");
        return super.getInsets();
    }

    public Insets getInsets(Insets arg0) {
        InstrumentedUILog.add("36");
        return super.getInsets(arg0);
    }

    public EventListener[] getListeners(Class arg0) {
        InstrumentedUILog.add(new Object[] { "getListeners" });
        return super.getListeners(arg0);
    }

    public Point getLocation(Point arg0) {
        InstrumentedUILog.add("38");
        return super.getLocation(arg0);
    }

    public Dimension getMaximumSize() {
        InstrumentedUILog.add("39");
        return super.getMaximumSize();
    }

    public Dimension getMinimumSize() {
        InstrumentedUILog.add("40");
        return super.getMinimumSize();
    }

    public Component getNextFocusableComponent() {
        InstrumentedUILog.add(new Object[] { "getNextFocusableComponent" });
        return super.getNextFocusableComponent();
    }

    public Dimension getPreferredSize() {
        InstrumentedUILog.add("42");
        return super.getPreferredSize();
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        InstrumentedUILog.add("43");
        return super.getPropertyChangeListeners();
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(
            String arg0) {
        InstrumentedUILog.add("44");
        return super.getPropertyChangeListeners(arg0);
    }

    public KeyStroke[] getRegisteredKeyStrokes() {
        InstrumentedUILog.add("45");
        return super.getRegisteredKeyStrokes();
    }

    public JRootPane getRootPane() {
        InstrumentedUILog.add("46");
        return super.getRootPane();
    }

    public Dimension getSize(Dimension arg0) {
        InstrumentedUILog.add("47");
        return super.getSize(arg0);
    }

    public Point getToolTipLocation(MouseEvent arg0) {
        InstrumentedUILog.add("48");
        return super.getToolTipLocation(arg0);
    }

    public String getToolTipText() {
        InstrumentedUILog.add(new Object[] { "getToolTipText" });
        return super.getToolTipText();
    }

    public String getToolTipText(MouseEvent arg0) {
        InstrumentedUILog.add("50");
        return super.getToolTipText(arg0);
    }

    public Container getTopLevelAncestor() {
        InstrumentedUILog.add("51");
        return super.getTopLevelAncestor();
    }

    public TransferHandler getTransferHandler() {
        InstrumentedUILog.add("52");
        return super.getTransferHandler();
    }

    public String getUIClassID() {
        InstrumentedUILog.add(new Object[] { "getUIClassID" });
        return super.getUIClassID();
    }

    public boolean getVerifyInputWhenFocusTarget() {
        InstrumentedUILog.add(new Object[] { "getVerifyInputWhenFocusTarget" });
        return super.getVerifyInputWhenFocusTarget();
    }

    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        InstrumentedUILog.add("55");
        return super.getVetoableChangeListeners();
    }

    public Rectangle getVisibleRect() {
        InstrumentedUILog.add(new Object[] { "getVisibleRect" });
        return super.getVisibleRect();
    }

    public int getWidth() {
        InstrumentedUILog.add(new Object[] { "getWidth" });
        return super.getWidth();
    }

    public int getX() {
        InstrumentedUILog.add(new Object[] { "getX" });
        return super.getX();
    }

    public int getY() {
        InstrumentedUILog.add(new Object[] { "getY" });
        return super.getY();
    }

    public void grabFocus() {
        InstrumentedUILog.add("60");
        super.grabFocus();
    }

    public boolean isDoubleBuffered() {
        InstrumentedUILog.add(new Object[] { "isDoubleBuffered" });
        return super.isDoubleBuffered();
    }

    public boolean isManagingFocus() {
        InstrumentedUILog.add(new Object[] { "isManagingFocus" });
        return super.isManagingFocus();
    }

    public boolean isMaximumSizeSet() {
        InstrumentedUILog.add(new Object[] { "isMaximumSizeSet" });
        return super.isMaximumSizeSet();
    }

    public boolean isMinimumSizeSet() {
        InstrumentedUILog.add(new Object[] { "isMinimumSizeSet" });
        return super.isMinimumSizeSet();
    }

    public boolean isOpaque() {
        InstrumentedUILog.add("65");
        return super.isOpaque();
    }

    public boolean isOptimizedDrawingEnabled() {
        InstrumentedUILog.add("66");
        return super.isOptimizedDrawingEnabled();
    }

    public boolean isPaintingTile() {
        InstrumentedUILog.add("67");
        return super.isPaintingTile();
    }

    public boolean isPreferredSizeSet() {
        InstrumentedUILog.add(new Object[] { "isPreferredSizeSet" });
        return super.isPreferredSizeSet();
    }

    public boolean isRequestFocusEnabled() {
        InstrumentedUILog.add(new Object[] { "isRequestFocusEnabled" });
        return super.isRequestFocusEnabled();
    }

    public boolean isValidateRoot() {
        InstrumentedUILog.add(new Object[] { "isValidateRoot" });
        return super.isValidateRoot();
    }

    public void paint(Graphics arg0) {
        InstrumentedUILog.add("71");
        super.paint(arg0);
    }

    protected void paintBorder(Graphics arg0) {
        InstrumentedUILog.add("72");
        super.paintBorder(arg0);
    }

    protected void paintChildren(Graphics arg0) {
        InstrumentedUILog.add("73");
        super.paintChildren(arg0);
    }

    protected void paintComponent(Graphics arg0) {
        InstrumentedUILog.add("74");
        super.paintComponent(arg0);
    }

    public void paintImmediately(int arg0, int arg1, int arg2, int arg3) {
        InstrumentedUILog.add(new Object[] { "paintImmediately",
                "" + arg0 + " " + arg1 + " " + arg2 + " " + arg3 });
        super.paintImmediately(arg0, arg1, arg2, arg3);
    }

    public void paintImmediately(Rectangle arg0) {
        InstrumentedUILog.add("76");
        super.paintImmediately(arg0);
    }

    protected String paramString() {
        InstrumentedUILog.add(new Object[] { "paramString" });
        return super.paramString();
    }

    public void print(Graphics arg0) {
        InstrumentedUILog.add("78");
        super.print(arg0);
    }

    public void printAll(Graphics arg0) {
        InstrumentedUILog.add("79");
        super.printAll(arg0);
    }

    protected void printBorder(Graphics arg0) {
        InstrumentedUILog.add("80");
        super.printBorder(arg0);
    }

    protected void printChildren(Graphics arg0) {
        InstrumentedUILog.add("81");
        super.printChildren(arg0);
    }

    protected void printComponent(Graphics arg0) {
        InstrumentedUILog.add("82");
        super.printComponent(arg0);
    }

    protected void processComponentKeyEvent(KeyEvent arg0) {
        InstrumentedUILog.add("83");
        super.processComponentKeyEvent(arg0);
    }

    protected boolean processKeyBinding(KeyStroke arg0, KeyEvent arg1,
            int arg2, boolean arg3) {
        InstrumentedUILog.add("84");
        return super.processKeyBinding(arg0, arg1, arg2, arg3);
    }

    protected void processKeyEvent(KeyEvent arg0) {
        InstrumentedUILog.add("85");
        super.processKeyEvent(arg0);
    }

    protected void processMouseMotionEvent(MouseEvent arg0) {
        InstrumentedUILog.add("86");
        super.processMouseMotionEvent(arg0);
    }

    public void registerKeyboardAction(ActionListener arg0, KeyStroke arg1,
            int arg2) {
        InstrumentedUILog.add(new Object[] { "registerKeyboardAction", arg0,
                arg1, "" + arg2 });
        super.registerKeyboardAction(arg0, arg1, arg2);
    }

    public void registerKeyboardAction(ActionListener arg0, String arg1,
            KeyStroke arg2, int arg3) {
        InstrumentedUILog.add(new Object[] { "registerKeyboardAction", arg0,
                arg1, arg2, "" + arg3 });
        super.registerKeyboardAction(arg0, arg1, arg2, arg3);
    }

    public void removeAncestorListener(AncestorListener arg0) {
        InstrumentedUILog.add("89");
        super.removeAncestorListener(arg0);
    }

    public void removeNotify() {
        InstrumentedUILog.add(new Object[] { "removeNotify" });
        super.removeNotify();
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener arg0) {
        InstrumentedUILog.add("91");
        super.removePropertyChangeListener(arg0);
    }

    public synchronized void removePropertyChangeListener(String arg0,
            PropertyChangeListener arg1) {
        InstrumentedUILog.add("92");
        super.removePropertyChangeListener(arg0, arg1);
    }

    public synchronized void removeVetoableChangeListener(
            VetoableChangeListener arg0) {
        InstrumentedUILog.add("93");
        super.removeVetoableChangeListener(arg0);
    }

    public void repaint(long arg0, int arg1, int arg2, int arg3, int arg4) {
        InstrumentedUILog
                .add(new Object[] {
                        "repaint",
                        "" + arg0 + " " + arg1 + " " + arg2 + " " + arg3 + " "
                                + arg4 });
        super.repaint(arg0, arg1, arg2, arg3, arg4);
    }

    public void repaint(Rectangle arg0) {
        InstrumentedUILog.add("95");
        super.repaint(arg0);
    }

    public boolean requestDefaultFocus() {
        InstrumentedUILog.add("96");
        return super.requestDefaultFocus();
    }

    public void requestFocus() {
        InstrumentedUILog.add(new Object[] { "requestFocus" });
        super.requestFocus();
    }

    public boolean requestFocus(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "requestFocus", "" + arg0 });
        return super.requestFocus(arg0);
    }

    public boolean requestFocusInWindow() {
        InstrumentedUILog.add(new Object[] { "requestFocusInWindow" });
        return super.requestFocusInWindow();
    }

    protected boolean requestFocusInWindow(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "requestFocusInWindow", "" + arg0 });
        return super.requestFocusInWindow(arg0);
    }

    public void resetKeyboardActions() {
        InstrumentedUILog.add("101");
        super.resetKeyboardActions();
    }

    public void setLocation(int arg0, int arg1) {
        InstrumentedUILog.add(new Object[] { "setLocation",
                "" + arg0 + " " + arg1 });
        super.setLocation(arg0, arg1);
    }

    public void reshape(int arg0, int arg1, int arg2, int arg3) {
        InstrumentedUILog.add(new Object[] { "reshape",
                "" + arg0 + " " + arg1 + " " + arg2 + " " + arg3 });
        super.reshape(arg0, arg1, arg2, arg3);
    }

    public void revalidate() {
        InstrumentedUILog.add(new Object[] { "revalidate" });
        super.revalidate();
    }

    public void scrollRectToVisible(Rectangle arg0) {
        InstrumentedUILog.add("104");
        super.scrollRectToVisible(arg0);
    }

    public void setAlignmentX(float arg0) {
        InstrumentedUILog.add("105");
        super.setAlignmentX(arg0);
    }

    public void setAlignmentY(float arg0) {
        InstrumentedUILog.add(new Object[] { "setAlignmentY", "" + arg0 });
        super.setAlignmentY(arg0);
    }

    public void setAutoscrolls(boolean arg0) {
        InstrumentedUILog.add("107");
        super.setAutoscrolls(arg0);
    }

    public void setBackground(Color arg0) {
        InstrumentedUILog.add("108");
        super.setBackground(arg0);
    }

    public void setBorder(Border arg0) {
        InstrumentedUILog.add(new Object[] { "setBorder", arg0 });
        super.setBorder(arg0);
    }

    public void setDebugGraphicsOptions(int arg0) {
        InstrumentedUILog.add("110");
        super.setDebugGraphicsOptions(arg0);
    }

    public void setDoubleBuffered(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setDoubleBuffered", "" + arg0 });
        super.setDoubleBuffered(arg0);
    }

    public void setEnabled(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setEnabled", "" + arg0 });
        super.setEnabled(arg0);
    }

    public void setFont(Font arg0) {
        InstrumentedUILog.add("113");
        super.setFont(arg0);
    }

    public void setForeground(Color arg0) {
        InstrumentedUILog.add("114");
        super.setForeground(arg0);
    }

    public void setInputVerifier(InputVerifier arg0) {
        InstrumentedUILog.add(new Object[] { "setInputVerifier", arg0 });
        super.setInputVerifier(arg0);
    }

    public void setMaximumSize(Dimension arg0) {
        InstrumentedUILog.add("116");
        super.setMaximumSize(arg0);
    }

    public void setMinimumSize(Dimension arg0) {
        InstrumentedUILog.add("117");
        super.setMinimumSize(arg0);
    }

    public void setNextFocusableComponent(Component arg0) {
        InstrumentedUILog.add("118");
        super.setNextFocusableComponent(arg0);
    }

    public void setOpaque(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setOpaque", "" + arg0 });
        super.setOpaque(arg0);
    }

    public void setPreferredSize(Dimension arg0) {
        InstrumentedUILog.add("120");
        super.setPreferredSize(arg0);
    }

    public void setRequestFocusEnabled(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setRequestFocusEnabled",
                "" + arg0 });
        super.setRequestFocusEnabled(arg0);
    }

    public void setToolTipText(String arg0) {
        InstrumentedUILog.add(new Object[] { "setToolTipText", arg0 });
        super.setToolTipText(arg0);
    }

    public void setTransferHandler(TransferHandler arg0) {
        InstrumentedUILog.add("123");
        super.setTransferHandler(arg0);
    }

    public void setUI(ComponentUI arg0) {
        InstrumentedUILog.add(new Object[] { "setUI", arg0 });
        super.setUI(arg0);
    }

    public void setVerifyInputWhenFocusTarget(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setVerifyInputWhenFocusTarget",
                "" + arg0 });
        super.setVerifyInputWhenFocusTarget(arg0);
    }

    public void setVisible(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "setVisible", "" + arg0 });
        super.setVisible(arg0);
    }

    public void unregisterKeyboardAction(KeyStroke arg0) {
        InstrumentedUILog.add("127");
        super.unregisterKeyboardAction(arg0);
    }

    public void update(Graphics arg0) {
        InstrumentedUILog.add("128");
        super.update(arg0);
    }

    public void updateUI() {
        InstrumentedUILog.add("129");
        super.updateUI();
    }

    public int countComponents() {
        InstrumentedUILog.add(new Object[] { "awt.Container.countComponents" });
        return super.countComponents();
    }

    public int getComponentCount() {
        InstrumentedUILog.add(new Object[] { "awt.Container.getComponentCount" });
        return super.getComponentCount();
    }

    public void doLayout() {
        InstrumentedUILog.add("awt.Container 3");
        super.doLayout();
    }

    public void invalidate() {
        InstrumentedUILog.add(new Object[] { "awt.Container.invalidate" });
        super.invalidate();
    }

    public void layout() {
        InstrumentedUILog.add("awt.Container 5");
        super.layout();
    }

    public void removeAll() {
        InstrumentedUILog.add("awt.Container 6");
        super.removeAll();
    }

    public void transferFocusBackward() {
        InstrumentedUILog.add("awt.Container 7");
        super.transferFocusBackward();
    }

    public void transferFocusDownCycle() {
        InstrumentedUILog.add("awt.Container 8");
        super.transferFocusDownCycle();
    }

    public void validate() {
        InstrumentedUILog.add("awt.Container 9");
        super.validate();
    }

    protected void validateTree() {
        InstrumentedUILog.add("awt.Container 10");
        super.validateTree();
    }

    public boolean isFocusCycleRoot() {
        InstrumentedUILog.add("awt.Container 11");
        return super.isFocusCycleRoot();
    }

    public boolean isFocusTraversalPolicySet() {
        InstrumentedUILog.add("awt.Container 12");
        return super.isFocusTraversalPolicySet();
    }

    public void remove(int arg0) {
        InstrumentedUILog.add("awt.Container 13");
        super.remove(arg0);
    }

    public boolean areFocusTraversalKeysSet(int arg0) {
        InstrumentedUILog.add("awt.Container 14");
        return super.areFocusTraversalKeysSet(arg0);
    }

    public void setFocusCycleRoot(boolean arg0) {
        InstrumentedUILog.add("awt.Container 15");
        super.setFocusCycleRoot(arg0);
    }

    protected void processEvent(AWTEvent arg0) {
        InstrumentedUILog.add("awt.Container 16");
        super.processEvent(arg0);
    }

    public Component[] getComponents() {
        InstrumentedUILog.add("awt.Container 17");
        return super.getComponents();
    }

    public Component getComponent(int arg0) {
        InstrumentedUILog.add("awt.Container 18");
        return super.getComponent(arg0);
    }

    public Component findComponentAt(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 19");
        return super.findComponentAt(arg0, arg1);
    }

    public Component getComponentAt(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 20");
        return super.getComponentAt(arg0, arg1);
    }

    public Component locate(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 21");
        return super.locate(arg0, arg1);
    }

    public void remove(Component arg0) {
        InstrumentedUILog.add("awt.Container 22");
        super.remove(arg0);
    }

    public boolean isAncestorOf(Component arg0) {
        InstrumentedUILog.add("awt.Container 23");
        return super.isAncestorOf(arg0);
    }

    public void applyComponentOrientation(ComponentOrientation arg0) {
        InstrumentedUILog.add("awt.Container 24");
        super.applyComponentOrientation(arg0);
    }

    public boolean isFocusCycleRoot(Container arg0) {
        InstrumentedUILog.add("awt.Container 25");
        return super.isFocusCycleRoot(arg0);
    }

    public Dimension minimumSize() {
        InstrumentedUILog.add("awt.Container 26");
        return super.minimumSize();
    }

    public Dimension preferredSize() {
        InstrumentedUILog.add("awt.Container 27");
        return super.preferredSize();
    }

    public void deliverEvent(Event arg0) {
        InstrumentedUILog.add("awt.Container 28");
        super.deliverEvent(arg0);
    }

    public FocusTraversalPolicy getFocusTraversalPolicy() {
        InstrumentedUILog.add("awt.Container 29");
        return super.getFocusTraversalPolicy();
    }

    public void setFocusTraversalPolicy(FocusTraversalPolicy arg0) {
        InstrumentedUILog.add("awt.Container 30");
        super.setFocusTraversalPolicy(arg0);
    }

    public void paintComponents(Graphics arg0) {
        InstrumentedUILog.add("awt.Container 31");
        super.paintComponents(arg0);
    }

    public void printComponents(Graphics arg0) {
        InstrumentedUILog.add("awt.Container 32");
        super.printComponents(arg0);
    }

    public Insets insets() {
        InstrumentedUILog.add("awt.Container 33");
        return super.insets();
    }

    public LayoutManager getLayout() {
        InstrumentedUILog.add("awt.Container 34");
        return super.getLayout();
    }

    public void setLayout(LayoutManager arg0) {
        InstrumentedUILog.add("awt.Container 35");
        super.setLayout(arg0);
    }

    protected void processContainerEvent(ContainerEvent arg0) {
        InstrumentedUILog.add("awt.Container 36");
        super.processContainerEvent(arg0);
    }

    public synchronized ContainerListener[] getContainerListeners() {
        InstrumentedUILog.add(new Object[] {"awt.Container.getContainerListeners"});
        return super.getContainerListeners();
    }

    public synchronized void addContainerListener(ContainerListener arg0) {
        InstrumentedUILog.add("awt.Container 38");
        super.addContainerListener(arg0);
    }

    public synchronized void removeContainerListener(ContainerListener arg0) {
        InstrumentedUILog.add("awt.Container 39");
        super.removeContainerListener(arg0);
    }

    public void list(PrintStream arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 40");
        super.list(arg0, arg1);
    }

    public void list(PrintWriter arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 41");
        super.list(arg0, arg1);
    }

    public Set getFocusTraversalKeys(int arg0) {
        InstrumentedUILog.add("awt.Container 42");
        return super.getFocusTraversalKeys(arg0);
    }

    public void setFocusTraversalKeys(int arg0, Set arg1) {
        InstrumentedUILog.add("awt.Container 43");
        super.setFocusTraversalKeys(arg0, arg1);
    }

    public Component add(Component arg0) {
        InstrumentedUILog.add("awt.Container 44");
        return super.add(arg0);
    }

    public Component add(Component arg0, int arg1) {
        InstrumentedUILog.add("awt.Container 45");
        return super.add(arg0, arg1);
    }

    public Component findComponentAt(Point arg0) {
        InstrumentedUILog.add("awt.Container 46");
        return super.findComponentAt(arg0);
    }

    public Component getComponentAt(Point arg0) {
        InstrumentedUILog.add("awt.Container 47");
        return super.getComponentAt(arg0);
    }

    public void add(Component arg0, Object arg1) {
        InstrumentedUILog.add("awt.Container 48");
        super.add(arg0, arg1);
    }

    public void add(Component arg0, Object arg1, int arg2) {
        InstrumentedUILog.add("awt.Container 49");
        super.add(arg0, arg1, arg2);
    }

    protected void addImpl(Component arg0, Object arg1, int arg2) {
        InstrumentedUILog.add("awt.Container 50");
        super.addImpl(arg0, arg1, arg2);
    }

    public Component add(String arg0, Component arg1) {
        InstrumentedUILog.add("awt.Container 51");
        return super.add(arg0, arg1);
    }

    public boolean action(Event arg0, Object arg1) {
        InstrumentedUILog.add("awt.Component 1");
        return super.action(arg0, arg1);
    }

    public synchronized void add(PopupMenu arg0) {
        InstrumentedUILog.add("awt.Component 2");
        super.add(arg0);
    }

    public synchronized void addComponentListener(ComponentListener arg0) {
        InstrumentedUILog.add("awt.Component 3");
        super.addComponentListener(arg0);
    }

    public synchronized void addFocusListener(FocusListener arg0) {
        InstrumentedUILog.add(new Object[] { "awt.Component.addFocusListener",
                arg0 });
        super.addFocusListener(arg0);
    }

    public void addHierarchyBoundsListener(HierarchyBoundsListener arg0) {
        InstrumentedUILog.add("awt.Component 5");
        super.addHierarchyBoundsListener(arg0);
    }

    public void addHierarchyListener(HierarchyListener arg0) {
        InstrumentedUILog.add("awt.Component 6");
        super.addHierarchyListener(arg0);
    }

    public synchronized void addInputMethodListener(InputMethodListener arg0) {
        InstrumentedUILog.add("awt.Component 7");
        super.addInputMethodListener(arg0);
    }

    public synchronized void addKeyListener(KeyListener arg0) {
        InstrumentedUILog.add("awt.Component 8");
        super.addKeyListener(arg0);
    }

    public synchronized void addMouseListener(MouseListener arg0) {
        InstrumentedUILog.add(new Object[] { "awt.Component.addMouseListener",
                arg0 });
        super.addMouseListener(arg0);
    }

    public synchronized void addMouseMotionListener(MouseMotionListener arg0) {
        InstrumentedUILog.add(new Object[] {
                "awt.Component.addMouseMotionListener", arg0 });
        super.addMouseMotionListener(arg0);
    }

    public synchronized void addMouseWheelListener(MouseWheelListener arg0) {
        InstrumentedUILog.add("awt.Component 11");
        super.addMouseWheelListener(arg0);
    }

    public Rectangle bounds() {
        InstrumentedUILog.add(new Object[] {"awt.Component.bounds"});
        return super.bounds();
    }

    public int checkImage(Image arg0, ImageObserver arg1) {
        InstrumentedUILog.add("awt.Component 13");
        return super.checkImage(arg0, arg1);
    }

    public int checkImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
        InstrumentedUILog.add("awt.Component 14");
        return super.checkImage(arg0, arg1, arg2, arg3);
    }

    protected AWTEvent coalesceEvents(AWTEvent arg0, AWTEvent arg1) {
        InstrumentedUILog.add("awt.Component 15");
        return super.coalesceEvents(arg0, arg1);
    }

    public boolean contains(Point arg0) {
        InstrumentedUILog.add(new Object[] {"awt.Component.contains", arg0});
        return super.contains(arg0);
    }

    public Image createImage(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 17");
        return super.createImage(arg0, arg1);
    }

    public VolatileImage createVolatileImage(int arg0, int arg1,
            ImageCapabilities arg2) throws AWTException {
        InstrumentedUILog.add("awt.Component 18");
        return super.createVolatileImage(arg0, arg1, arg2);
    }

    public VolatileImage createVolatileImage(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 19");
        return super.createVolatileImage(arg0, arg1);
    }

    public void enable(boolean arg0) {
        InstrumentedUILog
                .add(new Object[] { "awt.Component.enable", "" + arg0 });
        super.enable(arg0);
    }

    public void enableInputMethods(boolean arg0) {
        InstrumentedUILog.add("awt.Component 21");
        super.enableInputMethods(arg0);
    }

    public Color getBackground() {
        InstrumentedUILog.add("awt.Component 22");
        return super.getBackground();
    }

    public Rectangle getBounds() {
        InstrumentedUILog.add(new Object[] {"awt.Component.getBounds"});
        return super.getBounds();
    }

    public ColorModel getColorModel() {
        InstrumentedUILog.add("awt.Component 24");
        return super.getColorModel();
    }

    public synchronized ComponentListener[] getComponentListeners() {
        InstrumentedUILog.add(new Object[] {"awt.Component.getComponentListeners"});
        return super.getComponentListeners();
    }

    public ComponentOrientation getComponentOrientation() {
        InstrumentedUILog.add("awt.Component 26");
        return super.getComponentOrientation();
    }

    public Cursor getCursor() {
        InstrumentedUILog.add("awt.Component 27");
        return super.getCursor();
    }

    public synchronized DropTarget getDropTarget() {
        InstrumentedUILog.add("awt.Component 28");
        return super.getDropTarget();
    }

    public Container getFocusCycleRootAncestor() {
        InstrumentedUILog.add("awt.Component 29");
        return super.getFocusCycleRootAncestor();
    }

    public synchronized FocusListener[] getFocusListeners() {
        InstrumentedUILog.add("awt.Component 30");
        return super.getFocusListeners();
    }

    public boolean getFocusTraversalKeysEnabled() {
        InstrumentedUILog.add("awt.Component 31");
        return super.getFocusTraversalKeysEnabled();
    }

    public Font getFont() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getFont" });
        return super.getFont();
    }

    public FontMetrics getFontMetrics(Font arg0) {
        InstrumentedUILog.add("awt.Component 33");
        return super.getFontMetrics(arg0);
    }

    public Color getForeground() {
        InstrumentedUILog.add("awt.Component 34");
        return super.getForeground();
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        InstrumentedUILog.add("awt.Component 35");
        return super.getGraphicsConfiguration();
    }

    public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners() {
        InstrumentedUILog.add("awt.Component 36");
        return super.getHierarchyBoundsListeners();
    }

    public synchronized HierarchyListener[] getHierarchyListeners() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getHierarchyListeners" });
        return super.getHierarchyListeners();
    }

    public boolean getIgnoreRepaint() {
        InstrumentedUILog.add("awt.Component 38");
        return super.getIgnoreRepaint();
    }

    public InputContext getInputContext() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getInputContext" });
        return super.getInputContext();
    }

    public synchronized InputMethodListener[] getInputMethodListeners() {
        InstrumentedUILog.add("awt.Component 40");
        return super.getInputMethodListeners();
    }

    public InputMethodRequests getInputMethodRequests() {
        InstrumentedUILog.add("awt.Component 41");
        return super.getInputMethodRequests();
    }

    public synchronized KeyListener[] getKeyListeners() {
        InstrumentedUILog.add("awt.Component 42");
        return super.getKeyListeners();
    }

    public Locale getLocale() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getLocale" });
        return super.getLocale();
    }

    public Point getLocation() {
        InstrumentedUILog.add("awt.Component 44");
        return super.getLocation();
    }

    public Point getLocationOnScreen() {
        InstrumentedUILog.add("awt.Component 45");
        return super.getLocationOnScreen();
    }

    public synchronized MouseListener[] getMouseListeners() {
        InstrumentedUILog.add("awt.Component 46");
        return super.getMouseListeners();
    }

    public synchronized MouseMotionListener[] getMouseMotionListeners() {
        InstrumentedUILog.add("awt.Component 47");
        return super.getMouseMotionListeners();
    }

    public synchronized MouseWheelListener[] getMouseWheelListeners() {
        InstrumentedUILog.add("awt.Component 48");
        return super.getMouseWheelListeners();
    }

    public String getName() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getName" });
        return super.getName();
    }

    public Container getParent() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getParent" });
        return super.getParent();
    }

    public ComponentPeer getPeer() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getPeer" });
        return super.getPeer();
    }

    public Dimension getSize() {
        InstrumentedUILog.add("awt.Component 52");
        return super.getSize();
    }

    public Toolkit getToolkit() {
        InstrumentedUILog.add(new Object[] { "awt.Component.getToolkit" });
        return super.getToolkit();
    }

    public boolean gotFocus(Event arg0, Object arg1) {
        InstrumentedUILog.add("awt.Component 54");
        return super.gotFocus(arg0, arg1);
    }

    public boolean handleEvent(Event arg0) {
        InstrumentedUILog.add("awt.Component 55");
        return super.handleEvent(arg0);
    }

    public boolean hasFocus() {
        InstrumentedUILog.add(new Object[] { "awt.Component.hasFocus" });
        return super.hasFocus();
    }

    public void hide() {
        InstrumentedUILog.add(new Object[] { "awt.Component.hide" });
        super.hide();
    }

    public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
            int arg4, int arg5) {
        InstrumentedUILog.add("awt.Component 58");
        return super.imageUpdate(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public boolean inside(int arg0, int arg1) {
        InstrumentedUILog.add(new Object[] { "awt.Component.inside",
                "" + arg0 + " " + arg1 });
        return super.inside(arg0, arg1);
    }

    public boolean isBackgroundSet() {
        InstrumentedUILog.add("awt.Component 60");
        return super.isBackgroundSet();
    }

    public boolean isCursorSet() {
        InstrumentedUILog.add("awt.Component 61");
        return super.isCursorSet();
    }

    public boolean isDisplayable() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isDisplayable" });
        return super.isDisplayable();
    }

    public boolean isFocusable() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isFocusable" });
        return super.isFocusable();
    }

    public boolean isFocusOwner() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isFocusOwner" });
        return super.isFocusOwner();
    }

    public boolean isFocusTraversable() {
        InstrumentedUILog
                .add(new Object[] { "awt.Component.isFocusTraversable" });
        return super.isFocusTraversable();
    }

    public boolean isFontSet() {
        InstrumentedUILog.add("awt.Component 66");
        return super.isFontSet();
    }

    public boolean isForegroundSet() {
        InstrumentedUILog.add("awt.Component 67");
        return super.isForegroundSet();
    }

    public boolean isLightweight() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isLightweight" });
        return super.isLightweight();
    }

    public boolean isShowing() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isShowing" });
        return super.isShowing();
    }

    public boolean isValid() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isValid" });
        return super.isValid();
    }

    public boolean isVisible() {
        InstrumentedUILog.add(new Object[] { "awt.Component.isVisible" });
        return super.isVisible();
    }

    public boolean keyDown(Event arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 72");
        return super.keyDown(arg0, arg1);
    }

    public boolean keyUp(Event arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 73");
        return super.keyUp(arg0, arg1);
    }

    public void list() {
        InstrumentedUILog.add("awt.Component 74");
        super.list();
    }

    public void list(PrintStream arg0) {
        InstrumentedUILog.add("awt.Component 75");
        super.list(arg0);
    }

    public void list(PrintWriter arg0) {
        InstrumentedUILog.add("awt.Component 76");
        super.list(arg0);
    }

    public Point location() {
        InstrumentedUILog.add("awt.Component 77");
        return super.location();
    }

    public boolean lostFocus(Event arg0, Object arg1) {
        InstrumentedUILog.add("awt.Component 78");
        return super.lostFocus(arg0, arg1);
    }

    public boolean mouseDown(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 79");
        return super.mouseDown(arg0, arg1, arg2);
    }

    public boolean mouseDrag(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 80");
        return super.mouseDrag(arg0, arg1, arg2);
    }

    public boolean mouseEnter(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 81");
        return super.mouseEnter(arg0, arg1, arg2);
    }

    public boolean mouseExit(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 82");
        return super.mouseExit(arg0, arg1, arg2);
    }

    public boolean mouseMove(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 83");
        return super.mouseMove(arg0, arg1, arg2);
    }

    public boolean mouseUp(Event arg0, int arg1, int arg2) {
        InstrumentedUILog.add("awt.Component 84");
        return super.mouseUp(arg0, arg1, arg2);
    }

    public void move(int arg0, int arg1) {
        InstrumentedUILog.add(new Object[] { "awt.Component.move",
                "" + arg0 + " " + arg1 });
        super.move(arg0, arg1);
    }

    public void nextFocus() {
        InstrumentedUILog.add("awt.Component 86");
        super.nextFocus();
    }

    public void paintAll(Graphics arg0) {
        InstrumentedUILog.add("awt.Component 87");
        super.paintAll(arg0);
    }

    public boolean postEvent(Event arg0) {
        InstrumentedUILog.add("awt.Component 88");
        return super.postEvent(arg0);
    }

    public boolean prepareImage(Image arg0, ImageObserver arg1) {
        InstrumentedUILog.add("awt.Component 89");
        return super.prepareImage(arg0, arg1);
    }

    public boolean prepareImage(Image arg0, int arg1, int arg2,
            ImageObserver arg3) {
        InstrumentedUILog.add("awt.Component 90");
        return super.prepareImage(arg0, arg1, arg2, arg3);
    }

    protected void processComponentEvent(ComponentEvent arg0) {
        InstrumentedUILog.add(new Object[] {"awt.Component.processComponentEvent", arg0});
        super.processComponentEvent(arg0);
    }

    protected void processFocusEvent(FocusEvent arg0) {
        InstrumentedUILog.add("awt.Component 92");
        super.processFocusEvent(arg0);
    }

    protected void processHierarchyBoundsEvent(HierarchyEvent arg0) {
        InstrumentedUILog.add("awt.Component 93");
        super.processHierarchyBoundsEvent(arg0);
    }

    protected void processHierarchyEvent(HierarchyEvent arg0) {
        InstrumentedUILog.add("awt.Component 94");
        super.processHierarchyEvent(arg0);
    }

    protected void processInputMethodEvent(InputMethodEvent arg0) {
        InstrumentedUILog.add("awt.Component 95");
        super.processInputMethodEvent(arg0);
    }

    protected void processMouseEvent(MouseEvent arg0) {
        InstrumentedUILog.add("awt.Component 96");
        super.processMouseEvent(arg0);
    }

    protected void processMouseWheelEvent(MouseWheelEvent arg0) {
        InstrumentedUILog.add("awt.Component 97");
        super.processMouseWheelEvent(arg0);
    }

    public synchronized void remove(MenuComponent arg0) {
        InstrumentedUILog.add("awt.Component 98");
        super.remove(arg0);
    }

    public synchronized void removeComponentListener(ComponentListener arg0) {
        InstrumentedUILog.add("awt.Component 99");
        super.removeComponentListener(arg0);
    }

    public synchronized void removeFocusListener(FocusListener arg0) {
        InstrumentedUILog.add("awt.Component 100");
        super.removeFocusListener(arg0);
    }

    public void removeHierarchyBoundsListener(HierarchyBoundsListener arg0) {
        InstrumentedUILog.add("awt.Component 101");
        super.removeHierarchyBoundsListener(arg0);
    }

    public void removeHierarchyListener(HierarchyListener arg0) {
        InstrumentedUILog.add("awt.Component 102");
        super.removeHierarchyListener(arg0);
    }

    public synchronized void removeInputMethodListener(InputMethodListener arg0) {
        InstrumentedUILog.add("awt.Component 103");
        super.removeInputMethodListener(arg0);
    }

    public synchronized void removeKeyListener(KeyListener arg0) {
        InstrumentedUILog.add("awt.Component 104");
        super.removeKeyListener(arg0);
    }

    public synchronized void removeMouseListener(MouseListener arg0) {
        InstrumentedUILog.add(new Object[] {
                "awt.Component.removeMouseListener", arg0 });
        super.removeMouseListener(arg0);
    }

    public synchronized void removeMouseMotionListener(MouseMotionListener arg0) {
        InstrumentedUILog.add(new Object[] {
                "awt.Component.removeMouseMotionListener", arg0 });
        super.removeMouseMotionListener(arg0);
    }

    public synchronized void removeMouseWheelListener(MouseWheelListener arg0) {
        InstrumentedUILog.add("awt.Component 107");
        super.removeMouseWheelListener(arg0);
    }

    public void repaint() {
        InstrumentedUILog.add(new Object[] { "awt.Component.repaint" });
        super.repaint();
    }

    public void repaint(int arg0, int arg1, int arg2, int arg3) {
        InstrumentedUILog.add("awt.Component 109");
        super.repaint(arg0, arg1, arg2, arg3);
    }

    public void repaint(long arg0) {
        InstrumentedUILog.add("awt.Component 110");
        super.repaint(arg0);
    }

    public void resize(Dimension arg0) {
        InstrumentedUILog.add("awt.Component 111");
        super.resize(arg0);
    }

    public void resize(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 112");
        super.resize(arg0, arg1);
    }

    public void setBounds(int arg0, int arg1, int arg2, int arg3) {
        InstrumentedUILog.add(new Object[] { "awt.Component.setBounds",
                "" + arg0 + " " + arg1 + " " + arg2 + " " + arg3 });
        super.setBounds(arg0, arg1, arg2, arg3);
    }

    public void setBounds(Rectangle arg0) {
        InstrumentedUILog.add("awt.Component 114");
        super.setBounds(arg0);
    }

    public void setComponentOrientation(ComponentOrientation arg0) {
        InstrumentedUILog.add("awt.Component 115");
        super.setComponentOrientation(arg0);
    }

    public void setCursor(Cursor arg0) {
        InstrumentedUILog.add("awt.Component 116");
        super.setCursor(arg0);
    }

    public synchronized void setDropTarget(DropTarget arg0) {
        InstrumentedUILog.add("awt.Component 117");
        super.setDropTarget(arg0);
    }

    public void setFocusable(boolean arg0) {
        InstrumentedUILog.add("awt.Component 118");
        super.setFocusable(arg0);
    }

    public void setFocusTraversalKeysEnabled(boolean arg0) {
        InstrumentedUILog.add("awt.Component 119");
        super.setFocusTraversalKeysEnabled(arg0);
    }

    public void setIgnoreRepaint(boolean arg0) {
        InstrumentedUILog.add("awt.Component 120");
        super.setIgnoreRepaint(arg0);
    }

    public void setLocale(Locale arg0) {
        InstrumentedUILog.add(new Object[] { "awt.Component.setLocale", arg0 });
        super.setLocale(arg0);
    }

    public void setLocation(Point arg0) {
        InstrumentedUILog.add("awt.Component 122");
        super.setLocation(arg0);
    }

    public void setName(String arg0) {
        InstrumentedUILog.add("awt.Component 123");
        super.setName(arg0);
    }

    public void setSize(Dimension arg0) {
        InstrumentedUILog.add("awt.Component 124");
        super.setSize(arg0);
    }

    public void setSize(int arg0, int arg1) {
        InstrumentedUILog.add("awt.Component 125");
        super.setSize(arg0, arg1);
    }

    public void show() {
        InstrumentedUILog.add(new Object[] {"awt.Component.show"});
        super.show();
    }

    public void show(boolean arg0) {
        InstrumentedUILog.add(new Object[] { "awt.Component.show", "" + arg0 });
        super.show(arg0);
    }

    public Dimension size() {
        InstrumentedUILog.add("awt.Component 128");
        return super.size();
    }

    public String toString() {
        InstrumentedUILog.add(new Object[] { "awt.Component.toString" });
        return super.toString();
    }

    public void transferFocus() {
        InstrumentedUILog.add("awt.Component 130");
        super.transferFocus();
    }

    public void transferFocusUpCycle() {
        InstrumentedUILog.add("awt.Component 131");
        super.transferFocusUpCycle();
    }

    protected Object clone() throws CloneNotSupportedException {
        InstrumentedUILog.add("lang.object clone");
        return super.clone();
    }

    public boolean equals(Object arg0) {
        InstrumentedUILog.add("lang.object equals");
        return super.equals(arg0);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int hashCode() {
        return super.hashCode();
    }
}