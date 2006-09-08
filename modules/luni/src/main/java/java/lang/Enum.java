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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.apache.harmony.luni.util.Msg;

/**
 * The superclass of all enumerated types.
 */
public abstract class Enum<E extends Enum<E>> implements Serializable,
        Comparable<E> {

    private static final long serialVersionUID = 0L;

    private final String name;

    private final int ordinal;

    /**
     * Constructor for enum subtypes.
     * 
     * @param name
     *            the enum constant declared name.
     * @param ordinal
     *            the enum constant position ordinal.
     */
    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Answers the name of the enum constant. The name is the field as it
     * appears in the <code>Enum</code> declaration.
     * 
     * @return the precise enum constant name.
     * @see #toString()
     */
    public final String name() {
        return name;
    }

    /**
     * Answers the position of the enum constant in the declaration. The first
     * constant has and ordinal value of zero.
     * 
     * @return the constant's ordinal value.
     */
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Answer a string representation of the receiver suitable for display to a
     * programmer.
     * 
     * @return the displayable string name.
     */
    @Override
    public String toString() {
        return "Enum:" + name;
    }

    /**
     * Answers true only if the receiver is equal to the argument. Since enums
     * are unique this is equivalent to an identity test.
     * 
     * @return true if the receiver and argument are equal, otherwise return
     *         false.
     */
    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    /**
     * Answers the hash of the receiver.
     * 
     * @return the hash code.
     */
    @Override
    public final int hashCode() {
        return ordinal + (name == null ? 0 : name.hashCode());
    }

    /**
     * Enums are singletons, they may not be cloned. This method always throws a
     * {@link CloneNotSupportedException}.
     * 
     * @return does not return.
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        // KA004=Enums may not be cloned
        throw new CloneNotSupportedException(Msg.getString("KA004"));
    }

    /**
     * Answers the comparative ordering of the receiver and the given argument.
     * If the receiver is naturally ordered before the actual argument then the
     * result is negative, if the receiver is naturally ordered in equal
     * position to the actual argument then the result is zero, and if the
     * receiver is naturally ordered after the actual argument then the result
     * is positive.
     * 
     * @return negative, zero, or positive value depending upon before, equal,
     *         or after natural order respectively.
     * @see Comparable#compareTo(Object)
     */
    public final int compareTo(E o) {
        return ordinal - o.ordinal;
    }

    /**
     * Answers the enum constant's declaring class.
     * 
     * @return the class object representing the constant's enum type.
     */
    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> myClass = getClass();
        Class<?> mySuperClass = myClass.getSuperclass();
        if (Enum.class == mySuperClass) {
            return (Class<E>)myClass;
        }
        return (Class<E>)mySuperClass;
    }

    /**
     * Answers the named constant of the given enum type.
     * 
     * @param enumType
     *            the class of the enumerated type to search for the constant
     *            value.
     * @param name
     *            the name of the constant value to find.
     * @return the enum constant
     * @throws NullPointerException
     *             if either the <code>enumType</code> or <code>name</code>
     *             are <code>null</code>.
     * @throws IllegalArgumentException
     *             if <code>enumType</code> is not an enumerated type or does
     *             not have a constant value called <code>name</code>.
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if ((enumType == null) || (name == null)) {
            // KA001=Argument must not be null
            throw new NullPointerException(Msg.getString("KA001"));
        }
        T[] values = getValues(enumType);
        if (values == null) {
            // KA005={0} is not an enum type
            throw new IllegalArgumentException(Msg.getString("KA005", enumType));
        }
        for (T enumConst : values) {
            if (enumConst.name.equals(name)) {
                return enumConst;
            }
        }
        // KA006={0} is not a constant in the enum type {1}
        throw new IllegalArgumentException(Msg.getString("KA006", name,
                enumType));
    }

    /*
     * Helper to invoke the values() static method on T and answer the result.
     * Returns null if there is a problem.
     */
    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T[] getValues(final Class<T> enumType) {
        try {
            Method values = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Method>() {
                        public Method run() throws Exception {
                            Method valsMethod = enumType.getMethod("values",
                                    (Class[]) null);
                            valsMethod.setAccessible(true);
                            return valsMethod;
                        }
                    });
            return (T[]) values.invoke(enumType, (Object[])null);
        } catch (Exception e) {
            return null;
        }
    }
}
