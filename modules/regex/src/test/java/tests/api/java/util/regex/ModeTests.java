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

package tests.api.java.util.regex;

import junit.framework.TestCase;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Tests Pattern compilation modes and modes triggered in pattern strings
 * 
 */
public class ModeTests extends TestCase {
	public void testCase() {
		Pattern p;
		Matcher m;

		try {
			p = Pattern.compile("([a-z]+)[0-9]+");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("cAT123#dog345");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("dog"));
		assertFalse(m.find());

		try {
			p = Pattern.compile("([a-z]+)[0-9]+", Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("cAt123#doG345");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("cAt"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("doG"));
		assertFalse(m.find());

		try {
			p = Pattern.compile("(?i)([a-z]+)[0-9]+");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("cAt123#doG345");
		assertTrue(m.find());
		System.out.println(m.group());
		System.out.println(m.group(1));
		assertTrue(m.group(1).equals("cAt"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("doG"));
		assertFalse(m.find());
	}

	public void testMultiline() {
		Pattern p;
		Matcher m;

		try {
			p = Pattern.compile("^foo");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foobar");
		assertTrue(m.find());
		assertTrue(m.start() == 0 && m.end() == 3);
		assertFalse(m.find());

		m = p.matcher("barfoo");
		assertFalse(m.find());

		try {
			p = Pattern.compile("foo$");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foobar");
		assertFalse(m.find());

		m = p.matcher("barfoo");
		assertTrue(m.find());
		assertTrue(m.start() == 3 && m.end() == 6);
		assertFalse(m.find());

		try {
			p = Pattern.compile("^foo([0-9]*)", Pattern.MULTILINE);
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("1"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("2"));
		assertFalse(m.find());

		try {
			p = Pattern.compile("foo([0-9]*)$", Pattern.MULTILINE);
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("3"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("4"));
		assertFalse(m.find());

		try {
			p = Pattern.compile("(?m)^foo([0-9]*)");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("1"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("2"));
		assertFalse(m.find());

		try {
			p = Pattern.compile("(?m)foo([0-9]*)$");
		} catch (PatternSyntaxException e) {
			assertFalse(true);
			return;
		}

		m = p.matcher("foo1bar\nfoo2foo3\nbarfoo4");
		assertTrue(m.find());
		assertTrue(m.group(1).equals("3"));
		assertTrue(m.find());
		assertTrue(m.group(1).equals("4"));
		assertFalse(m.find());
	}
}
