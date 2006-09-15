/*
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.lang.annotation;

/**
 * <p>
 * An enumeration of element types.
 * </p>
 * 
 * @since 1.5
 */
public enum ElementType {
    /**
     * <p>
     * Class, interface or enum declaration.
     * </p>
     */
    TYPE,
    /**
     * <p>
     * Field declaration.
     * </p>
     */
    FIELD,
    /**
     * <p>
     * Method declaration.
     * </p>
     */
    METHOD,
    /**
     * <p>
     * Parameter declaration.
     * </p>
     */
    PARAMETER,
    /**
     * <p>
     * Constructor declaration.
     * </p>
     */
    CONSTRUCTOR,
    /**
     * <p>
     * Local variable declaration.
     * </p>
     */
    LOCAL_VARIABLE,
    /**
     * <p>
     * Annotation type declaration.
     * </p>
     */
    ANNOTATION_TYPE,
    /**
     * <p>
     * Package declaration.
     * </p>
     */
    PACKAGE
}
