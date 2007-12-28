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

package org.apache.harmony.tools.appletviewer;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class AppletFrame extends JFrame {
    private final AppletInfo appletInfo;
    private final Applet applet;
    private final JLabel statusLabel;
    
    private static ShutdownHandler shutdownHandler = new ShutdownHandler();
    
    public AppletFrame(AppletInfo appletInfo) throws Exception {
        this.appletInfo = appletInfo;
        shutdownHandler.addFrame(this);
        
        // Load applet class
        URL []urls = new URL[1];
        urls[0] = appletInfo.getCodeBase();
        URLClassLoader cl = new URLClassLoader(urls);
        Class clz = cl.loadClass(this.appletInfo.getCode());
        applet = (Applet)clz.newInstance();
        applet.setStub(new ViewerAppletStub(applet, appletInfo));
        
        // Create applet pane
        setLayout(new BorderLayout());
        JPanel appletPanel = new JPanel();
        appletPanel.add(applet);
        add(appletPanel, BorderLayout.CENTER);
        applet.setPreferredSize(new Dimension(appletInfo.getWidth(), appletInfo.getHeight()));
        
        // Create status pane
        JPanel panel = new JPanel();
        statusLabel = new JLabel();
        panel.add(statusLabel);
        add(panel, BorderLayout.SOUTH);
        appletInfo.setStatusLabel(statusLabel);
        
        statusLabel.setMinimumSize(new Dimension(100, 40));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Start applet and make frame visible
        // Init should be called after pack to make components displayable
        pack();
        applet.init();
        setVisible(true);       
        applet.start();
    }       
    
    private  static class ShutdownHandler implements WindowListener {
        HashSet<JFrame> frameList = new HashSet<JFrame>();

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            frameList.remove(e.getWindow());
            if (frameList.isEmpty())
                System.exit(0);
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }
        
        public void addFrame(JFrame frame) {
            frameList.add(frame);
            frame.addWindowListener(this);
        }
    }
}
