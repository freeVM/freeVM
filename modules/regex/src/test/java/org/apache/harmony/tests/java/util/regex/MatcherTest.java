/*
 *  Copyright 2005, 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.10.2.2 $
 */
package org.apache.harmony.tests.java.util.regex;

import junit.framework.TestCase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.10.2.2 $
 */
public class MatcherTest extends TestCase {
	String[] testPatterns = {
			"(a|b)*abb",
			"(1*2*3*4*)*567",
			"(a|b|c|d)*aab",
			"(1|2|3|4|5|6|7|8|9|0)(1|2|3|4|5|6|7|8|9|0)*",
			"(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ)*",
			"(a|b)*(a|b)*A(a|b)*lice.*",
			"(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|"
					+ "i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*(1|2|3|4|5|6|7|8|9|0)*|while|for|struct|if|do"
	};

	String[] groupPatterns = { "(a|b)*aabb", "((a)|b)*aabb", "((a|b)*)a(abb)",
			"(((a)|(b))*)aabb", "(((a)|(b))*)aa(b)b", "(((a)|(b))*)a(a(b)b)" };

	public void testAppendReplacement() {
		Pattern pat = Pattern.compile("XX");
		Matcher m = pat.matcher("Today is XX-XX-XX ...");
		StringBuffer sb = new StringBuffer();

		for (int i = 0; m.find(); i++) {
			m.appendReplacement(sb, new Integer(i * 10 + i).toString());
		}
		m.appendTail(sb);
		assertEquals("Today is 0-11-22 ...", sb.toString());
	}

	public void testAppendReplacementRef() {
		Pattern p = Pattern.compile("xx (rur|\\$)");
		Matcher m = p.matcher("xx $ equals to xx rur.");
		StringBuffer sb = new StringBuffer();
		for (int i = 1; m.find(); i *= 30) {
			String rep = new Integer(i).toString() + " $1";
			m.appendReplacement(sb, rep);
		}
		m.appendTail(sb);
		assertEquals("1 $ equals to 30 rur.", sb.toString());
	}

	public void testReplaceAll() {
		String input = "aabfooaabfooabfoob";
		String pattern = "a*b";
		Pattern pat = Pattern.compile(pattern);
		Matcher mat = pat.matcher(input);

		assertEquals("-foo-foo-foo-", mat.replaceAll("-"));
	}

	/*
	 * Class under test for Matcher reset(CharSequence)
	 */
	public void testResetCharSequence() {
	}

