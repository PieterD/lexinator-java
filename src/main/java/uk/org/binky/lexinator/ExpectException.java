package uk.org.binky.lexinator;

public final class ExpectException extends Exception {
	static final long serialVersionUID = 0x99a58a0c76038d23l;
	private final Token<?> expected;
	private final Token<?> received;

	public ExpectException(final Token<?> expected, final Token<?> received) {
		this.expected = expected;
		this.received = received;
	}

	public ExpectException(final Token<?> received) {
		this.expected = null;
		this.received = received;
	}
	
	@Override
	public String toString() {
		if (this.expected == null) {
			return String.format("expected nothing, got: file(%s) line(%d) type(%s) value(%s)", received.file, received.line, received.type, received.value);
		}
		if (this.received == null) {
			return String.format("received nothing, expected: file(%s) line(%d) type(%s) value(%s)", expected.file, expected.line, expected.type, expected.value);
		}
		return String.format("expected/received: file(%s/%s) line(%d/%d) type(%s/%s) value(%s/%s)", expected.file, received.file, expected.line, received.line, expected.type.name(), received.type.name(), expected.value, received.value);
	}
}
