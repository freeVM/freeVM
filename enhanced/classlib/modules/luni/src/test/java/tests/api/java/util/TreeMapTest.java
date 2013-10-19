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

package tests.api.java.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import tests.support.Support_MapTest2;
import tests.support.Support_UnmodifiableCollectionTest;

public class TreeMapTest extends junit.framework.TestCase {

	public static class ReversedComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return -(((Comparable) o1).compareTo((Comparable) o2));
		}

		public boolean equals(Object o1, Object o2) {
			return (((Comparable) o1).compareTo((Comparable) o2)) == 0;
		}
	}

	TreeMap tm;

	Object objArray[] = new Object[1000];

	/**
	 * @tests java.util.TreeMap#TreeMap()
	 */
	public void test_Constructor() {
		// Test for method java.util.TreeMap()
		new Support_MapTest2(new TreeMap()).runTest();

		assertTrue("New treeMap non-empty", new TreeMap().isEmpty());
	}

	/**
	 * @tests java.util.TreeMap#TreeMap(java.util.Comparator)
	 */
	public void test_ConstructorLjava_util_Comparator() {
		// Test for method java.util.TreeMap(java.util.Comparator)
		Comparator comp = new ReversedComparator();
		TreeMap reversedTreeMap = new TreeMap(comp);
		assertTrue("TreeMap answered incorrect comparator", reversedTreeMap
				.comparator() == comp);
		reversedTreeMap.put(new Integer(1).toString(), new Integer(1));
		reversedTreeMap.put(new Integer(2).toString(), new Integer(2));
		assertTrue("TreeMap does not use comparator (firstKey was incorrect)",
				reversedTreeMap.firstKey().equals(new Integer(2).toString()));
		assertTrue("TreeMap does not use comparator (lastKey was incorrect)",
				reversedTreeMap.lastKey().equals(new Integer(1).toString()));

	}

	/**
	 * @tests java.util.TreeMap#TreeMap(java.util.Map)
	 */
	public void test_ConstructorLjava_util_Map() {
		// Test for method java.util.TreeMap(java.util.Map)
		TreeMap myTreeMap = new TreeMap(new HashMap(tm));
		assertTrue("Map is incorrect size", myTreeMap.size() == objArray.length);
		for (int counter = 0; counter < objArray.length; counter++)
			assertTrue("Map has incorrect mappings", myTreeMap.get(
					objArray[counter].toString()).equals(objArray[counter]));
	}

	/**
	 * @tests java.util.TreeMap#TreeMap(java.util.SortedMap)
	 */
	public void test_ConstructorLjava_util_SortedMap() {
		// Test for method java.util.TreeMap(java.util.SortedMap)
		Comparator comp = new ReversedComparator();
		TreeMap reversedTreeMap = new TreeMap(comp);
		reversedTreeMap.put(new Integer(1).toString(), new Integer(1));
		reversedTreeMap.put(new Integer(2).toString(), new Integer(2));
		TreeMap anotherTreeMap = new TreeMap(reversedTreeMap);
		assertTrue("New tree map does not answer correct comparator",
				anotherTreeMap.comparator() == comp);
		assertTrue("TreeMap does not use comparator (firstKey was incorrect)",
				anotherTreeMap.firstKey().equals(new Integer(2).toString()));
		assertTrue("TreeMap does not use comparator (lastKey was incorrect)",
				anotherTreeMap.lastKey().equals(new Integer(1).toString()));

	}

	/**
	 * @tests java.util.TreeMap#clear()
	 */
	public void test_clear() {
		// Test for method void java.util.TreeMap.clear()
		tm.clear();
		assertTrue("Cleared map returned non-zero size", tm.size() == 0);
	}

	/**
	 * @tests java.util.TreeMap#clone()
	 */
	public void test_clone() {
		// Test for method java.lang.Object java.util.TreeMap.clone()
		TreeMap clonedMap = (TreeMap) tm.clone();
		assertTrue("Cloned map does not equal the original map", clonedMap
				.equals(tm));
		assertTrue("Cloned map is the same reference as the original map",
				clonedMap != tm);
		for (int counter = 0; counter < objArray.length; counter++)
			assertTrue("Cloned map contains incorrect elements", clonedMap
					.get(objArray[counter].toString()) == tm
					.get(objArray[counter].toString()));

		TreeMap map = new TreeMap();
		map.put("key", "value");
		// get the keySet() and values() on the original Map
		Set keys = map.keySet();
		Collection values = map.values();
		assertTrue("values() does not work", values.iterator().next().equals(
				"value"));
		assertTrue("keySet() does not work", keys.iterator().next().equals(
				"key"));
		AbstractMap map2 = (AbstractMap) map.clone();
		map2.put("key", "value2");
		Collection values2 = map2.values();
		assertTrue("values() is identical", values2 != values);
		// values() and keySet() on the cloned() map should be different
		assertTrue("values() was not cloned", values2.iterator().next().equals(
				"value2"));
		map2.clear();
		map2.put("key2", "value3");
		Set key2 = map2.keySet();
		assertTrue("keySet() is identical", key2 != keys);
		assertTrue("keySet() was not cloned", key2.iterator().next().equals(
				"key2"));
	}

	/**
	 * @tests java.util.TreeMap#comparator()
	 */
	public void test_comparator() {
		// Test for method java.util.Comparator java.util.TreeMap.comparator()\
		Comparator comp = new ReversedComparator();
		TreeMap reversedTreeMap = new TreeMap(comp);
		assertTrue("TreeMap answered incorrect comparator", reversedTreeMap
				.comparator() == comp);
		reversedTreeMap.put(new Integer(1).toString(), new Integer(1));
		reversedTreeMap.put(new Integer(2).toString(), new Integer(2));
		assertTrue("TreeMap does not use comparator (firstKey was incorrect)",
				reversedTreeMap.firstKey().equals(new Integer(2).toString()));
		assertTrue("TreeMap does not use comparator (lastKey was incorrect)",
				reversedTreeMap.lastKey().equals(new Integer(1).toString()));
	}

	/**
	 * @tests java.util.TreeMap#containsKey(java.lang.Object)
	 */
	public void test_containsKeyLjava_lang_Object() {
		// Test for method boolean
		// java.util.TreeMap.containsKey(java.lang.Object)
		assertTrue("Returned false for valid key", tm.containsKey("95"));
		assertTrue("Returned true for invalid key", !tm.containsKey("XXXXX"));
	}

	/**
	 * @tests java.util.TreeMap#containsValue(java.lang.Object)
	 */
	public void test_containsValueLjava_lang_Object() {
		// Test for method boolean
		// java.util.TreeMap.containsValue(java.lang.Object)
		assertTrue("Returned false for valid value", tm
				.containsValue(objArray[986]));
		assertTrue("Returned true for invalid value", !tm
				.containsValue(new Object()));
	}

	/**
	 * @tests java.util.TreeMap#entrySet()
	 */
	public void test_entrySet() {
		// Test for method java.util.Set java.util.TreeMap.entrySet()
		Set anEntrySet = tm.entrySet();
		Iterator entrySetIterator = anEntrySet.iterator();
		assertTrue("EntrySet is incorrect size",
				anEntrySet.size() == objArray.length);
		Map.Entry entry;
		while (entrySetIterator.hasNext()) {
			entry = (Map.Entry) entrySetIterator.next();
			assertTrue("EntrySet does not contain correct mappings", tm
					.get(entry.getKey()) == entry.getValue());
		}
	}

	/**
	 * @tests java.util.TreeMap#firstKey()
	 */
	public void test_firstKey() {
		// Test for method java.lang.Object java.util.TreeMap.firstKey()
		assertTrue("Returned incorrect first key", tm.firstKey().equals("0"));
	}

	/**
	 * @tests java.util.TreeMap#get(java.lang.Object)
	 */
	public void test_getLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.TreeMap.get(java.lang.Object)
		Object o = new Object();
		tm.put("Hello", o);
		assertTrue("Failed to get mapping", tm.get("Hello") == o);

	}

	/**
	 * @tests java.util.TreeMap#headMap(java.lang.Object)
	 */
	public void test_headMapLjava_lang_Object() {
		// Test for method java.util.SortedMap
		// java.util.TreeMap.headMap(java.lang.Object)
		Map head = tm.headMap("100");
		assertTrue("Returned map of incorrect size", head.size() == 3);
		assertTrue("Returned incorrect elements", head.containsKey("0")
				&& head.containsValue(new Integer("1"))
				&& head.containsKey("10"));
	}

	/**
	 * @tests java.util.TreeMap#keySet()
	 */
	public void test_keySet() {
		// Test for method java.util.Set java.util.TreeMap.keySet()
		Set ks = tm.keySet();
		assertTrue("Returned set of incorrect size",
				ks.size() == objArray.length);
		for (int i = 0; i < tm.size(); i++)
			assertTrue("Returned set is missing keys", ks.contains(new Integer(
					i).toString()));
	}

	/**
	 * @tests java.util.TreeMap#lastKey()
	 */
	public void test_lastKey() {
		// Test for method java.lang.Object java.util.TreeMap.lastKey()
		assertTrue("Returned incorrect last key", tm.lastKey().equals(
				objArray[objArray.length - 1].toString()));
	}

	/**
	 * @tests java.util.TreeMap#put(java.lang.Object, java.lang.Object)
	 */
	public void test_putLjava_lang_ObjectLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.TreeMap.put(java.lang.Object, java.lang.Object)
		Object o = new Object();
		tm.put("Hello", o);
		assertTrue("Failed to put mapping", tm.get("Hello") == o);

	}

	/**
	 * @tests java.util.TreeMap#putAll(java.util.Map)
	 */
	public void test_putAllLjava_util_Map() {
		// Test for method void java.util.TreeMap.putAll(java.util.Map)
		TreeMap x = new TreeMap();
		x.putAll(tm);
		assertTrue("Map incorrect size after put", x.size() == tm.size());
		for (int i = 0; i < objArray.length; i++)
			assertTrue("Failed to put all elements", x.get(
					objArray[i].toString()).equals(objArray[i]));
	}

	/**
	 * @tests java.util.TreeMap#remove(java.lang.Object)
	 */
	public void test_removeLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.TreeMap.remove(java.lang.Object)
		tm.remove("990");
		assertTrue("Failed to remove mapping", !tm.containsKey("990"));

	}

	/**
	 * @tests java.util.TreeMap#size()
	 */
	public void test_size() {
		// Test for method int java.util.TreeMap.size()
		assertTrue("Returned incorrect size", tm.size() == 1000);
	}

	/**
	 * @tests java.util.TreeMap#subMap(java.lang.Object, java.lang.Object)
	 */
	public void test_subMapLjava_lang_ObjectLjava_lang_Object() {
		// Test for method java.util.SortedMap
		// java.util.TreeMap.subMap(java.lang.Object, java.lang.Object)
		SortedMap subMap = tm.subMap(objArray[100].toString(), objArray[109]
				.toString());
		assertTrue("subMap is of incorrect size", subMap.size() == 9);
		for (int counter = 100; counter < 109; counter++)
			assertTrue("SubMap contains incorrect elements", subMap.get(
					objArray[counter].toString()).equals(objArray[counter]));

		int result;
		try {
			tm.subMap(objArray[9].toString(), objArray[1].toString());
			result = 0;
		} catch (IllegalArgumentException e) {
			result = 1;
		}
		assertTrue(
				"end key less than start key should throw IllegalArgumentException",
				result == 1);
	}

	/**
	 * @tests java.util.TreeMap#tailMap(java.lang.Object)
	 */
	public void test_tailMapLjava_lang_Object() {
		// Test for method java.util.SortedMap
		// java.util.TreeMap.tailMap(java.lang.Object)
		Map tail = tm.tailMap(objArray[900].toString());
		assertTrue("Returned map of incorrect size : " + tail.size(), tail
				.size() == (objArray.length - 900) + 9);
		for (int i = 900; i < objArray.length; i++)
			assertTrue("Map contains incorrect entries", tail
					.containsValue(objArray[i]));
	}

	/**
	 * @tests java.util.TreeMap#values()
	 */
	public void test_values() {
		// Test for method java.util.Collection java.util.TreeMap.values()
		Collection vals = tm.values();
		vals.iterator();
		assertTrue("Returned collection of incorrect size",
				vals.size() == objArray.length);
		for (int i = 0; i < objArray.length; i++)
			assertTrue("Collection contains incorrect elements", vals
					.contains(objArray[i]));

		TreeMap myTreeMap = new TreeMap();
		for (int i = 0; i < 100; i++)
			myTreeMap.put(objArray[i], objArray[i]);
		Collection values = myTreeMap.values();
		new Support_UnmodifiableCollectionTest(
				"Test Returned Collection From TreeMap.values()", values)
				.runTest();
		values.remove(new Integer(0));
		assertTrue(
				"Removing from the values collection should remove from the original map",
				!myTreeMap.containsValue(new Integer(0)));

	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		tm = new TreeMap();
		for (int i = 0; i < objArray.length; i++) {
			Object x = objArray[i] = new Integer(i);
			tm.put(x.toString(), x);
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
