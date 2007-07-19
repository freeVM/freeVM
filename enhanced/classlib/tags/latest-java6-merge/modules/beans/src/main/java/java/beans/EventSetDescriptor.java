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

package java.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import org.apache.harmony.beans.internal.nls.Messages;

public class EventSetDescriptor extends FeatureDescriptor {
    private Class<?> listenerType;

    private ArrayList<MethodDescriptor> listenerMethodDescriptors;

    private Method[] listenerMethods;

    private Method getListenerMethod;

    private Method addListenerMethod;

    private Method removeListenerMethod;

    private boolean unicast;

    private boolean inDefaultEventSet = true;

    public EventSetDescriptor(Class<?> sourceClass, String eventSetName,
            Class<?> listenerType, String listenerMethodName)
            throws IntrospectionException {
        Method m;

        checkNotNull(sourceClass, eventSetName, listenerType,
                listenerMethodName);

        setName(eventSetName);
        this.listenerType = listenerType;
        m = findListenerMethodByName(listenerMethodName);
        checkEventType(eventSetName, m);
        listenerMethodDescriptors = new ArrayList<MethodDescriptor>();
        listenerMethodDescriptors.add(new MethodDescriptor(m));
        addListenerMethod = findMethodByPrefix(sourceClass, "add", ""); //$NON-NLS-1$ //$NON-NLS-2$
        removeListenerMethod = findMethodByPrefix(sourceClass, "remove", ""); //$NON-NLS-1$ //$NON-NLS-2$

        if (addListenerMethod == null || removeListenerMethod == null) {
            throw new IntrospectionException(Messages.getString("beans.38")); //$NON-NLS-1$
        }

        getListenerMethod = findMethodByPrefix(sourceClass, "get", "s"); //$NON-NLS-1$ //$NON-NLS-2$
        unicast = isUnicastByDefault(addListenerMethod);
    }

