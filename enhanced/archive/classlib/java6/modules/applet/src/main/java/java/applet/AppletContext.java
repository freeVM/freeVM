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
 * @author Pavel Dolgov
 * @version $Revision: 1.3 $
 */  
package java.applet;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

public interface AppletContext {

    Applet getApplet(String name);

    Enumeration<Applet> getApplets();

    AudioClip getAudioClip(URL url);

    Image getImage(URL url);

    InputStream getStream(String key);

    Iterator<String> getStreamKeys();

    void setStream(String key, InputStream stream) throws IOException;

    void showDocument(URL url, String target);

    void showDocument(URL url);

    void showStatus(String status);

}