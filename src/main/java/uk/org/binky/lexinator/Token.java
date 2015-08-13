package uk.org.binky.lexinator;

/**
 * This class describes a token.
 *
 * @param <T> The token type used.
 */
public final class Token<T extends Enum<T>> {
	public final String file;
	public final int line;
	public final T type;
	public final String value;
	public Token(final String file, final int line, final T type, final String value) {
		this.file = file;
		this.line = line;
		this.type = type;
		this.value = value;
	}

	/**
	 * Compares this token to another, and returns true if they are equal.
	 * TODO: proper compareTo, equals, hash, etc.
	 *
	 * @param that Another token
	 * @return true if both tokens are equal
	 */
	public boolean compare(final Token<T> that) {
		if (this.file != that.file || this.line != that.line || this.type != that.type || !this.value.equals(that.value)) {
			return false;
		}
		return true;
	}
}
