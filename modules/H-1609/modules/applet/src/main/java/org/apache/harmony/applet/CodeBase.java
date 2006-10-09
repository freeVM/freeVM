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
 * @author Pavel Dolgov
 * @version $Revision: 1.2 $
 */
package org.apache.harmony.applet;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.IdentityHashMap;

import org.apache.harmony.awt.ContextThreadGroup;


/**
 * Common context for all applets loaded from the same code base
 */
final class CodeBase {
    
    final URL codeBase;
    final URLClassLoader classLoader;
    final ContextThreadGroup threadGroup;
    final Factory factory;

    // Document -> DocumentSlice
    private final IdentityHashMap docSlices = new IdentityHashMap();
    
    CodeBase(URL url, Factory factory) {
        this.codeBase = url;
        this.factory = factory;
        classLoader = new URLClassLoader(new URL[]{ url });
        threadGroup = new ContextThreadGroup("Context-" + url.toString());
        threadGroup.setMaxPriority(4);
    }
    
    DocumentSlice getDocumentSlice(Document doc) {
        synchronized(docSlices) {
            DocumentSlice slice = (DocumentSlice)docSlices.get(doc);
            if (slice == null) {
                slice = new DocumentSlice(doc, this);
                docSlices.put(doc, slice);
            }
            return slice;
        }
    }
    
    void remove(DocumentSlice slice) {
        boolean empty = false;
        
        synchronized(docSlices) {
            docSlices.remove(slice.document);
            empty = (docSlices.size() == 0);
        }
        
        if (empty) {
            threadGroup.dispose();
            factory.remove(this);
        }
    }
}
