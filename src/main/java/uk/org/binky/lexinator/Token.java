package uk.org.binky.lexinator;

public final class Token<T extends Enum<T>> {
	public final String file;
	public final int line;
	public final T type;
	public final String value;
	Token(String file, int line, T type, String value) {
		this.file = file;
		this.line = line;
		this.type = type;
		this.value = value;
	}
}