	public void testAppendSlashes() {
		Pattern p = Pattern.compile("\\\\");
		Matcher m = p.matcher("one\\cat\\two\\cats\\in\\the\\yard");
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "\\\\");
		}
		m.appendTail(sb);
		assertEquals("one\\cat\\two\\cats\\in\\the\\yard", sb.toString());

	}

	public void testReplaceFirst() {
		String input = "zzzdogzzzdogzzz";
		String pattern = "dog";
		Pattern pat = Pattern.compile(pattern);
		Matcher mat = pat.matcher(input);

		assertEquals("zzzcatzzzdogzzz", mat.replaceFirst("cat"));
	}

	public void testPattern() {
		for (String element : testPatterns) {
			Pattern test = Pattern.compile(element);
			assertEquals(test, test.matcher("aaa").pattern());
		}

		for (String element : testPatterns) {
			assertEquals(element, Pattern.compile(element)
					.matcher("aaa").pattern().toString());
		}
	}

	/*
	 * Class under test for Matcher reset()
	 */
	public void testReset() {
	}

	/*
	 * Class under test for String group(int)
	 */
	public void testGroupint() {
		String positiveTestString = "ababababbaaabb";
		String negativeTestString = "gjhfgdsjfhgcbv";

		// test IndexOutOfBoundsException
		// //
		for (int i = 0; i < groupPatterns.length; i++) {
			Pattern test = Pattern.compile(groupPatterns[i]);
			Matcher mat = test.matcher(positiveTestString);
			mat.matches();
			try {
				// groupPattern <index + 1> equals to number of groups
				// of the specified pattern
				// //
				mat.group(i + 2);
				fail("IndexOutBoundsException expected");
				mat.group(i + 100);
				fail("IndexOutBoundsException expected");
				mat.group(-1);
				fail("IndexOutBoundsException expected");
				mat.group(-100);
				fail("IndexOutBoundsException expected");
			} catch (IndexOutOfBoundsException iobe) {
			}
		}

		String[][] groupResults = { { "a" }, { "a", "a" },
				{ "ababababba", "a", "abb" }, { "ababababba", "a", "a", "b" },
				{ "ababababba", "a", "a", "b", "b" },
				{ "ababababba", "a", "a", "b", "abb", "b" }, };

		for (int i = 0; i < groupPatterns.length; i++) {
			Pattern test = Pattern.compile(groupPatterns[i]);
			Matcher mat = test.matcher(positiveTestString);
			mat.matches();
			for (int j = 0; j < groupResults[i].length; j++) {
				assertEquals("i: " + i + " j: " + j, groupResults[i][j], mat
						.group(j + 1));
			}

		}

	}

	public void testGroup() {
		String positiveTestString = "ababababbaaabb";
		String negativeTestString = "gjhfgdsjfhgcbv";
		for (String element : groupPatterns) {
			Pattern test = Pattern.compile(element);
			Matcher mat = test.matcher(positiveTestString);
			mat.matches();
			// test result
			assertEquals(positiveTestString, mat.group());

			// test equal to group(0) result
			assertEquals(mat.group(0), mat.group());
		}

		for (String element : groupPatterns) {
			Pattern test = Pattern.compile(element);
			Matcher mat = test.matcher(negativeTestString);
			mat.matches();
			try {
				mat.group();
				fail("IllegalStateException expected for <false> matches result");
			} catch (IllegalStateException ise) {
			}
		}
	}

	public void testGroupPossessive() {
		Pattern pat = Pattern.compile("((a)|(b))++c");
		Matcher mat = pat.matcher("aac");

		mat.matches();
		assertEquals("a", mat.group(1));
	}

	/*
	 * Class under test for boolean find(int)
	 */
	public void testFindint() {
	}

	/*
	 * Class under test for int start(int)
	 */
	public void testStartint() {
	}

	/*
	 * Class under test for int end(int)
	 */
	public void testEndint() {
	}

	public void testMatchesMisc() {
		String[][] posSeq = {
				{ "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
				{ "213567", "12324567", "1234567", "213213567",
						"21312312312567", "444444567" },
				{ "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
				{ "213234567", "3458", "0987654", "7689546432", "0398576",
						"98432", "5" },
				{
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
								+ "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
				{ "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
						"abbbAbbbliceaaa", "Alice" },
				{ "a123", "bnxnvgds156", "for", "while", "if", "struct" }

		};

		for (int i = 0; i < testPatterns.length; i++) {
			Pattern pat = Pattern.compile(testPatterns[i]);
			for (int j = 0; j < posSeq[i].length; j++) {
				Matcher mat = pat.matcher(posSeq[i][j]);
				assertTrue("Incorrect match: " + testPatterns[i] + " vs "
						+ posSeq[i][j], mat.matches());
			}
		}
	}

	public void testMatchesQuantifiers() {
		String[] testPatternsSingles = { "a{5}", "a{2,4}", "a{3,}" };
		String[] testPatternsMultiple = { "((a)|(b)){1,2}abb",
				"((a)|(b)){2,4}", "((a)|(b)){3,}" };

		String[][] stringSingles = { { "aaaaa", "aaa" },
				{ "aa", "a", "aaa", "aaaaaa", "aaaa", "aaaaa" },
				{ "aaa", "a", "aaaa", "aa" }, };

		String[][] stringMultiples = { { "ababb", "aba" },
				{ "ab", "b", "bab", "ababa", "abba", "abababbb" },
				{ "aba", "b", "abaa", "ba" }, };

		for (int i = 0; i < testPatternsSingles.length; i++) {
			Pattern pat = Pattern.compile(testPatternsSingles[i]);
			for (int j = 0; j < stringSingles.length / 2; j++) {
				assertTrue("Match expected, but failed: " + pat.pattern()
						+ " : " + stringSingles[i][j], pat.matcher(
						stringSingles[i][j * 2]).matches());
				assertFalse("Match failure expected, but match succeed: "
						+ pat.pattern() + " : " + stringSingles[i][j * 2 + 1],
						pat.matcher(stringSingles[i][j * 2 + 1]).matches());
			}
		}

		for (int i = 0; i < testPatternsMultiple.length; i++) {
			Pattern pat = Pattern.compile(testPatternsMultiple[i]);
			for (int j = 0; j < stringMultiples.length / 2; j++) {
				assertTrue("Match expected, but failed: " + pat.pattern()
						+ " : " + stringMultiples[i][j], pat.matcher(
						stringMultiples[i][j * 2]).matches());
				assertFalse(
						"Match failure expected, but match succeed: "
								+ pat.pattern() + " : "
								+ stringMultiples[i][j * 2 + 1], pat.matcher(
								stringMultiples[i][j * 2 + 1]).matches());
			}
		}
	}

	public void testQuantVsGroup() {
		String patternString = "(d{1,3})((a|c)*)(d{1,3})((a|c)*)(d{1,3})";
		String testString = "dacaacaacaaddaaacaacaaddd";

		Pattern pat = Pattern.compile(patternString);
		Matcher mat = pat.matcher(testString);

		mat.matches();
		assertEquals("dacaacaacaaddaaacaacaaddd", mat.group());
		assertEquals("d", mat.group(1));
		assertEquals("acaacaacaa", mat.group(2));
		assertEquals("dd", mat.group(4));
		assertEquals("aaacaacaa", mat.group(5));
		assertEquals("ddd", mat.group(7));
	}

	public void testLookingAt() {
	}

	/*
	 * Class under test for boolean find()
	 */
	public void testFind() {
		String testPattern = "(abb)";
		String testString = "cccabbabbabbabbabb";
		Pattern pat = Pattern.compile(testPattern);
		Matcher mat = pat.matcher(testString);
		int start = 3;
		int end = 6;
		while (mat.find()) {
			assertEquals(start, mat.start(1));
			assertEquals(end, mat.end(1));

			start = end;
			end += 3;
		}

		testPattern = "(\\d{1,3})";
		testString = "aaaa123456789045";

		Pattern pat2 = Pattern.compile(testPattern);
		Matcher mat2 = pat2.matcher(testString);
		start = 4;
		int length = 3;
		while (mat2.find()) {
			assertEquals(testString.substring(start, start + length), mat2
					.group(1));
			start += length;
		}
	}

	public void testSEOLsymbols() {
		Pattern pat = Pattern.compile("^a\\(bb\\[$");
		Matcher mat = pat.matcher("a(bb[");

		assertTrue(mat.matches());
	}

	/*
	 * Class under test for int start()
	 */
	public void testStart() {
	}

	public void testGroupCount() {
		for (int i = 0; i < groupPatterns.length; i++) {
			Pattern test = Pattern.compile(groupPatterns[i]);
			Matcher mat = test.matcher("ababababbaaabb");
			mat.matches();
			assertEquals(i + 1, mat.groupCount());

		}
	}

	public void testRelactantQuantifiers() {
		Pattern pat = Pattern.compile("(ab*)*b");
		Matcher mat = pat.matcher("abbbb");

		if (mat.matches()) {
			assertEquals("abbb", mat.group(1));
		} else {
			fail("Match expected: (ab*)*b vs abbbb");
		}
	}

	public void testEnhancedFind() {
		String input = "foob";
		String pattern = "a*b";
		Pattern pat = Pattern.compile(pattern);
		Matcher mat = pat.matcher(input);

		mat.find();
		assertEquals("b", mat.group());
	}

	public void _testMatchesURI() {
		final Pattern pat = Pattern
				.compile("^(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
		Runnable r1 = new Runnable() {
			public void run() {
				Matcher mat = pat
						.matcher("file:/c:/workspace/api/build.win32/classes/META-INF/"
								+ "services/javax.xml.parsers.DocumentBuilderFactory");
				int k1 = 1;
				while (mat.matches()) {
				}// System.err.println("1: " + mat.group());
				System.out.println("1: fail");
			}
		};

		Runnable r2 = new Runnable() {
			public void run() {
				Matcher mat = pat.matcher("file:/c:/workspace/"
						+ "services/javax.xml.parsers.DocumentBuilderFactory");
				int k1 = 1;
				while (mat.matches()) {
				}// System.err.println("2: " + mat.group());
				System.out.println("2: fail");
			}
		};

		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);

		t1.start();
		t2.start();

		try {
			t1.join();
			t2.join();
		} catch (Exception e) {
		}

	}

	public void _testUnifiedQuantifiers() {
		// Pattern pat1 = Pattern.compile("\\s+a");
		Pattern pat2 = Pattern.compile(" *a");

		Matcher mat = pat2.matcher("      a");

		System.out.println("unified: " + mat.find());
	}

	public void _testCompositeGroupQuantifiers() {
		Pattern pat = Pattern.compile("(a|b){0,3}abb");
		Matcher mat = pat.matcher("ababababababababababaab");

		System.out.println("composite: " + mat.find());
	}

	public void testPosCompositeGroup() {
		String[] posExamples = { "aabbcc", "aacc", "bbaabbcc" };
		String[] negExamples = { "aabb", "bb", "bbaabb" };
		Pattern posPat = Pattern.compile("(aa|bb){1,3}+cc");
		Pattern negPat = Pattern.compile("(aa|bb){1,3}+bb");

		Matcher mat;
		for (String element : posExamples) {
			mat = posPat.matcher(element);
			assertTrue(mat.matches());
		}

		for (String element : negExamples) {
			mat = negPat.matcher(element);
			assertFalse(mat.matches());
		}

		assertTrue(Pattern.matches("(aa|bb){1,3}+bb", "aabbaabb"));

	}

	public void testPosAltGroup() {
		String[] posExamples = { "aacc", "bbcc", "cc" };
		String[] negExamples = { "bb", "aa" };
		Pattern posPat = Pattern.compile("(aa|bb)?+cc");
		Pattern negPat = Pattern.compile("(aa|bb)?+bb");

		Matcher mat;
		for (String element : posExamples) {
			mat = posPat.matcher(element);
			assertTrue(posPat.toString() + " vs: " + element, mat
					.matches());
		}

		for (String element : negExamples) {
			mat = negPat.matcher(element);
			assertFalse(mat.matches());
		}

		assertTrue(Pattern.matches("(aa|bb)?+bb", "aabb"));
	}

	public void testRelCompGroup() {

		Matcher mat;
		Pattern pat;
		String res = "";
		for (int i = 0; i < 4; i++) {
			pat = Pattern.compile("((aa|bb){" + i + ",3}?).*cc");
			mat = pat.matcher("aaaaaacc");
			assertTrue(pat.toString() + " vs: " + "aaaaaacc", mat.matches());
			assertEquals(res, mat.group(1));
			res += "aa";
		}
	}

	public void testRelAltGroup() {

		Matcher mat;
		Pattern pat;

		pat = Pattern.compile("((aa|bb)??).*cc");
		mat = pat.matcher("aacc");
		assertTrue(pat.toString() + " vs: " + "aacc", mat.matches());
		assertEquals("", mat.group(1));

		pat = Pattern.compile("((aa|bb)??)cc");
		mat = pat.matcher("aacc");
		assertTrue(pat.toString() + " vs: " + "aacc", mat.matches());
		assertEquals("aa", mat.group(1));
	}

	public void testIgnoreCase() {
		Pattern pat = Pattern.compile("(aa|bb)*", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher("aAbb");

		assertTrue(mat.matches());

		pat = Pattern.compile("(a|b|c|d|e)*", Pattern.CASE_INSENSITIVE);
		mat = pat.matcher("aAebbAEaEdebbedEccEdebbedEaedaebEbdCCdbBDcdcdADa");
		assertTrue(mat.matches());

		pat = Pattern.compile("[a-e]*", Pattern.CASE_INSENSITIVE);
		mat = pat.matcher("aAebbAEaEdebbedEccEdebbedEaedaebEbdCCdbBDcdcdADa");
		assertTrue(mat.matches());

	}

	public void testQuoteReplacement() {
		assertEquals("\\\\aaCC\\$1", Matcher.quoteReplacement("\\aaCC$1"));
	}

	public void testOverFlow() {
		Pattern tp = Pattern.compile("(a*)*");
		Matcher tm = tp.matcher("aaa");
		assertTrue(tm.matches());
		assertEquals("", tm.group(1));

		assertTrue(Pattern.matches("(1+)\\1+", "11"));
		assertTrue(Pattern.matches("(1+)(2*)\\2+", "11"));

		Pattern pat = Pattern.compile("(1+)\\1*");
		Matcher mat = pat.matcher("11");

		assertTrue(mat.matches());
		assertEquals("11", mat.group(1));

		pat = Pattern.compile("((1+)|(2+))(\\2+)");
		mat = pat.matcher("11");

		assertTrue(mat.matches());
		assertEquals("1", mat.group(2));
		assertEquals("1", mat.group(1));
		assertEquals("1", mat.group(4));
		assertEquals(null, mat.group(3));

	}

	public void testUnicode() {

		assertTrue(Pattern.matches("\\x61a", "aa"));
		assertTrue(Pattern.matches("\\u0061a", "aa"));
		assertTrue(Pattern.matches("\\0141a", "aa"));
		assertTrue(Pattern.matches("\\0777", "?7"));

	}

	public void testUnicodeCategory() {
		assertTrue(Pattern.matches("\\p{Ll}", "k")); // Unicode lower case
		assertTrue(Pattern.matches("\\P{Ll}", "K")); // Unicode non-lower
														// case
		assertTrue(Pattern.matches("\\p{Lu}", "K")); // Unicode upper case
		assertTrue(Pattern.matches("\\P{Lu}", "k")); // Unicode non-upper
														// case
		// combinations
		assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Lu}]]", "k"));
		assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Ll}]]", "K"));
		assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Lu}]]", "K"));
		assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Ll}]]", "k"));

		// category/character combinations
		assertFalse(Pattern.matches("[\\p{L}&&[^a-z]]", "k"));
		assertTrue(Pattern.matches("[\\p{L}&&[^a-z]]", "K"));

		assertTrue(Pattern.matches("[\\p{Lu}a-z]", "k"));
		assertTrue(Pattern.matches("[a-z\\p{Lu}]", "k"));

		assertFalse(Pattern.matches("[\\p{Lu}a-d]", "k"));
		assertTrue(Pattern.matches("[a-d\\p{Lu}]", "K"));

		assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Lu}&&[^K]]]", "K"));
		assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Lu}&&[^G]]]", "K"));

	}

	public void testSplitEmpty() {

		Pattern pat = Pattern.compile("");
		String[] s = pat.split("", -1);

		assertEquals(1, s.length);
		assertEquals("", s[0]);
	}

	public void testFindDollar() {
		Matcher mat = Pattern.compile("a$").matcher("a\n");
		assertTrue(mat.find());
		assertEquals("a", mat.group());
	}

	/*
	 * Verify if the Matcher can match the input when region is changed
	 */
	public void testMatchesRegionChanged() {
		// Regression for HARMONY-610
		String input = " word ";
		Pattern pattern = Pattern.compile("\\w+");
		Matcher matcher = pattern.matcher(input);
		matcher.region(1, 5);
		assertTrue(matcher.matches());
	}

	/*
	 * Verify if the Matcher behaves correct when region is changed
	 */
	public void testFindRegionChanged() {
		// Regression for HARMONY-625
		Pattern pattern = Pattern.compile("(?s).*");
		Matcher matcher = pattern.matcher("abcde");
		matcher.find();
		assertEquals("abcde", matcher.group());

		matcher = pattern.matcher("abcde");
		matcher.region(0, 2);
		matcher.find();
		assertEquals("ab", matcher.group());

	}

	/*
	 * Regression test for HARMONY-674
	 */
    public void testPatternMatcher() throws Exception {
        Pattern pattern = Pattern.compile("(?:\\d+)(?:pt)");
        assertTrue(pattern.matcher("14pt").matches());
    } 

	public static void main(String[] args) {
		junit.textui.TestRunner.run(MatcherTest.class);
	}
}
