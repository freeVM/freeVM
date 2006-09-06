/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.plaf.ColorChooserUI;

public class JColorChooser extends JComponent implements Accessible {
    protected class AccessibleJColorChooser extends AccessibleJComponent {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COLOR_CHOOSER;
        }
    }
    
    public static final String SELECTION_MODEL_PROPERTY = "selectionModel";
    public static final String PREVIEW_PANEL_PROPERTY = "previewPanel";
    public static final String CHOOSER_PANELS_PROPERTY = "chooserPanels";
    
    private ColorSelectionModel colorSelectionModel;
    private boolean dragEnabled;
    private JComponent previewPanel;
    private AbstractColorChooserPanel[] chooserPanels = new AbstractColorChooserPanel[] {};
    private static Color resultColor;

    public JColorChooser() {
        this(new DefaultColorSelectionModel(Color.WHITE));
    }
    
    public JColorChooser(final Color initialColor) {
        this(new DefaultColorSelectionModel(initialColor));
    }
    
    public JColorChooser(final ColorSelectionModel model) {
        colorSelectionModel = model;
        setLayout(new BorderLayout());
        
        updateUI();
    }
    
    public static Color showDialog(final Component component, final String title, 
                                   final Color initialColor) throws HeadlessException {
        
        final JColorChooser colorChooser = new JColorChooser(initialColor);
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultColor = colorChooser.getColor();
            }
        };
        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultColor = null;
            }
        };
        createDialog(component, title, true, colorChooser, okListener, cancelListener).show();
        
        return resultColor;
    }
    
    public static JDialog createDialog(final Component c, final String title,
                                       final boolean modal, final JColorChooser chooserPane,
                                       final ActionListener okListener,
                                       final ActionListener cancelListener) throws HeadlessException {
        
        Window ancestingWindow = c instanceof Window ? (Window)c : SwingUtilities.getWindowAncestor(c);
        final JDialog result;
        if (ancestingWindow instanceof Frame) {
            result = new JDialog((Frame)ancestingWindow);
        } else if (ancestingWindow instanceof Dialog) {
            result = new JDialog((Dialog)ancestingWindow);
        } else {
            result = new JDialog();
        }
        result.setModal(modal);
        result.setLocationRelativeTo(c);
        result.setTitle(title);
        result.getContentPane().add(chooserPane);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            result.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
        }
        JPanel buttonsPanel = new JPanel();
        
        ActionListener disposeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                result.dispose();
            }
        };
        
        String okText = UIManager.getString("ColorChooser.okText");
        final JButton okButton = new JButton(okText);
        okButton.addActionListener(okListener);
        
        String cancelText = UIManager.getString("ColorChooser.cancelText");
        final JButton cancelButton = new JButton(cancelText);
        cancelButton.addActionListener(cancelListener);
        
        String resetText = UIManager.getString("ColorChooser.resetText");
        JButton resetButton = new JButton(resetText);
        int resetMnemonic = UIManager.getInt("ColorChooser.resetMnemonic");
        resetButton.setMnemonic(resetMnemonic);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooserPane.setColor(Color.WHITE);
            }
        });
        
        okButton.addActionListener(disposeListener);
        cancelButton.addActionListener(disposeListener);
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(okButton);
        buttonsPanel.add(Box.createHorizontalStrut(6));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(6));
        buttonsPanel.add(resetButton);
        
        result.add(buttonsPanel, BorderLayout.SOUTH);
        result.getRootPane().setDefaultButton(okButton);

        result.pack();

        InputMap map = LookAndFeel.makeComponentInputMap(chooserPane, new Object[] { "ESCAPE", "cancelAction" });
        SwingUtilities.replaceUIInputMap(chooserPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, map);
        chooserPane.getActionMap().put("cancelAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick(0);
            }
        });
        
        return result;
    }
    
    public ColorChooserUI getUI() {
        return (ColorChooserUI)ui;
    }
    
    public void setUI(final ColorChooserUI ui) {
        super.setUI(ui);
    }
    
    public void updateUI() {
        setUI((ColorChooserUI)UIManager.getUI(this));
    }
    
    public String getUIClassID() {
        return "ColorChooserUI";
    }
    
    public Color getColor() {
        return colorSelectionModel.getSelectedColor();
    }
    
    public void setColor(final Color color) {
        colorSelectionModel.setSelectedColor(color);
    }
    
    public void setColor(final int r, final int g, final int b) {
        setColor(new Color(r, g, b));
    }
    
    public void setColor(final int c) {
        setColor(new Color(c));
    }
    
    public void setDragEnabled(final boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        dragEnabled = b;
    }
    
    public boolean getDragEnabled() {
        return dragEnabled;
    }
    
    public void setPreviewPanel(final JComponent preview) {
        JComponent oldValue = previewPanel;
        if (preview == null) {
            previewPanel = ColorChooserComponentFactory.getPreviewPanel();
        } else {
            previewPanel = preview;
        }
        
        firePropertyChange(PREVIEW_PANEL_PROPERTY, oldValue, previewPanel);
    }
    
    public JComponent getPreviewPanel() {
        return previewPanel;
    }
    
    public void addChooserPanel(final AbstractColorChooserPanel panel) {
        AbstractColorChooserPanel[] newChooserPanels = new AbstractColorChooserPanel[chooserPanels.length + 1];
        System.arraycopy(chooserPanels, 0, newChooserPanels, 0, chooserPanels.length);
        newChooserPanels[chooserPanels.length] = panel;
        setChooserPanels(newChooserPanels);
    }
    
    public AbstractColorChooserPanel removeChooserPanel(final AbstractColorChooserPanel panel) {
        AbstractColorChooserPanel panelToRemove = null;
        int index = 0;
        for (int i = 0; i < chooserPanels.length; i++) {
            if (panel.equals(chooserPanels[i])) {
                panelToRemove = chooserPanels[i];
                index = i;
                break;
            }
        }
        
        if (panelToRemove == null) {
            throw new IllegalArgumentException("panel to remove can not be found");
        }
        
        AbstractColorChooserPanel[] newChooserPanels = new AbstractColorChooserPanel[chooserPanels.length - 1];
        System.arraycopy(chooserPanels, 0, newChooserPanels, 0, index);
        System.arraycopy(chooserPanels, index + 1, newChooserPanels, index, newChooserPanels.length - index);
        setChooserPanels(newChooserPanels);
        
        return panelToRemove;
    }
    
    public void setChooserPanels(final AbstractColorChooserPanel[] panels) {
        AbstractColorChooserPanel[] oldValue = chooserPanels;
        chooserPanels = panels;
        
        firePropertyChange(CHOOSER_PANELS_PROPERTY, oldValue, chooserPanels);
    }
    
    public AbstractColorChooserPanel[] getChooserPanels() {
        return chooserPanels;
    }
    
    public ColorSelectionModel getSelectionModel() {
        return colorSelectionModel;
    }
    
    public void setSelectionModel(final ColorSelectionModel newModel) {
        ColorSelectionModel oldModel = colorSelectionModel;
        colorSelectionModel = newModel;
        firePropertyChange(SELECTION_MODEL_PROPERTY, oldModel, newModel);
    }
    
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJColorChooser();
        }
        
        return accessibleContext;
    }
}
