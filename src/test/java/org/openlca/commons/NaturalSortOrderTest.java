package org.openlca.commons;

import static org.junit.Assert.*;
import static org.openlca.commons.NaturalSortOrder.*;

import org.junit.Test;

public class NaturalSortOrderTest {

	@Test
	public void testSimple() {
		assertTrue(compare("101", "11") > 0);
	}
}
