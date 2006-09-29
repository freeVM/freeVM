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
 * @author Evgeniya G. Maenkova
 * @version $Revision$
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import javax.swing.text.html.HTMLEditorKit;

public class ParserDelegator extends HTMLEditorKit.Parser implements Serializable {



    public ParserDelegator() {
        throw new UnsupportedOperationException("Not implemented");
    }


    public void parse(final Reader a0, final HTMLEditorKit.ParserCallback a1, final boolean a2) throws IOException {
        throw new UnsupportedOperationException("Not implemented");

    }


    protected static DTD createDTD(final DTD a0, final String a1) {
        throw new UnsupportedOperationException("Not implemented");

    }


    protected static synchronized void setDefaultDTD() {
        throw new UnsupportedOperationException("Not implemented");

    }

}

