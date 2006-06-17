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
 * @author Evgeniya G. Maenkova
 * @version $Revision$
 */
package org.apache.harmony.awt.text;

import java.awt.Component;
import javax.swing.text.Document;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public interface RootViewContext {
    View getView();

    void setDocument(final Document doc);

    void setComponent(final Component comp);

    void setViewFactoryGetter(final ViewFactoryGetter getter);

    interface ViewFactoryGetter {
         ViewFactory getViewFactory();
    }
}