    public EventSetDescriptor(Class<?> sourceClass, String eventSetName,
            Class<?> listenerType, String[] listenerMethodNames,
            String addListenerMethodName, String removeListenerMethodName)
            throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType, listenerMethodNames,
                addListenerMethodName, removeListenerMethodName, null);

    }

    public EventSetDescriptor(Class<?> sourceClass, String eventSetName,
            Class<?> listenerType, String[] listenerMethodNames,
            String addListenerMethodName, String removeListenerMethodName,
            String getListenerMethodName) throws IntrospectionException {

        checkNotNull(sourceClass, eventSetName, listenerType,
                listenerMethodNames);

        setName(eventSetName);
        this.listenerType = listenerType;

        listenerMethodDescriptors = new ArrayList<MethodDescriptor>();
        for (String element : listenerMethodNames) {
            Method m = findListenerMethodByName(element);

            // checkEventType(eventSetName, m);
            listenerMethodDescriptors.add(new MethodDescriptor(m));
        }

        if (addListenerMethodName != null) {
            this.addListenerMethod = findAddRemoveListenerMethod(sourceClass,
                    addListenerMethodName);
        }
        if (removeListenerMethodName != null) {
            this.removeListenerMethod = findAddRemoveListenerMethod(
                    sourceClass, removeListenerMethodName);
        }
        if (getListenerMethodName != null) {
            this.getListenerMethod = findGetListenerMethod(sourceClass,
                    getListenerMethodName);
        }
        this.unicast = isUnicastByDefault(addListenerMethod);
    }

    private Method findListenerMethodByName(String listenerMethodName)
            throws IntrospectionException {
        Method method = null;
        Method[] methods = listenerType.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(listenerMethodName)) {
                Class[] paramTypes = m.getParameterTypes();
                if (paramTypes.length == 1
                        && paramTypes[0].getName().endsWith("Event")) {
                    method = m;
                    break;
                }

            }
        }
        if (null == method) {
            throw new IntrospectionException(Messages.getString("beans.31", //$NON-NLS-1$
                    listenerMethodName, listenerType.getName()));
        }
        return method;
    }

    public EventSetDescriptor(String eventSetName, Class<?> listenerType,
            Method[] listenerMethods, Method addListenerMethod,
            Method removeListenerMethod) throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethods, addListenerMethod,
                removeListenerMethod, null);
    }

    public EventSetDescriptor(String eventSetName, Class<?> listenerType,
            Method[] listenerMethods, Method addListenerMethod,
            Method removeListenerMethod, Method getListenerMethod)
            throws IntrospectionException {

        setName(eventSetName);
        this.listenerType = listenerType;

        this.listenerMethods = listenerMethods;
        if (listenerMethods != null) {
            listenerMethodDescriptors = new ArrayList<MethodDescriptor>();

            for (Method element : listenerMethods) {
                // XXX do we need this check?
                // checkEventType(eventSetName, element);
                // if (checkMethod(listenerType, element)) {
                this.listenerMethodDescriptors
                        .add(new MethodDescriptor(element));
                // }
            }
        }

        this.addListenerMethod = addListenerMethod;
        this.removeListenerMethod = removeListenerMethod;
        this.getListenerMethod = getListenerMethod;
        this.unicast = isUnicastByDefault(addListenerMethod);
    }

    public EventSetDescriptor(String eventSetName, Class<?> listenerType,
            MethodDescriptor[] listenerMethodDescriptors,
            Method addListenerMethod, Method removeListenerMethod)
            throws IntrospectionException {
        this(eventSetName, listenerType, null, addListenerMethod,
                removeListenerMethod, null);

        if (listenerMethodDescriptors != null) {
            this.listenerMethodDescriptors = new ArrayList<MethodDescriptor>();

            for (MethodDescriptor element : listenerMethodDescriptors) {
                element.getMethod();
                this.listenerMethodDescriptors.add(element);
            }
        }
    }

    // ensures that there is no nulls
    private void checkNotNull(Object sourceClass, Object eventSetName,
            Object listenerType, Object listenerMethodName) {
        if (sourceClass == null) {
            throw new NullPointerException(Messages.getString("beans.0C"));
        }
        if (eventSetName == null) {
            throw new NullPointerException(Messages.getString("beans.53"));
        }
        if (listenerType == null) {
            throw new NullPointerException(Messages.getString("beans.54"));
        }
        if (listenerMethodName == null) {
            throw new NullPointerException(Messages.getString("beans.52"));
        }
    }

    /**
     * Checks that given listener method has an argument of the valid type.
     * 
     * @param eventSetName
     *            event set name
     * @param listenerMethod
     *            listener method
     * @throws IntrospectionException
     *             if check fails
     */
    private static void checkEventType(String eventSetName,
            Method listenerMethod) throws IntrospectionException {
        Class<?>[] params = listenerMethod.getParameterTypes();
        String firstParamTypeName = null;
        String eventTypeName = prepareEventTypeName(eventSetName);

        if (params.length > 0) {
            firstParamTypeName = BeanInfoImpl.extractShortClassName(params[0]
                    .getName());
        }

        if (firstParamTypeName == null
                || !firstParamTypeName.equals(eventTypeName)) {
            throw new IntrospectionException(Messages.getString("beans.51", //$NON-NLS-1$
                    listenerMethod.getName(), eventTypeName));
        }
    }

    private static String prepareEventTypeName(String eventSetName) {
        StringBuilder sb = new StringBuilder();

        if (eventSetName != null && eventSetName.length() > 0) {
            sb.append(Character.toUpperCase(eventSetName.charAt(0)));

            if (eventSetName.length() > 1) {
                sb.append(eventSetName.substring(1));
            }
        }

        sb.append("Event"); //$NON-NLS-1$
        return sb.toString();
    }

    public Method[] getListenerMethods() {
        int i = 0;

        if (listenerMethods != null) {
            return listenerMethods;
        }

        if (listenerMethodDescriptors != null) {
            listenerMethods = new Method[listenerMethodDescriptors.size()];
            for (MethodDescriptor md : listenerMethodDescriptors) {
                listenerMethods[i++] = md.getMethod();
            }
            return listenerMethods;
        }

        return null;
    }

    public MethodDescriptor[] getListenerMethodDescriptors() {
        return listenerMethodDescriptors == null ? null
                : listenerMethodDescriptors
                        .toArray(new MethodDescriptor[listenerMethodDescriptors
                                .size()]);
    }

    public Method getRemoveListenerMethod() {
        return removeListenerMethod;
    }

    public Method getGetListenerMethod() {
        return getListenerMethod;
    }

    public Method getAddListenerMethod() {
        return addListenerMethod;
    }

    public Class<?> getListenerType() {
        return listenerType;
    }

    public void setUnicast(boolean unicast) {
        this.unicast = unicast;
    }

    public void setInDefaultEventSet(boolean inDefaultEventSet) {
        this.inDefaultEventSet = inDefaultEventSet;
    }

    public boolean isUnicast() {
        return unicast;
    }

    public boolean isInDefaultEventSet() {
        return inDefaultEventSet;
    }

    /**
     * Searches for {add|remove}Listener methods in the event source. Parameter
     * check is also performed.
     * 
     * @param sourceClass
     *            event source class
     * @param methodName
     *            method name to search
     * @return found method
     * @throws IntrospectionException
     *             if no valid method found
     */
    private Method findAddRemoveListenerMethod(Class<?> sourceClass,
            String methodName) throws IntrospectionException {
        try {
            return sourceClass.getMethod(methodName, listenerType);
        } catch (NoSuchMethodException e) {
            return findAddRemoveListnerMethodWithLessCheck(sourceClass,
                    methodName, listenerType);
        } catch (Exception e) {
            throw new IntrospectionException(Messages.getString("beans.31", //$NON-NLS-1$
                    methodName, listenerType.getName()));
        }
    }

    private Method findAddRemoveListnerMethodWithLessCheck(
            Class<?> sourceClass, String methodName, Class listenerTYpe)
            throws IntrospectionException {
        String expectedListenerTypeName = listenerType.getName();
        expectedListenerTypeName = expectedListenerTypeName
                .substring(expectedListenerTypeName.lastIndexOf(".") + 1);
        Method method = null;
        Method[] methods = sourceClass.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                Class[] paramTypes = m.getParameterTypes();
                if (paramTypes.length == 1) {
                    // String paramTypeName = paramTypes[0].getName();
                    // paramTypeName = paramTypeName.substring(paramTypeName
                    // .lastIndexOf(".") + 1);
                    // if (paramTypeName.endsWith("Listener")) {
                    // paramTypeName = paramTypeName.substring(0,
                    // paramTypeName.length() - "Listener".length());
                    // if (expectedListenerTypeName.startsWith(paramTypeName)) {
                    method = m;
                    break;
                    // }
                    // }
                }
            }
        }
        if (null == method) {
            throw new IntrospectionException(Messages.getString("beans.31", //$NON-NLS-1$
                    methodName, listenerType.getName()));
        }
        return method;
    }

    /**
     * @param sourceClass
     *            class of event source
     * @param methodName
     *            name of the custom getListeners() method
     * @return found Method object for custom getListener or null if nothing is
     *         found
     */
    private Method findGetListenerMethod(Class<?> sourceClass, String methodName) {
        try {
            return sourceClass.getMethod(methodName);
        } catch (Exception e) {
            // RI keeps silence here and just retunrs null
            return null;
        }
    }

    private Method findMethodByPrefix(Class<?> sourceClass, String prefix,
            String postfix) {
        String shortName = listenerType.getName();
        if (listenerType.getPackage() != null) {
            shortName = shortName.substring(listenerType.getPackage().getName()
                    .length() + 1);
        }
        String methodName = prefix + shortName + postfix;
        try {
            if (prefix.equals("get")) { //$NON-NLS-1$
                return sourceClass.getMethod(methodName);
            }
        } catch (NoSuchMethodException nsme) {
            return null;
        }
        Method[] m = sourceClass.getMethods();
        for (int i = 0; i < m.length; i++) {
            if (m[i].getName().equals(methodName)) {
                Class[] paramTypes = m[i].getParameterTypes();
                if (paramTypes.length == 1) {
                    return m[i];
                }
            }
        }
        return null;
    }

    private static boolean isUnicastByDefault(Method addMethod) {
        if (addMethod != null) {
            Class<?>[] exceptionTypes = addMethod.getExceptionTypes();
            for (Class<?> element : exceptionTypes) {
                if (element.equals(TooManyListenersException.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    void merge(EventSetDescriptor event) {
        super.merge(event);
        if (addListenerMethod == null) {
            addListenerMethod = event.addListenerMethod;
        }
        if (getListenerMethod == null) {
            getListenerMethod = event.getListenerMethod;
        }
        if (listenerMethodDescriptors == null) {
            listenerMethodDescriptors = event.listenerMethodDescriptors;
        }
        if (listenerMethods == null) {
            listenerMethods = event.listenerMethods;
        }
        if (listenerType == null) {
            listenerType = event.listenerType;
        }

        if (removeListenerMethod == null) {
            removeListenerMethod = event.removeListenerMethod;
        }
        inDefaultEventSet &= event.inDefaultEventSet;
    }
}
