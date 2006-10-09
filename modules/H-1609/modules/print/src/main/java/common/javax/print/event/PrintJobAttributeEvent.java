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
 * @author Aleksei V. Ivaschenko 
 * @version $Revision: 1.3 $ 
 */ 

package javax.print.event;

import javax.print.DocPrintJob;
import javax.print.attribute.PrintJobAttributeSet;

public class PrintJobAttributeEvent extends PrintEvent {

    private PrintJobAttributeSet attributes;

    public PrintJobAttributeEvent(DocPrintJob source,
            PrintJobAttributeSet attributes) {
        super(source);
        this.attributes = attributes;
    }

    public PrintJobAttributeSet getAttributes() {
        return attributes;
    }

    public DocPrintJob getPrintJob() {
        return (DocPrintJob)getSource();
    }
}
