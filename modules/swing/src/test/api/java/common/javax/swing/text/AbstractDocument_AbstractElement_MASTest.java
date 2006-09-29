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
 * @author Alexey A. Ivanov
 * @version $Revision$
 */
package javax.swing.text;

import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.AbstractDocument_AbstractElementTest.DisAbstractedDocument;
import javax.swing.text.AbstractDocument_AbstractElementTest.DisAbstractedDocument.DisAbstractedElement;

/**
 * Tests AbstractDocument.AbstractElement class - the part which implements
 * MutableAttributeSet interface. The document is write-lock during
 * execution of test-methods.
 *
 */
public class AbstractDocument_AbstractElement_MASTest
    extends MutableAttributeSetTest {

    protected DisAbstractedDocument aDocument;
    protected DisAbstractedElement  aElement;
    protected AbstractElement       parented;
    protected AttributeSet          aSet;

    public AbstractDocument_AbstractElement_MASTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        aDocument = new DisAbstractedDocument(new GapContent());
        aDocument.writeLock();
        aElement  = aDocument.new DisAbstractedElement(null, mas);
        mas = aElement;
        as  = aElement;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        aDocument.writeUnlock();
    }

}