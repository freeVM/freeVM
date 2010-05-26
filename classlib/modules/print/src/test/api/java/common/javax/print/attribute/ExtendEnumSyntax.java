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
 * @author Elena V. Sayapina 
 */ 

package javax.print.attribute;

import java.io.ObjectStreamException;
import tests.support.Support_Excludes;

/*
 * Auxiliary class for EnumSyntax unit test
 */
public final class ExtendEnumSyntax extends EnumSyntax {

    public ExtendEnumSyntax(int value) {
        super(value);
    }

    public static final ExtendEnumSyntax ENUM1 = new ExtendEnumSyntax(0);
    public static final ExtendEnumSyntax ENUM2 = new ExtendEnumSyntax(1);


    public final EnumSyntax[] enumValueTable = { ENUM1,
                                                  null};

    public final String[] stringTable = { "str1",
                                           null };

    public Object readResolveEx() throws ObjectStreamException {
        return readResolve();
    }

}
