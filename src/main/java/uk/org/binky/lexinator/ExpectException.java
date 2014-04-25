package uk.org.binky.lexinator;

public final class ExpectException extends Exception {
	static final long serialVersionUID = 0x99a58a0c76038d23l;
	private final Token<?> expected;
	private final Token<?> received;
	
	public ExpectException(Token<?> expected, Token<?> received) {
		this.expected = expected;
		this.received = received;
	}
	
	@Override
	public String toString() {
		return String.format("expected/received: file(%s/%s) line(%d/%d) type(%s/%s) value(%s/%s)", expected.file, received.file, expected.line, received.line, expected.type.name(), received.type.name(), expected.value, received.value);
	}
}
