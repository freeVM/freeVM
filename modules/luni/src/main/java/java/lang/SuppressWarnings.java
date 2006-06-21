/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;

/**
 * Stub implementation
 */
// GCH -- below lines do not compile inside Eclipse (OK with RI javac & jsr14
// target)
//@Target( { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
//        ElementType.PARAMETER, ElementType.CONSTRUCTOR,
//        ElementType.LOCAL_VARIABLE })
//@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    public String[] value();
}
