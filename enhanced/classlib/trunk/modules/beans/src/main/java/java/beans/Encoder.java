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

import java.awt.*;
import java.awt.font.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.harmony.beans.*;

/**
 * The <code>Encoder</code>, together with <code>PersistenceDelegate</code>
 * s, can encode an object into a series of java statements. By executing these
 * statements, a new object can be created and it will has the same state as the
 * original object which has been passed to the encoder. Here "has the same
 * state" means the two objects are indistinguishable from their public API.
 * <p>
 * The <code>Encoder</code> and <code>PersistenceDelegate</code> s do this
 * by creating copies of the input object and all objects it references. The
 * copy process continues recursively util every object in the object graph has
 * its new copy and the new version has the same state as the old version. All
 * statements used to create those new objects and executed on them during the
 * process form the result of encoding.
 * </p>
 * 
 */
@SuppressWarnings("unchecked")
public class Encoder {

	private static final Hashtable delegates = new Hashtable();

	private static final DefaultPersistenceDelegate defaultPD = new DefaultPersistenceDelegate();

	private static final ArrayPersistenceDelegate arrayPD = new ArrayPersistenceDelegate();

	private static final java_lang_reflect_ProxyPersistenceDelegate proxyPD = new java_lang_reflect_ProxyPersistenceDelegate();
    
    private static final NullPersistenceDelegate nullPD = new NullPersistenceDelegate();

	private static final ExceptionListener defaultExListener = new DefaultExceptionListener();
    
	private static class DefaultExceptionListener implements ExceptionListener {

		public void exceptionThrown(Exception exception) {
			System.err.println("Exception during encoding:" + exception); //$NON-NLS-1$
			System.err.println("Continue...");
		}

	}

	static {
		PersistenceDelegate ppd = new PrimitiveWrapperPersistenceDelegate();
		delegates.put(Boolean.class, ppd);
		delegates.put(Byte.class, ppd);
		delegates.put(Character.class, ppd);
		delegates.put(Double.class, ppd);
		delegates.put(Float.class, ppd);
		delegates.put(Integer.class, ppd);
		delegates.put(Long.class, ppd);
		delegates.put(Short.class, ppd);

		delegates.put(Class.class, new java_lang_ClassPersistenceDelegate());
		delegates.put(Field.class, new java_lang_reflect_FieldPersistenceDelegate());
		delegates.put(Method.class, new java_lang_reflect_MethodPersistenceDelegate());
		delegates.put(String.class, new java_lang_StringPersistenceDelegate());
		delegates.put(Proxy.class, new java_lang_reflect_ProxyPersistenceDelegate());
        
        delegates.put(Choice.class, new AwtChoicePersistenceDelegate());
        delegates.put(Color.class, new AwtColorPersistenceDelegate());
        delegates.put(Container.class, new AwtContainerPersistenceDelegate());
        delegates.put(Component.class, new AwtComponentPersistenceDelegate());
        delegates.put(Cursor.class, new AwtCursorPersistenceDelegate());
        delegates.put(Dimension.class, new AwtDimensionPersistenceDelegate());
        delegates.put(Font.class, new AwtFontPersistenceDelegate());
        delegates.put(Insets.class, new AwtInsetsPersistenceDelegate());
        delegates.put(List.class, new AwtListPersistenceDelegate());
        delegates.put(Menu.class, new AwtMenuPersistenceDelegate());
        delegates.put(MenuBar.class, new AwtMenuBarPersistenceDelegate());
        delegates.put(MenuShortcut.class, new AwtMenuShortcutPersistenceDelegate());
        delegates.put(Point.class, new AwtPointPersistenceDelegate());
        delegates.put(Rectangle.class, new AwtRectanglePersistenceDelegate());
        delegates.put(SystemColor.class, new AwtSystemColorPersistenceDelegate());
        delegates.put(TextAttribute.class, new AwtFontTextAttributePersistenceDelegate());
        
	}

	private ExceptionListener listener = defaultExListener;

