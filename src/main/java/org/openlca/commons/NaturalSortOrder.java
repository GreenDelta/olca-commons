package org.openlca.commons;

class NaturalSortOrder {

	private static final int ZERO = 0x30;

	private record Num(int value, int length) {
	}

	public static int compare(String a, String b) {
		boolean blankA = Strings.isBlank(a);
		boolean blankB = Strings.isBlank(b);
		if (blankA && blankB) return 0;
		if (blankA) return -1;
		if (blankB) return 1;

		int lenA = a.length();
		int lenB = b.length();
		int minLen = Math.min(lenA, lenB);

		for (int i = 0; i < minLen; i++) {
			char ca = Character.toLowerCase(a.charAt(i));
			char cb = Character.toLowerCase(b.charAt(i));
			if (ca == cb)
				continue;

			if (isDigit(ca) && isDigit(cb)) {
				var numA = eatDigits(a, i);
				var numB = eatDigits(b, i);
				if (numA.value != numB.value)
					return numA.value - numB.value;
				if (numA.length != numB.length)
					return numA.length - numB.length;
			}
			return ca - cb;
		}

		return Integer.compare(lenA, lenB);
	}

	private static Num eatDigits(String s, int start) {
		int num = 0;
		int pos = 0;
		int len = s.length();
		for (int i = start; i < len; i++) {
			int n = s.charAt(i) ^ ZERO;
			if (n > 9) break;
			num = num * 10 + n;
			pos++;
		}
		return new Num(num, pos);
	}

	private static boolean isDigit(int charCode) {
		return (charCode ^ ZERO) <= 9;
	}
}
