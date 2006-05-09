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

package java.lang.reflect;

public interface TypeVariable<D extends GenericDeclaration> extends Type {

    /**
     * Answers the upper bounds of the type variable.
     * 
     * @return array of type variable's upper bounds.
     * @throws MalformedParameterizedTypeException
     * @throws TypeNotPresentException
     */
    Type[] getBounds();

    /**
     * Answers a GenericDeclaration object for this type variable.
     * 
     * @return the generic declaration spec
     */
    D getGenericDeclaration();

    /**
     * Answers the type variable's name from source.
     * 
     * @return the variable's name from the source code.
     */
    String getName();
}
