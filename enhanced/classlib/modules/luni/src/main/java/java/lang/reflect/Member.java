/* Copyright 1998, 2002 The Apache Software Foundation or its licensors, as applicable
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


/**
 * Implementors of this interface model a class member.
 * 
 * @see Field
 * @see Constructor
 * @see Method
 */
public interface Member {
	
	public static final int PUBLIC = 0;

	public static final int DECLARED = 1;

	/**
	 * Return the java.lang.Class associated with the class that defined this
	 * member.
	 * 
	 * @return the declaring class
	 */
	public abstract Class getDeclaringClass();

	/**
	 * Return the modifiers for the member. The Modifier class should be used to
	 * decode the result.
	 * 
	 * @return the modifiers
	 * @see java.lang.reflect.Modifier
	 */
	public abstract int getModifiers();

	/**
	 * Return the name of the member.
	 * 
	 * @return the name
	 */
	public abstract String getName();
}
