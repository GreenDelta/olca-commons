package org.openlca.commons;

/// A utility class for string comparison in natural sort order.
/// See [compareNatural](https://api.flutter.dev/flutter/package-collection_collection/compareNatural.html)
public class NaturalSortOrder {

	private static final int ZERO = 0x30;

	private NaturalSortOrder() {
	}

	/**
	 * Compares strings a and b according to natural sort ordering.
	 * <p>
	 * A natural sort ordering is a lexical ordering where embedded numerals
	 * (digit sequences) are treated as a single unit and ordered by numerical value.
	 * <p>
	 * Example: "a", "a0", "a0b", "a1", "a01", "a9", "a10", "a100", "a100b", "aa"
	 */
	public static int compare(String a, String b) {
		for (int i = 0; i < a.length(); i++) {
			if (i >= b.length()) return 1;
			char aChar = a.charAt(i);
			char bChar = b.charAt(i);
			if (aChar != bChar) {
				return compareNaturally(a, b, i, aChar, bChar);
			}
		}
		if (b.length() > a.length()) return -1;
		return 0;
	}

	/**
	 * Checks for numbers overlapping the current mismatched characters.
	 */
	private static int compareNaturally(String a, String b, int index, int aChar, int bChar) {
		assert aChar != bChar;
		boolean aIsDigit = isDigit(aChar);
		boolean bIsDigit = isDigit(bChar);

		if (aIsDigit) {
			if (bIsDigit) {
				return compareNumerically(a, b, aChar, bChar, index);
			} else if (index > 0 && isDigit(a.charAt(index - 1))) {
				// aChar is the continuation of a longer number
				return 1;
			}
		} else if (bIsDigit && index > 0 && isDigit(b.charAt(index - 1))) {
			// bChar is the continuation of a longer number
			return -1;
		}
		// Characters are both non-digits, or not continuation of earlier number
		return Integer.signum(aChar - bChar);
	}

	/**
	 * Compare numbers overlapping aChar and bChar numerically.
	 */
	private static int compareNumerically(String a, String b, int aChar, int bChar, int index) {
		// Both are digits. Find the first significant different digit
		if (isNonZeroNumberSuffix(a, index)) {
			// Part of a longer number, differs at this index
			int result = compareDigitCount(a, b, index, index);
			if (result != 0) return result;
			return Integer.signum(aChar - bChar);
		}

		// Not part of larger (non-zero) number, so skip leading zeros
		int aIndex = index;
		int bIndex = index;

		if (aChar == ZERO) {
			do {
				aIndex++;
				if (aIndex == a.length()) return -1;
				aChar = a.charAt(aIndex);
			} while (aChar == ZERO);
			if (!isDigit(aChar)) return -1;
		} else if (bChar == ZERO) {
			do {
				bIndex++;
				if (bIndex == b.length()) return 1;
				bChar = b.charAt(bIndex);
			} while (bChar == ZERO);
			if (!isDigit(bChar)) return 1;
		}

		if (aChar != bChar) {
			int result = compareDigitCount(a, b, aIndex, bIndex);
			if (result != 0) return result;
			return Integer.signum(aChar - bChar);
		}

		// Same leading digit, one had more leading zeros
		while (true) {
			boolean aIsDigit = false;
			boolean bIsDigit = false;
			aChar = 0;
			bChar = 0;

			if (++aIndex < a.length()) {
				aChar = a.charAt(aIndex);
				aIsDigit = isDigit(aChar);
			}
			if (++bIndex < b.length()) {
				bChar = b.charAt(bIndex);
				bIsDigit = isDigit(bChar);
			}

			if (aIsDigit) {
				if (bIsDigit) {
					if (aChar == bChar) continue;
					break;
				}
				return 1;
			} else if (bIsDigit) {
				return -1;
			} else {
				// Neither is digit, numbers had same numerical value
				return Integer.signum(aIndex - bIndex);
			}
		}

		// At first differing digits
		int result = compareDigitCount(a, b, aIndex, bIndex);
		if (result != 0) return result;
		return Integer.signum(aChar - bChar);
	}

	/**
	 * Checks which of a and b has the longest sequence of digits.
	 * Starts counting from i+1 and j+1 (assumes that a[i] and b[j] are both digits).
	 */
	private static int compareDigitCount(String a, String b, int i, int j) {
		while (++i < a.length()) {
			boolean aIsDigit = isDigit(a.charAt(i));
			if (++j == b.length()) return aIsDigit ? 1 : 0;
			boolean bIsDigit = isDigit(b.charAt(j));
			if (aIsDigit) {
				if (bIsDigit) continue;
				return 1;
			} else if (bIsDigit) {
				return -1;
			} else {
				return 0;
			}
		}
		if (++j < b.length() && isDigit(b.charAt(j))) {
			return -1;
		}
		return 0;
	}

	/**
	 * Efficient digit check: (c ^ '0') <= 9 works because ASCII digits are 0x30-0x39.
	 */
	private static boolean isDigit(int charCode) {
		return (charCode ^ ZERO) <= 9;
	}

	/**
	 * Check if the digit at index is continuing a non-zero number.
	 */
	private static boolean isNonZeroNumberSuffix(String string, int index) {
		while (--index >= 0) {
			char c = string.charAt(index);
			if (c != ZERO) return isDigit(c);
		}
		return false;
	}

}
