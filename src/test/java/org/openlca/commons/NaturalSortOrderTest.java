package org.openlca.commons;

import static org.junit.Assert.*;
import static org.openlca.commons.NaturalSortOrder.*;

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
}
