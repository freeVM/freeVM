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

package java.lang.reflect;

import java.lang.annotation.Annotation;

/**
 * <p>
 * An interface implemented an annotated element to enable reflective access to
 * annotation information.
 * </p>
 * 
 * @since 1.5
 */
public interface AnnotatedElement {

    /**
     * <p>
     * Gets the {@link Annotation} for this element for the annotation type
     * passed, if it exists.
     * </p>
     * 
     * @param annotationType The Class instance of the annotation to search for.
     * @return The {@link Annotation} for this element or <code>null</code>.
     * @throws NullPointerException if <code>annotationType</code> is
     *         <code>null</code>.
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    /**
     * <p>
     * Gets all {@link Annotation}s for this element.
     * </p>
     * 
     * @return An array of {@link Annotation}s, which may be empty, but never
     *         <code>null</code>.
     */
    Annotation[] getAnnotations();

    /**
     * <p>
     * Gets all {@link Annotation}s that are explicitly declared by this
     * element (not inherited).
     * </p>
     * 
     * @return An array of {@link Annotation}s, which may be empty, but never
     *         <code>null</code>.
     */
    Annotation[] getDeclaredAnnotations();

    /**
     * <p>
     * Determines if this element has an annotation for the annotation type
     * passed.
     * </p>
     * 
     * @param annotationType The class instance of the annotation to search for.
     * @return <code>true</code> if the annotation exists, otherwise
     *         <code>false</code>.
     * @throws NullPointerException if <code>annotationType</code> is
     *         <code>null</code>.
     */
    boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
}
