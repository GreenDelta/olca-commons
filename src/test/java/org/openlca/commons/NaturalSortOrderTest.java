package org.openlca.commons;

import static org.junit.Assert.*;
import static org.openlca.commons.NaturalSortOrder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class NaturalSortOrderTest {

	@Test
	public void testNumericalValues() {
		assertTrue(compare("11", "101") < 0);
		assertTrue(compare("101", "11") > 0);
		assertTrue(compare("a2", "a10") < 0);
		assertTrue(compare("a10", "a2") > 0);
		assertTrue(compare("photo2.jpg", "photo11.jpg") < 0);
	}

	@Test
	public void testLeadingZeros() {
		assertTrue(compare("1", "01") < 0);
		assertTrue(compare("01", "001") < 0);
		assertTrue(compare("001", "01") > 0);
		assertTrue(compare("a1", "a01") < 0);
		assertTrue(compare("a01", "a001") < 0);

		assertTrue(compare("01", "11") < 0);
		assertTrue(compare("11", "002") > 0);
	}

	@Test
	public void testEquality() {
		assertEquals(0, compare("a10b", "a10b"));
		assertEquals(0, compare("123", "123"));
		assertEquals(0, compare("a10b", "A10B"));
		assertEquals(0, compare("Doc12.Txt", "doc12.txt"));
	}

	@Test
	public void testAlphabetical() {
		assertTrue(compare("abc", "def") < 0);
		assertTrue(compare("A", "b") < 0);
		assertTrue(compare("a", "1") > 0);
	}

	@Test
	public void testSize() {
		assertTrue(compare("a", "ab") < 0);
		assertTrue(compare("12", "123") < 0);
		assertTrue(compare("a10", "a10b") < 0);
	}

	@Test
	public void testBlank() {
		assertTrue(compare("", "a") < 0);
		assertTrue(compare("   ", "1") < 0);
		assertEquals(0, compare("", "   "));
		assertEquals(0, compare(null, ""));
	}

	/// A digit sequence must always be compared as a whole number, also when
	/// the strings share a common digit prefix and differ in a digit and a
	/// non-digit character. Otherwise, we get cycles like
	/// `a12 < a1b < a2 < a12`.
	@Test
	public void testDigitSequencesAreComparedAsWhole() {
		assertTrue(compare("a1b", "a12") < 0);
		assertTrue(compare("a12", "a1b") > 0);
		assertTrue(compare("a1b", "a2") < 0);
		assertTrue(compare("a2", "a12") < 0);

		assertTrue(compare("a1z", "a10") < 0);
		assertTrue(compare("a10", "a1z") > 0);

		assertTrue(compare("Process 1x", "Process 15") < 0);
		assertTrue(compare("Process 1x", "Process 2") < 0);
		assertTrue(compare("Process 2", "Process 15") < 0);

		assertTrue(compare("PM2.5", "PM10") < 0);
		assertTrue(compare("EN 1580", "EN 15804") < 0);
	}

	/// Numbers that do not fit into an `int` (or `long`) must still be compared
	/// by their numerical value.
	@Test
	public void testLargeNumbers() {
		assertTrue(compare("x2", "x3000000000") < 0);
		assertTrue(compare("x3000000000", "x2") > 0);
		assertTrue(compare("x1", "x9999999999") < 0);
		assertTrue(compare("x9999999999", "x1") > 0);

		// 2^32 + 1 would wrap around to 1 in an int
		assertTrue(compare("x2", "x4294967297") < 0);
		assertTrue(compare("x4294967297", "x2") > 0);

		// larger than Long.MAX_VALUE
		assertTrue(compare(
				"x99999999999999999999", "x100000000000000000000") < 0);
		assertTrue(compare(
				"x123456789012345678901234567890",
				"x123456789012345678901234567891") < 0);
		assertEquals(0, compare(
				"x123456789012345678901234567890",
				"X123456789012345678901234567890"));
	}

	/// Checks the comparator contract exhaustively for all strings up to a
	/// length of 3 over an alphabet that contains letters in both cases,
	/// digits, and separators:
	///
	/// - `sgn(compare(a, b)) == -sgn(compare(b, a))`
	/// - `compare(a, b) > 0 && compare(b, c) > 0` implies `compare(a, c) > 0`
	/// - `compare(a, b) == 0` implies `sgn(compare(a, c)) == sgn(compare(b, c))`
	@Test
	public void testContractExhaustive() {
		var strings = allStrings("aB012- ", 3);
		int n = strings.size();
		var sgn = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				sgn[i][j] = Integer.signum(compare(strings.get(i), strings.get(j)));
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (sgn[i][j] != -sgn[j][i]) {
					fail("not symmetric: " + pair(strings, i, j, sgn));
				}
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					boolean violated = sgn[i][j] == 0
							? sgn[i][k] != sgn[j][k]
							: sgn[i][j] == sgn[j][k] && sgn[i][k] != sgn[i][j];
					if (violated) {
						fail("not transitive: "
								+ pair(strings, i, j, sgn) + ", "
								+ pair(strings, j, k, sgn) + ", "
								+ pair(strings, i, k, sgn));
					}
				}
			}
		}
	}

	/// Sorts lists of random, process-like names with `List.sort` (TimSort,
	/// which throws an exception when it detects a contract violation) and
	/// checks that the result is ordered for every pair of elements.
	@Test
	public void testSortRandomNames() {
		String[] parts = {
				"1", "2", "3", "10", "12", "15", "01", "3000000000",
				"1a", "1b", "2x", "x", "a", "B", " ", ".", "-", "_", "(", ")"};
		for (int seed = 0; seed < 50; seed++) {
			var rand = new Random(seed);
			var names = new ArrayList<String>();
			for (int i = 0; i < 400; i++) {
				var name = new StringBuilder("Process ");
				int k = 1 + rand.nextInt(4);
				for (int j = 0; j < k; j++) {
					name.append(parts[rand.nextInt(parts.length)]);
				}
				names.add(name.toString());
			}

			try {
				names.sort(NaturalSortOrder::compare);
			} catch (IllegalArgumentException e) {
				fail("sorting failed for seed " + seed + ": " + e.getMessage());
			}

			for (int i = 0; i < names.size(); i++) {
				for (int j = i + 1; j < names.size(); j++) {
					var a = names.get(i);
					var b = names.get(j);
					if (compare(a, b) > 0) {
						fail("seed " + seed + ": sorted list contains '"
								+ a + "' before '" + b + "'");
					}
				}
			}
		}
	}

	private static List<String> allStrings(String alphabet, int maxLength) {
		var strings = new ArrayList<String>();
		strings.add("");
		int start = 0;
		for (int len = 1; len <= maxLength; len++) {
			int end = strings.size();
			for (int i = start; i < end; i++) {
				for (char c : alphabet.toCharArray()) {
					strings.add(strings.get(i) + c);
				}
			}
			start = end;
		}
		return strings;
	}

	private static String pair(List<String> strings, int i, int j, int[][] sgn) {
		var op = switch (sgn[i][j]) {
			case -1 -> "<";
			case 1 -> ">";
			default -> "=";
		};
		return "'" + strings.get(i) + "' " + op + " '" + strings.get(j) + "'";
	}
}
