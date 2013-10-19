/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.ui.swing;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Bazowa klasa panelu wyswietlanego w oknie dialogowym
 */

public class SwingDialogPanel extends JPanel
{

  public JButton btOk = new JButton("OK");
  public JButton btCancel = new JButton("Cancel");

  boolean state;
  
  boolean extra;

  /**
   * Walidacja panelu
   *
   * @param state czy wyswietlac komunikaty bledow
   * @return true jesli wszysko jest ok
   */
  public boolean check(boolean state)
  {
    return true;
  }


  protected void hideNotify()
  {
  }

  
  protected void showNotify()
  {
  }
  
  protected JButton getExtraButton()
  {
	return null;  
  }
  
  public boolean isExtraButtonPressed() {
	  return extra;
  }

}