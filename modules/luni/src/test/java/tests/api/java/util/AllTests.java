/* Copyright 2004, 2006 The Apache Software Foundation or its licensors, as applicable
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TODO Type description
 */
public class AllTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for java.util");
		// $JUnit-BEGIN$
		suite.addTestSuite(ConcurrentModTest.class);

		suite.addTestSuite(AbstractCollectionTest.class);
		suite.addTestSuite(AbstractListTest.class);
		suite.addTestSuite(AbstractMapTest.class);
		suite.addTestSuite(ArrayListTest.class);
		suite.addTestSuite(ArraysTest.class);
		suite.addTestSuite(BitSetTest.class);
		suite.addTestSuite(CalendarTest.class);
		suite.addTestSuite(CollectionsTest.class);
		suite.addTestSuite(ConcurrentModificationExceptionTest.class);
		suite.addTestSuite(CurrencyTest.class);
		suite.addTestSuite(DateTest.class);
		suite.addTestSuite(DuplicateFormatFlagsExceptionTest.class);
		suite.addTestSuite(EmptyStackExceptionTest.class);
		suite.addTestSuite(EventObjectTest.class);
		suite.addTestSuite(FormatFlagsConversionMismatchExceptionTest.class);
        suite.addTestSuite(FormattableFlagsTest.class);
        suite.addTestSuite(FormatterClosedExceptionTest.class);
		suite.addTestSuite(GregorianCalendarTest.class);
		suite.addTestSuite(HashMapTest.class);
		suite.addTestSuite(HashSetTest.class);
		suite.addTestSuite(HashtableTest.class);
		suite.addTestSuite(IdentityHashMapTest.class);
		suite.addTestSuite(IdentityHashMap2Test.class);
		suite.addTestSuite(IllegalFormatCodePointExceptionTest.class);
		suite.addTestSuite(IllegalFormatConversionExceptionTest.class);
		suite.addTestSuite(IllegalFormatFlagsExceptionTest.class);
		suite.addTestSuite(IllegalFormatPrecisionExceptionTest.class);
		suite.addTestSuite(IllegalFormatWidthExceptionTest.class);
		suite.addTestSuite(LinkedHashMapTest.class);
		suite.addTestSuite(LinkedHashSetTest.class);
		suite.addTestSuite(LinkedListTest.class);
		suite.addTestSuite(ListResourceBundleTest.class);
		suite.addTestSuite(LocaleTest.class);
		suite.addTestSuite(MissingFormatArgumentExceptionTest.class);
		suite.addTestSuite(MissingFormatWidthExceptionTest.class);
		suite.addTestSuite(MissingResourceExceptionTest.class);
		suite.addTestSuite(NoSuchElementExceptionTest.class);
		suite.addTestSuite(ObservableTest.class);
		suite.addTestSuite(PropertiesTest.class);
		suite.addTestSuite(PropertyPermissionTest.class);
		suite.addTestSuite(PropertyResourceBundleTest.class);
		suite.addTestSuite(RandomTest.class);
		suite.addTestSuite(ResourceBundleTest.class);
		suite.addTestSuite(SimpleTimeZoneTest.class);
		suite.addTestSuite(StackTest.class);
		suite.addTestSuite(StringTokenizerTest.class);
		suite.addTestSuite(TimerTest.class);
		suite.addTestSuite(TimerTaskTest.class);
		suite.addTestSuite(TimeZoneTest.class);
		suite.addTestSuite(TooManyListenersExceptionTest.class);
		suite.addTestSuite(TreeMapTest.class);
		suite.addTestSuite(TreeSetTest.class);
		suite.addTestSuite(UnknownFormatConversionExceptionTest.class);
		suite.addTestSuite(UnknownFormatFlagsExceptionTest.class);
		suite.addTestSuite(VectorTest.class);
		suite.addTestSuite(WeakHashMapTest.class);
		// $JUnit-END$

		return suite;
	}
}
