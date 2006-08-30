/* Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @author Victor A. Martynov
 * @version $Revision: 1.4 $
 */
package org.apache.harmony.x.management.console.plugin.editor;

import org.eclipse.swt.widgets.Composite;

public class EditorFactory {

	public static AbstractEditor create(Composite parent, 
                                        String mbean_name, 
                                        String attribute_name, 
                                        String type, 
                                        boolean isEditable) { 
		
		if(type.equals("boolean")) {
			return new BooleanEditor(parent, mbean_name, attribute_name, type, isEditable);
		} else if(type.equals("byte") 
			   || type.equals("short") 
			   || type.equals("int")
			   || type.equals("long")
			   || type.equals("float")
			   || type.equals("double")) {
			return new NumericEditor(parent, mbean_name, attribute_name, type, isEditable);
		} if(type.indexOf("[") != -1) { // Array
			return new ArrayEditor(parent, mbean_name, attribute_name, type, isEditable);
		}else {
			return new CommonEditor(parent, mbean_name, attribute_name, type, isEditable);
		}
		
	}

}
