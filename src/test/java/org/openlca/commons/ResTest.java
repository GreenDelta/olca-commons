package org.openlca.commons;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResTest {

	@Test
	public void testValue() {
		var res = Res.ok(42);
		assertEquals(Integer.valueOf(42), res.value());
		assertFalse(res.isEmpty());
		assertFalse(res.isError());
		assertTrue(res.isOk());
		assertEquals("42", res.toString());
	}

	@Test
	public void testEmpty() {
		var res = Res.ok();
		assertTrue(res.isEmpty());
		assertFalse(res.isError());
		assertTrue(res.isOk());
		assertEquals("ok!", res.toString());
	}

	@Test
	public void testError() {
		var res = Res.error("Some error");
		assertFalse(res.isEmpty());
		assertTrue(res.isError());
		assertFalse(res.isOk());
		assertEquals("Error: Some error", res.toString());
	}

	@Test
	public void testErrorWrapping() {
		Res<Integer> resI = Res.error("Integer error");
		Res<Double> resD = resI.wrapError("Double error");
		Res<String> resS = resD.castError();
		assertFalse(resS.isEmpty());
		assertTrue(resS.isError());
		assertFalse(resS.isOk());
		assertEquals("Error: Double error\n  -> Integer error", resD.toString());
	}

	@Test
	public void testErrorMessages() {
		assertEquals("Some error\n  -> Invalid value",
			Res.error("Some error",
				new IllegalArgumentException("Invalid value")).error());
		assertEquals("Some error\n  -> IllegalArgumentException",
			Res.error("Some error",
				new IllegalArgumentException((String) null)).error());
	}
}
