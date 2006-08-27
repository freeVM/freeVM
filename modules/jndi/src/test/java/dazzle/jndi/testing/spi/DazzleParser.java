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

package dazzle.jndi.testing.spi;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

class DazzleParser implements NameParser {

	static Properties syntax;

	static {
		syntax = new Properties();
		syntax.put("jndi.syntax.direction", "flat");
		syntax.put("jndi.syntax.ignorecase", "false");
	}

	DazzleParser() {
		super();
	}

	public Name parse(String strName) throws NamingException {
		return new CompoundName(strName, syntax);
	}
}
