/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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

package java.io;


/**
 * SerializablePermission objects represent permission to access unsafe
 * serialization operations. The name of the permission should be one of:
 * <dl>
 * <dt>enableSubclassImplementation</dt>
 * <dd>Subclasses can override serialization behavior</dd>
 * <dt>enableSubstitution</dt>
 * <dd>Object substitution can be enabled</dd>
 * </dl>
 * 
 * @see ObjectStreamConstants
 */
public final class SerializablePermission extends java.security.BasicPermission {
	private static final long serialVersionUID = 8537212141160296410L;

	private String actions;

	/**
	 * Creates an instance of this class with the given name.
	 * 
	 * @param permissionName
	 *            the name of the new permission.
	 */
	public SerializablePermission(String permissionName) {
		super(permissionName);
	}

	/**
	 * Creates an instance of this class with the given name and action list.
	 * The action list is ignored.
	 * 
	 * @param name
	 *            the name of the new permission.
	 * @param actions
	 *            ignored.
	 */
	public SerializablePermission(String name, String actions) {
		super(name, actions);
	}
}
