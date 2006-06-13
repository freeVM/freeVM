/*
 * Copyright 2005 The Apache Software Foundation or its licensors, as
 * applicable.
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

package java.beans;

/**
 * Describes a bean's global information.
 * 
 * @author Maxim V. Berkultsev
 */
public class BeanDescriptor extends FeatureDescriptor {
    private Class<?> beanClass;

    private Class<?> customizerClass;

    /**
     * <p>
     * Constructs an instance with the bean's {@link Class} and a customizer
     * {@link Class}. The descriptor's {@link #getName()} is set as the
     * unqualified name of the <code>beanClass</code>.
     * </p>
     * 
     * @param beanClass The bean's Class.
     * @param customizerClass The bean's customizer Class.
     */
    public BeanDescriptor(Class<?> beanClass, Class<?> customizerClass) {
        super();
        if (beanClass == null) {
            throw new NullPointerException();
        }
        setName(getShortClassName(beanClass));
        this.beanClass = beanClass;
        this.customizerClass = customizerClass;
    }

    /**
     * <p>
     * Constructs an instance with the bean's {@link Class}. The descriptor's
     * {@link #getName()} is set as the unqualified name of the
     * <code>beanClass</code>.
     * </p>
     * 
     * @param beanClass The bean's Class.
     */
    public BeanDescriptor(Class<?> beanClass) {
        super();

        if (beanClass == null) {
            throw new NullPointerException();
        }
        setName(getShortClassName(beanClass));
        this.beanClass = beanClass;
    }

    /**
     * <p>
     * Gets the bean's customizer {@link Class}/
     * </p>
     * 
     * @return A {@link Class} instance or <code>null</code>.
     */
    public Class<?> getCustomizerClass() {
        return customizerClass;
    }

    /**
     * <p>
     * Gets the bean's {@link Class}.
     * </p>
     * 
     * @return A {@link Class} instance.
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * <p>
     * Utility method for getting the unqualified name of a {@link Class}.
     * </p>
     * 
     * @param beanClass The Class to get the name from.
     * @return A String instance or <code>null</code>.
     */
    private String getShortClassName(Class<?> beanClass) {
        String result = null;

        if (beanClass != null) {
            String beanClassName = beanClass.getName();
            int idx = beanClassName.lastIndexOf(".");
            result = (idx == -1) ? beanClassName : beanClassName
                    .substring(idx + 1);
        }

        return result;
    }

}
