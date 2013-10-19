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
package tests.api.java.util.jar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

public class AttributesTest extends junit.framework.TestCase {

	Attributes a;

	/**
	 * @tests java.util.jar.Attributes#Attributes()
	 */
	public void test_Constructor() {
		// Test for method java.util.jar.Attributes()
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.jar.Attributes#Attributes(java.util.jar.Attributes)
	 */
	public void test_ConstructorLjava_util_jar_Attributes() {
		Attributes a2 = new Attributes(a);
		assertTrue("not equal", a.equals(a2));
		a.putValue("1", "one(1)");
		assertTrue("equal", !a.equals(a2));
	}

	/**
	 * @tests java.util.jar.Attributes#clear()
	 */
	public void test_clear() {
		// Test for method void java.util.jar.Attributes.clear()
		a.clear();
		assertTrue("a) All entries should be null after clear",
				a.get("1") == null);
		assertTrue("b) All entries should be null after clear",
				a.get("2") == null);
		assertTrue("c) All entries should be null after clear",
				a.get("3") == null);
		assertTrue("d) All entries should be null after clear",
				a.get("4") == null);
		assertTrue("Should not contain any keys", !a.containsKey("1"));
	}

	/**
	 * @tests java.util.jar.Attributes#containsKey(java.lang.Object)
	 */
	public void test_containsKeyLjava_lang_Object() {
		// Test for method boolean
		// java.util.jar.Attributes.containsKey(java.lang.Object)
		assertTrue("a) Should have returned false", !a.containsKey(new Integer(
				1)));
		assertTrue("b) Should have returned false", !a.containsKey("0"));
		assertTrue("Should have returned true", a
				.containsKey(new Attributes.Name("1")));
	}

	/**
	 * @tests java.util.jar.Attributes#containsValue(java.lang.Object)
	 */
	public void test_containsValueLjava_lang_Object() {
		// Test for method boolean
		// java.util.jar.Attributes.containsValue(java.lang.Object)
		assertTrue("Should have returned false", !a.containsValue("One"));
		assertTrue("Should have returned true", a.containsValue("one"));

	}

	/**
	 * @tests java.util.jar.Attributes#entrySet()
	 */
	public void test_entrySet() {
		// Test for method java.util.Set java.util.jar.Attributes.entrySet()
		Set entrySet = a.entrySet();
		Set keySet = new HashSet();
		Set valueSet = new HashSet();
		Iterator i;
		assertTrue("Wrong size--Wanted: 4, got: " + entrySet.size(), entrySet
				.size() == 4);
		i = entrySet.iterator();
		while (i.hasNext()) {
			java.util.Map.Entry e;
			e = (Map.Entry) i.next();
			keySet.add(e.getKey());
			valueSet.add(e.getValue());
		}

		assertTrue("a) Should contain entry", valueSet.contains("one"));
		assertTrue("b) Should contain entry", valueSet.contains("two"));
		assertTrue("c) Should contain entry", valueSet.contains("three"));
		assertTrue("d) Should contain entry", valueSet.contains("four"));

		assertTrue("a) Should contain key", keySet
				.contains(new Attributes.Name("1")));
		assertTrue("b) Should contain key", keySet
				.contains(new Attributes.Name("2")));
		assertTrue("c) Should contain key", keySet
				.contains(new Attributes.Name("3")));
		assertTrue("d) Should contain key", keySet
				.contains(new Attributes.Name("4")));
	}

	/**
	 * @tests java.util.jar.Attributes#get(java.lang.Object)
	 */
	public void test_getLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.jar.Attributes.get(java.lang.Object)
		assertTrue("a) Incorrect value returned", a.getValue("1").equals("one"));
		assertTrue("b) Incorrect value returned", a.getValue("0") == null);
	}

	/**
	 * @tests java.util.jar.Attributes#isEmpty()
	 */
	public void test_isEmpty() {
		// Test for method boolean java.util.jar.Attributes.isEmpty()
		assertTrue("Should not be empty", !a.isEmpty());
		a.clear();
		assertTrue("a) Should be empty", a.isEmpty());
		a = new Attributes();
		assertTrue("b) Should be empty", a.isEmpty());
	}

