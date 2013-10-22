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

package org.apache.harmony.tests.java.text;

import java.text.Annotation;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase {

	/**
	 * @tests java.text.Annotation(Object)
	 */
	public void testAnnotation() {
		assertNotNull(new Annotation(null));
		assertNotNull(new Annotation("value"));
	}

	/**
	 * @tests java.text.Annotation.getValue()
	 */
	public void testGetValue() {
		Annotation a = new Annotation(null);
		assertNull(a.getValue());
		String s = "value";
		a = new Annotation(s);
		assertEquals("value", a.getValue());
	}

	/**
	 * @tests java.text.Annotation.toString()
	 */
	public void testToString() {
		assertNotNull(new Annotation(null).toString());
		assertNotNull(new Annotation("value").toString());
	}
}
