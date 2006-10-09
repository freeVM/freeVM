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
 * @author Igor A. Pyankov 
 * @version $Revision: 1.2 $ 
 */ 

package org.apache.harmony.x.print.ipp.util;

import java.awt.GridLayout;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class IppHttpAuthenticator extends Authenticator {
    protected PasswordAuthentication getPasswordAuthentication() {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(2, 2));
        
        panel.add(new JLabel("username"));
        panel.add(username);
        panel.add(new JLabel("password"));
        panel.add(password);
        
        int option = JOptionPane.showConfirmDialog(null,
            new Object[] { "Site: " + getRequestingHost(),
                    "Realm: " + getRequestingPrompt(),
                    panel },
            "Enter Username and Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String user = username.getText();
            char   pass[] = password.getText().toCharArray();
            
            return new PasswordAuthentication(user, pass);
        }
        return null;
    }

}