	/**
	 * @tests java.util.jar.Attributes#keySet()
	 */
	public void test_keySet() {
		// Test for method java.util.Set java.util.jar.Attributes.keySet()
		Set s = a.keySet();
		assertTrue("Wrong size--Wanted: 4, got: " + s.size(), s.size() == 4);
		assertTrue("a) Should contain entry", s.contains(new Attributes.Name(
				"1")));
		assertTrue("b) Should contain entry", s.contains(new Attributes.Name(
				"2")));
		assertTrue("c) Should contain entry", s.contains(new Attributes.Name(
				"3")));
		assertTrue("d) Should contain entry", s.contains(new Attributes.Name(
				"4")));
	}

	/**
	 * @tests java.util.jar.Attributes#put(java.lang.Object, java.lang.Object)
	 */
	public void test_putLjava_lang_ObjectLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.jar.Attributes.put(java.lang.Object, java.lang.Object)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.jar.Attributes#putAll(java.util.Map)
	 */
	public void test_putAllLjava_util_Map() {
		// Test for method void java.util.jar.Attributes.putAll(java.util.Map)
		Attributes b = new Attributes();
		b.putValue("3", "san");
		b.putValue("4", "shi");
		b.putValue("5", "go");
		b.putValue("6", "roku");
		a.putAll(b);
		assertTrue("Should not have been replaced", a.getValue("1").equals(
				"one"));
		assertTrue("Should have been replaced", a.getValue("3").equals("san"));
		assertTrue("Should have been added", a.getValue("5").equals("go"));

	}

	/**
	 * @tests java.util.jar.Attributes#remove(java.lang.Object)
	 */
	public void test_removeLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.jar.Attributes.remove(java.lang.Object)
		a.remove(new Attributes.Name("1"));
		a.remove(new Attributes.Name("3"));
		assertTrue("Should have been removed", a.getValue("1") == null);
		assertTrue("Should not have been removed", a.getValue("4").equals(
				"four"));
	}

	/**
	 * @tests java.util.jar.Attributes#size()
	 */
	public void test_size() {
		// Test for method int java.util.jar.Attributes.size()
		assertTrue("Incorrect size returned", a.size() == 4);
		a.clear();
		assertTrue("Should have returned 0 size, but got: " + a.size(), a
				.size() == 0);
	}

	/**
	 * @tests java.util.jar.Attributes#values()
	 */
	public void test_values() {
		// Test for method java.util.Collection
		// java.util.jar.Attributes.values()
		Collection valueCollection = a.values();
		assertTrue("a) Should contain entry", valueCollection.contains("one"));
		assertTrue("b) Should contain entry", valueCollection.contains("two"));
		assertTrue("c) Should contain entry", valueCollection.contains("three"));
		assertTrue("d) Should contain entry", valueCollection.contains("four"));
	}

	/**
	 * @tests java.util.jar.Attributes#clone()
	 */
	public void test_clone() {
		Attributes a2 = (Attributes) a.clone();
		assertTrue("not equal", a.equals(a2));
		a.putValue("1", "one(1)");
		assertTrue("equal", !a.equals(a2));
	}

	/**
	 * @tests java.util.jar.Attributes#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		Attributes.Name n1 = new Attributes.Name("name"), n2 = new Attributes.Name(
				"Name");
		assertTrue("Names not equal", n1.equals(n2));

		Attributes a1 = new Attributes();
		a1.putValue("one", "1");
		a1.putValue("two", "2");
		Attributes a2 = new Attributes();
		a2.putValue("One", "1");
		a2.putValue("TWO", "2");
		assertTrue("not equal", a1.equals(a2));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		a = new Attributes();
		a.putValue("1", "one");
		a.putValue("2", "two");
		a.putValue("3", "three");
		a.putValue("4", "four");
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}

}
