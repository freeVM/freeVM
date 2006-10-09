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
 * @author Elena V. Sayapina 
 * @version $Revision: 1.5 $ 
 */ 

package javax.print.attribute.standard;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

/*
 * Table values are obtained from PWG 5100.3:Production Printing Attributes
 * Set1, section 3.17, ftp://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf
 */

public final class PresentationDirection extends EnumSyntax
    implements PrintJobAttribute, PrintRequestAttribute {

    public static final PresentationDirection
        TOBOTTOM_TORIGHT = new PresentationDirection(0);

    public static final PresentationDirection
        TOBOTTOM_TOLEFT = new PresentationDirection(1);

    public static final PresentationDirection
        TOTOP_TORIGHT = new PresentationDirection(2);

    public static final PresentationDirection
        TOTOP_TOLEFT = new PresentationDirection(3);

    public static final PresentationDirection
        TORIGHT_TOBOTTOM = new PresentationDirection(4);

    public static final PresentationDirection
        TORIGHT_TOTOP = new PresentationDirection(5);

    public static final PresentationDirection
        TOLEFT_TOBOTTOM = new PresentationDirection(6);

    public static final PresentationDirection
        TOLEFT_TOTOP = new PresentationDirection(7);


    private static final PresentationDirection[] enumValueTable = {

            TOBOTTOM_TORIGHT,
            TOBOTTOM_TOLEFT,
            TOTOP_TORIGHT,
            TOTOP_TOLEFT,
            TORIGHT_TOBOTTOM,
            TORIGHT_TOTOP,
            TOLEFT_TOBOTTOM,
            TOLEFT_TOTOP
    };

    private static final String[] stringTable = { "tobottom-toright",
                                                  "tobottom-toleft",
                                                  "totop-toright",
                                                  "totop-toleft",
                                                  "toright-tobottom",
                                                  "toright-totop",
                                                  "toleft-tobottom",
                                                  "toleft-totop" };


    PresentationDirection(int value) {
        super(value);
    }


    public final Class getCategory() {
    /* 1.5 support requires the following changes
       Class<? extends Attribute> getCategory() { */
        return PresentationDirection.class;
    }

    protected EnumSyntax[]  getEnumValueTable() {
        return enumValueTable;
    }

    public final String getName() {
        return "presentation-direction";
    }

    protected String[] getStringTable() {
        return stringTable;
    }


}
