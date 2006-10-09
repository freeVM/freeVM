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
 * @version $Revision: 1.4 $ 
 */ 

package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;

/*
 * Table values are obtained from RFC2911: Internet Printing Protocol/1.1: 
 * Model and Semantics, Appendix C, http://ietf.org/rfc/rfc2911.txt?number=2911
 */

public class MediaTray extends Media implements Attribute {


    public static final MediaTray TOP = new MediaTray(0);

    public static final MediaTray MIDDLE = new MediaTray(1);

    public static final MediaTray BOTTOM = new MediaTray(2);

    public static final MediaTray ENVELOPE = new MediaTray(3);

    public static final MediaTray MANUAL = new MediaTray(4);

    public static final MediaTray LARGE_CAPACITY = new MediaTray(5);

    public static final MediaTray MAIN = new MediaTray(6);

    public static final MediaTray SIDE = new MediaTray(7);
    

    private static final MediaTray[] enumValueTable = { TOP,
                                                        MIDDLE,
                                                        BOTTOM,
                                                        ENVELOPE,
                                                        MANUAL,
                                                        LARGE_CAPACITY,
                                                        MAIN,
                                                        SIDE };
    private static final String[] stringTable ={ "top",
                                                 "middle",
                                                 "bottom",
                                                 "envelope",
                                                 "manual",
                                                 "large-capacity",
                                                 "main",
                                                 "side" };
    
    
    protected MediaTray(int value) {
        super(value);
    }
    

    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[]) enumValueTable.clone();
    }
    
    protected String[] getStringTable() {
        return (String[]) stringTable.clone();
    }

    
}
