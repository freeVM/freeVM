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

package tests.api.javax.naming.directory;

import javax.naming.directory.AttributeInUseException;

import junit.framework.TestCase;

public class TestAttributeInUseException extends TestCase {

	public void testSetRemainingName() {
		AttributeInUseException ex = new AttributeInUseException("Test");
		ex.setRemainingName(null);
		assertNull(ex.getRemainingName());
	}

	public void testSetResolvedName() {
		AttributeInUseException ex = new AttributeInUseException("Test");
		ex.setResolvedName(null);
		assertNull(ex.getResolvedName());
	}
}
