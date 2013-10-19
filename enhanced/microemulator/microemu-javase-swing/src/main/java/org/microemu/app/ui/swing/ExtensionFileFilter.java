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

import java.io.File;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;


public class ExtensionFileFilter extends FileFilter 
{
  
  String description;
  
  Hashtable extensions = new Hashtable(); 
  

  public ExtensionFileFilter(String description)
  {
    this.description = description;
  }
  
  
  public boolean accept(File file) 
  {
    if(file != null) {
	    if(file.isDirectory()) {
        return true;
	    }
      String ext = getExtension(file);
      if(ext != null && extensions.get(ext) != null) {
        return true;
      }
    }
    
  	return false;
  }
  
  
  public void addExtension(String extension)
  {
    extensions.put(extension.toLowerCase(), this);
  }

  
  public String getDescription() 
  {
    return description;
  }

  
  String getExtension(File file) 
  {
    if (file != null) {
	    String filename = file.getName();
	    int i = filename.lastIndexOf('.');
	    if (i > 0 && i < filename.length() - 1) {
        return filename.substring(i + 1).toLowerCase();
	    }
    }
  
    return null;
  }
  
}