	private ReferenceMap oldNewMap = new ReferenceMap();

	/**
	 * Construct a new encoder.
	 */
	public Encoder() {
		super();
	}

	/**
	 * Clear all the new objects have been created.
	 */
	void clear() {
		oldNewMap.clear();
	}

	/**
	 * Gets the new copy of the given old object.
	 * <p>
	 * Strings are special objects which have their new copy by default, so if
	 * the old object is a string, it is returned directly.
	 * </p>
	 * 
	 * @param old
	 *            an old object
	 * @return the new copy of the given old object, or null if there is not
	 *         one.
	 */
	public Object get(Object old) {
		if (old == null || old instanceof String) {
			return old;
		}
		return oldNewMap.get(old);
	}

	/**
	 * Returns the exception listener of this encoder.
	 * <p>
	 * An encoder always have a non-null exception listener. A default exception
	 * listener is used when the encoder is created.
	 * </p>
	 * 
	 * @return the exception listener of this encoder
	 */
	public ExceptionListener getExceptionListener() {
		return listener;
	}

	/**
	 * Returns a <code>PersistenceDelegate</code> for the given class type.
	 * <p>
	 * The <code>PersistenceDelegate</code> is determined as following:
	 * <ol>
	 * <li>If a <code>PersistenceDelegate</code> has been registered by
	 * calling <code>setPersistenceDelegate</code> for the given type, it is
	 * returned.</li>
	 * <li>If the given type is an array class, a special
	 * <code>PersistenceDelegate</code> for array types is returned.</li>
	 * <li>If the given type is a proxy class, a special
	 * <code>PersistenceDelegate</code> for proxy classes is returned.</li>
	 * <li><code>Introspector</code> is used to check the bean descriptor
	 * value "persistenceDelegate". If one is set, it is returned.</li>
	 * <li>If none of the above applies, the
	 * <code>DefaultPersistenceDelegate</code> is returned.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param type
	 *            a class type
	 * @return a <code>PersistenceDelegate</code> for the given class type
	 */
	public PersistenceDelegate getPersistenceDelegate(Class<?> type) {
		if (type == null) {
			return nullPD; // may be return a special PD?
		}

        // registered delegate
		PersistenceDelegate registeredPD = (PersistenceDelegate) delegates
				.get(type);
		if (registeredPD != null) {
			return registeredPD;
		}
		
        if (Collection.class.isAssignableFrom(type)) {
            return new UtilCollectionPersistenceDelegate();
        }
        
		if (type.isArray()) {
			return arrayPD;
		}
		if (Proxy.isProxyClass(type)) {
			return proxyPD;
		}

		// check "persistenceDelegate" property
		try {
			BeanInfo binfo = Introspector.getBeanInfo(type);
			if (binfo != null) {
				PersistenceDelegate pd = (PersistenceDelegate) binfo
						.getBeanDescriptor().getValue("persistenceDelegate"); //$NON-NLS-1$
				if (pd != null) {
					return pd;
				}
			}
		} catch (Exception e) {
			// ignore
		}

		// default persistence delegate
		return defaultPD;
	}

	private void put(Object old, Object nu) {
		oldNewMap.put(old, nu);
	}

	/**
	 * Remvoe the existing new copy of the given old object.
	 * 
	 * @param old
	 *            an old object
	 * @return the removed new version of the old object, or null if there is
	 *         not one
	 */
	public Object remove(Object old) {
		return oldNewMap.remove(old);
	}

	/**
	 * Sets the exception listener of this encoder.
	 * 
	 * @param listener
	 *            the exception listener to set
	 */
	public void setExceptionListener(ExceptionListener listener) {
		if (listener == null) {
			listener = defaultExListener;
		}
		this.listener = listener;
	}

	/**
	 * Register the <code>PersistenceDelegate</code> of the specified type.
	 * 
	 * @param type
	 * @param delegate
	 */
	public void setPersistenceDelegate(Class<?> type, PersistenceDelegate delegate) {
		if (type == null || delegate == null) {
			throw new NullPointerException();
		}
		delegates.put(type, delegate);
	}

