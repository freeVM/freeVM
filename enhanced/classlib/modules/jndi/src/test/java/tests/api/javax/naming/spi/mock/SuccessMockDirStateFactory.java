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
package tests.api.javax.naming.mock;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public class SuccessMockDirStateFactory extends MockDirStateFactory {
	public Result getStateToBind(Object o, Name n, Context c, Hashtable h,
			Attributes a) throws NamingException {
		Hashtable r = new Hashtable();
		if (null != o) {
			r.put("o", o);
		}
		if (null != n) {
			r.put("n", n);
		}
		if (null != c) {
			r.put("c", c);
		}
		if (null != h) {
			r.put("h", h);
		}
		if (null != a) {
			r.put("a", a);
		}
		return new Result(r, a);

	}
}
