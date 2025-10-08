package org.openlca.commons;

public class Strings {

	private Strings() {
	}

	/// Compares the given strings case-insensitively, handling `null` values.
	public static int compareIgnoreCase(String a, String b) {
		if (a == null && b == null)
			return 0;
		if (a == null)
			return -1;
		if (b == null)
			return 1;
		return a.compareToIgnoreCase(b);
	}

	/// Returns `true` if the given string is `null` or blank.
	public boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	public boolean isNotBlank(String s) {
		return s != null && !s.isBlank();
	}

}