	private Object forceNew(Object old) {
		if (old == null) {
			return null;
		}
		Object nu = get(old);
		if (nu != null) {
			return nu;
		}
		writeObject(old);
		return get(old);
	}

	private Object[] forceNewArray(Object oldArray[]) {
		if (oldArray == null) {
			return null;
		}
		Object newArray[] = new Object[oldArray.length];
		for (int i = 0; i < oldArray.length; i++) {
			newArray[i] = forceNew(oldArray[i]);
		}
		return newArray;
	}

	/**
	 * Write an expression of old objects.
	 * <p>
	 * The implementation first check the return value of the expression. If
	 * there exists a new version of the object, simply return.
	 * </p>
	 * <p>
	 * A new expression is created using the new versions of the target and the
	 * arguments. If any of the old objects do not have its new version yet,
	 * <code>writeObject()</code> is called to create the new version.
	 * </p>
	 * <p>
	 * The new expression is then executed to obtained a new copy of the old
	 * return value.
	 * </p>
	 * <p>
	 * Call <code>writeObject()</code> with the old return value, so that more
	 * statements will be executed on its new version to change it into the same
	 * state as the old value.
	 * </p>
	 * 
	 * @param oldExp
	 *            the expression to write. The target, arguments, and return
	 *            value of the expression are all old objects.
	 */
	public void writeExpression(Expression oldExp) {
		if (oldExp == null) {
			throw new NullPointerException();
		}
		try {
			// if oldValue exists, noop
			Object oldValue = oldExp.getValue();
			if (oldValue == null || get(oldValue) != null) {
				return;
			}

			// copy to newExp
			Object newTarget = forceNew(oldExp.getTarget());
			Object newArgs[] = forceNewArray(oldExp.getArguments());
			Expression newExp = new Expression(newTarget, oldExp
					.getMethodName(), newArgs);

			// execute newExp
			Object newValue = null;
			try {
				newValue = newExp.getValue();
			} catch (IndexOutOfBoundsException ex) {
				// Current Container does not have any component, newVal set
				// to null
			}

			// relate oldValue to newValue
			put(oldValue, newValue);

			// force same state
			writeObject(oldValue);
		} catch (Exception e) {
			listener.exceptionThrown(new Exception(
					"failed to write expression: " + oldExp, e)); //$NON-NLS-1$
		}
	}

	/**
	 * Encode the given object into a series of statements and expressions.
	 * <p>
	 * The implementation simply finds the <code>PersistenceDelegate</code>
	 * responsible for the object's class, and delegate the call to it.
	 * </p>
	 * 
	 * @param o
	 *            the object to encode
	 */
	protected void writeObject(Object o) {
		if (o == null) {
			return;
		}
		Class type = o.getClass();
		getPersistenceDelegate(type).writeObject(o, this);
	}

	/**
	 * Write a statement of old objects.
	 * <p>
	 * A new statement is created by using the new versions of the target and
	 * arguments. If any of the objects do not have its new copy yet,
	 * <code>writeObject()</code> is called to create one.
	 * </p>
	 * <p>
	 * The new statement is then executed to change the state of the new object.
	 * </p>
	 * 
	 * @param oldStat
	 *            a statement of old objects
	 */
	public void writeStatement(Statement oldStat) {
		if (oldStat == null) {
			throw new NullPointerException();
		}
		try {
			// copy to newStat
			Object newTarget = forceNew(oldStat.getTarget());
			Object newArgs[] = forceNewArray(oldStat.getArguments());
			Statement newStat = new Statement(newTarget, oldStat
					.getMethodName(), newArgs);

			// execute newStat
			newStat.execute();
		} catch (Exception e) {
			listener.exceptionThrown(new Exception(
					"failed to write statement: " + oldStat, e)); //$NON-NLS-1$
		}
	}

}

