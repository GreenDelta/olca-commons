package org.openlca.commons;

/// Implements the natural sort order of strings, see
/// [Strings#compareNatural(String, String)].
///
/// The strings are compared token by token, where a token is either a maximal
/// sequence of digits (a number) or a single other character. Two numbers are
/// compared by their numerical value, without converting them to a primitive
/// number type, so that numbers of arbitrary length are supported. If two
/// numbers have the same value, the one with fewer leading zeros comes first
/// (e.g. `1 < 01`). All other characters are compared case insensitively by
/// their (lower case) character code. A number token is compared with another
/// character via its first digit; as digits have consecutive character codes,
/// this gives the same result for all numbers. `null` comes before all other
/// strings, followed by blank strings (empty or only whitespace), which are
/// equal to each other. This is consistent with
/// [Strings#compareIgnoreCase(String, String)], where `null` also comes first.
class NaturalSortOrder {

	public static int compare(String a, String b) {
		if (a == null && b == null) return 0;
		if (a == null) return -1;
		if (b == null) return 1;

		boolean blankA = Strings.isBlank(a);
		boolean blankB = Strings.isBlank(b);
		if (blankA && blankB) return 0;
		if (blankA) return -1;
		if (blankB) return 1;

		int lenA = a.length();
		int lenB = b.length();
		int i = 0;
		int j = 0;

		while (i < lenA && j < lenB) {
			char ca = Character.toLowerCase(a.charAt(i));
			char cb = Character.toLowerCase(b.charAt(j));

			// both positions are at the start of a digit sequence because
			// digit sequences are always consumed as a whole
			if (isDigit(ca) && isDigit(cb)) {
				int endA = endOfDigits(a, i);
				int endB = endOfDigits(b, j);
				int c = compareNumbers(a, i, endA, b, j, endB);
				if (c != 0)
					return c;
				i = endA;
				j = endB;
				continue;
			}

			if (ca != cb)
				return Character.compare(ca, cb);
			i++;
			j++;
		}

		boolean endA = i >= lenA;
		boolean endB = j >= lenB;
		if (endA && endB) return 0;
		return endA ? -1 : 1;
	}

	/// Compares the digit sequences `a[startA, endA)` and `b[startB, endB)` by
	/// their numerical value. If the values are equal, the sequence with fewer
	/// leading zeros comes first.
	private static int compareNumbers(
			String a, int startA, int endA, String b, int startB, int endB) {

		int sigA = skipZeros(a, startA, endA);
		int sigB = skipZeros(b, startB, endB);

		// without leading zeros, the number with more digits is larger
		int sigLenA = endA - sigA;
		int sigLenB = endB - sigB;
		if (sigLenA != sigLenB)
			return Integer.compare(sigLenA, sigLenB);

		// same number of digits: the first different digit decides
		for (int k = 0; k < sigLenA; k++) {
			char da = a.charAt(sigA + k);
			char db = b.charAt(sigB + k);
			if (da != db)
				return Character.compare(da, db);
		}

		// same value: fewer leading zeros come first
		return Integer.compare(endA - startA, endB - startB);
	}

	/// Returns the position of the first character that is not a digit, starting
	/// from the given position, or the length of the string.
	private static int endOfDigits(String s, int start) {
		int pos = start;
		while (pos < s.length() && isDigit(s.charAt(pos))) {
			pos++;
		}
		return pos;
	}

	/// Returns the position of the first non-zero digit in `s[start, end)`, or
	/// `end` if this range contains only zeros.
	private static int skipZeros(String s, int start, int end) {
		int pos = start;
		while (pos < end && s.charAt(pos) == '0') {
			pos++;
		}
		return pos;
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
}
