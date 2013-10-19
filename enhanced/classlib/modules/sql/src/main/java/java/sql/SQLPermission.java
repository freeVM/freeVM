/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
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


package java.sql;

import java.security.BasicPermission;
import java.security.Guard;
import java.io.Serializable;

/**
 * Permission relating to security access control in the java.sql package.
 * <p>
 * Currently, the only permission supported has the name "setLog".  The setLog permission
 * controls whether a Java application or applet can open a logging stream using the 
 * DriverManager.setLogWriter method or the DriverManager.setLogStream method.  This is a
 * potentially dangerous operation since the logging stream can contain usernames, passwords 
 */
public final class SQLPermission extends BasicPermission implements Guard, Serializable {
	
	private static final long serialVersionUID = -1439323187199563495L;

	/**
	 * Creates a new SQLPermission object with the specified name.
	 * @param name the name to use for this SQLPermission
	 */
	public SQLPermission(String name) {
		super( name );
		//if (name != "setLog") throw new IllegalArgumentException();	
	} // end method SQLPermissions( String )
    
	/**
	 * Creates a new SQLPermission object with the specified name.
	 * @param name is the name of the SQLPermission.  Currently only "setLog" is allowed.
	 * @param actions is currently unused and should be set to null
	 */
	public SQLPermission(String name, String actions) {
    	super( name, null );
    	//if (name != "setLog") throw new IllegalArgumentException(); 
    } // end method SQLPermissions( String, String )
    
} // end class SQLPermission


