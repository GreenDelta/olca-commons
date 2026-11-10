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

	@Test
	public void testThen() {

		// Function variant: Ok -> next result
		var r1 = Res.ok("hello").then(s -> Res.ok(s.length()));
		assertFalse(r1.isError());
		assertEquals(Integer.valueOf(5), r1.value());

		// Function variant: Ok -> error
		var r2 = Res.ok("hello").<Integer>then(_ -> Res.error("oops"));
		assertTrue(r2.isError());

		// Function variant: error propagation
		var r3 = Res.<String>error("fail").then(_ -> Res.ok(42));
		assertTrue(r3.isError());

		// Function variant: Empty propagation (was broken before)
		var r4 = Res.ok().then(_ -> Res.ok());
		assertFalse(r4.isError());

		// Supplier variant: Ok -> supplied value
		var r5 = Res.ok(42).then(() -> Res.ok("hello"));
		assertFalse(r5.isError());
		assertEquals("hello", r5.value());

		// Supplier variant: error propagation
		var r6 = Res.<Integer>error("fail").then(() -> Res.ok("hello"));
		assertTrue(r6.isError());

		// Supplier variant: Empty -> supplied value
		var r7 = Res.ok().then(() -> Res.ok(42));
		assertFalse(r7.isError());
		assertEquals(Integer.valueOf(42), r7.value());
	}

	@Test
	public void testOkWithNull() {
		var e = assertThrows(NullPointerException.class, () -> Res.ok(null));
		assertEquals("value must not be null", e.getMessage());
	}

	@Test
	public void testValueOfErrorOrEmpty() {
		var e1 = assertThrows(IllegalStateException.class,
			() -> Res.error("fail").value());
		assertEquals("Result is an error: fail", e1.getMessage());
		assertNull(e1.getCause());

		var cause = new IllegalArgumentException("invalid");
		var e2 = assertThrows(IllegalStateException.class,
			() -> Res.error("fail", cause).value());
		assertSame(cause, e2.getCause());

		var e3 = assertThrows(IllegalStateException.class,
			() -> Res.ok().value());
		assertEquals("Result is empty", e3.getMessage());
	}

	@Test
	public void testErrorOfOkOrEmpty() {
		assertThrows(IllegalStateException.class, () -> Res.ok(42).error());
		assertThrows(IllegalStateException.class, () -> Res.ok().error());
	}

	@Test
	public void testOrElse() {
		assertEquals(Integer.valueOf(42), Res.ok(42).orElse(() -> 7));
		assertEquals(Integer.valueOf(7), Res.<Integer>error("fail").orElse(() -> 7));
		assertNull(Res.<Integer>error("fail").orElse(null));
		assertNull(Res.ok().orElse(() -> null));
	}

	@Test
	public void testOrElseThrow() {
		assertEquals(Integer.valueOf(42), Res.ok(42).orElseThrow());

		// an empty result does not throw an exception but returns `null`
		assertNull(Res.ok().orElseThrow());

		var e1 = assertThrows(IllegalStateException.class,
			() -> Res.error("fail").orElseThrow());
		assertEquals("Result is an error: fail", e1.getMessage());
		assertNull(e1.getCause());

		var cause = new IllegalArgumentException("invalid");
		var e2 = assertThrows(IllegalStateException.class,
			() -> Res.error("fail", cause).orElseThrow());
		assertSame(cause, e2.getCause());
	}

	@Test
	public void testCastErrorOfOkOrEmpty() {
		Res<String> r1 = Res.ok(42).castError();
		assertTrue(r1.isError());
		assertEquals("Value casted to error: 42", r1.error());

		Res<String> r2 = Res.ok().castError();
		assertTrue(r2.isError());
		assertEquals("Casted to error", r2.error());
	}
}
