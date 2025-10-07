package org.openlca.commons;

import java.util.Objects;
import java.util.function.Supplier;

/// A return type that can contain the return value, an error, or could be
/// empty. This is useful when you do not want to throw exceptions but return
/// a possible error as a value.
public sealed interface Res<T> {

	/// Returns the value of the result. This will throw an exception when the
	/// result does not contain a value but an error or is empty.
	T value();

	/// Returns the error of the result. This will throw an exception when this
	/// result is not an error.
	String error();

	/// Returns `true` when this result is an error.
	default boolean isError() {
		return false;
	}

	/// Returns `true` when this result is not an error (has a value or is
	/// empty).
	default boolean isOk() {
		return true;
	}

	/// Returns `true` when this result is empty.
	default boolean isEmpty() {
		return false;
	}

	/// Creates a new result of the given value. Note that the value must not be
	/// `null`, if you want to wrap a nullable object use an Optional, like:
	/// ```
	/// Res.ok(Optional.of(42));
 /// ```
	static <T> Res<T> ok(T value) {
		return new Ok<>(value);
	}

	/// Creates an ok result, without a value. This is useful for methods, that
	/// return nothing (`void``) or an error.
	static Res<Void> ok() {
		return Empty.instance;
	}

	/// Creates an error with the given message. Note that the error must not be
	/// `null`.
	static <T> Res<T> error(String message) {
		return new Err<>(message);
	}

	/// Creates an error from the given message and exception.
	static <T> Res<T> error(String message, Throwable err) {
		return err != null
			? error(message + ": " + err.getMessage())
			: error(message);
	}

	/// Casts an error into another result type.
	@SuppressWarnings("unchecked")
	default <E> Res<E> castError() {
		return switch (this) {
			case Err<?> err -> (Res<E>) err;
			case Ok(Object value) -> error("Value casted to error: " + value);
			case Empty ignored -> error("Casted to error");
		};
	}

	/// Wraps an error with the given message.
	default <E> Res<E> wrapError(String message) {
		return switch (this) {
			case Err(String inner) -> error(message + "\n  -> " + inner);
			default -> castError().wrapError(message);
		};
	}

	/// Returns the value if present, otherwise it calls the given function and
	/// returns its result. It is fine to pass `null` as the function argument,
	/// so that `null` is also returned as the default value.
	default T orElse(Supplier<T> fn) {
		if (this instanceof Ok(T value))
			return value;
		return fn != null
			? fn.get()
			: null;
	}

	/// Returns the value of the result or throws an exception if the result is
	/// an error or empty.
	default T orElseThrow() {
		return switch (this) {
			case Ok(T value) -> value;
			case Err(String error) -> throw new IllegalStateException(
				"Result is an error: " + error);
			case Empty() -> throw new IllegalStateException(
				"Result is empty");
		};
	}

	record Ok<T>(T value) implements Res<T> {

		public Ok {
			Objects.requireNonNull(value);
		}

		@Override
		public String error() {
			throw new IllegalStateException("Result is not an error");
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

	record Empty() implements Res<Void> {

		private static final Empty instance = new Empty();

		@Override
		public Void value() {
			throw new IllegalStateException("Result is empty");
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String error() {
			throw new IllegalStateException("Result is not an error");
		}

		@Override
		public String toString() {
			return "ok!";
		}
	}

	record Err<T>(String error) implements Res<T> {

		public Err {
			Objects.requireNonNull(error);
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public boolean isOk() {
			return false;
		}

		@Override
		public T value() {
			throw new IllegalStateException("Result is an error: " + error);
		}

		@Override
		public String toString() {
			return "Error: " + error;
		}
	}
}
