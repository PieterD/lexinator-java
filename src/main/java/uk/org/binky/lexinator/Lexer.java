package uk.org.binky.lexinator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedList;
import java.util.List;

public abstract class Lexer<T extends Enum<T>> {
	public final char EndOfText = '\u0003';
	private final T tokenTypeError;
	private final T tokenTypeWarning;
	
	final String name;
	final CharSequence text;
	State state = null;
	
	final LinkedList<Token<T>> tokens = new LinkedList<Token<T>>();
	Mark mark = new Mark();
	
	Lexer(String name, CharSequence text, T tokenTypeError, T tokenTypeWarning) {
		this.name = name;
		this.text = text;
		this.tokenTypeError = tokenTypeError;
		this.tokenTypeWarning = tokenTypeWarning;
	}
	
	Lexer(String name, CharSequence text, T tokenTypeError) {
		this(name, text, tokenTypeError, null);
	}
	
	private void step() {
		if (state != null) {
			State next = state.stateMethod();
			state = next;
		}
	}
	
	/**
	 * Test helper: fetch the next token, and assert its values.
	 * 
	 * @param line
	 * @param type
	 * @param value
	 */
	void expect(int line, T type, String value) {
		Token<T> token = getToken();
		assertNotNull(token);
		assertEquals(line, token.line);
		assertEquals(type, token.type);
		assertEquals(value, token.value);
	}
	
	/**
	 * Test helper: assert that there are no more tokens.
	 */
	void expectEnd() {
		Token<T> token = getToken();
		assertNull(token);
	}
	
	/**
	 * Fetch the next token.
	 * 
	 * @return The next token
	 */
	Token<T> getToken() {
		while(state != null) {
			step();
			Token<T> token = tokens.pollFirst();
			if (token != null) {
				return token;
			}
		}
		return null;
	}
	
	/**
	 * Fetch all (remaining) tokens.
	 * 
	 * @return A list of all tokens remaining.
	 */
	List<Token<T>> getAllTokens() {
		LinkedList<Token<T>> list = new LinkedList<Token<T>>();
		while(true) {
			Token<T> token = getToken();
			if (token == null) {
				break;
			}
			list.addLast(token);
		}
		return list;
	}
	
	/**
	 * Returns the string being accumulated for the next token.
	 * 
	 * @return The token string
	 */
	protected String get() {
		return text.subSequence(mark.start, mark.pos).toString();
	}
	
	/**
	 * Returns the length of the string accumulated so far.
	 * 
	 * @return The length of the token string
	 */
	protected int len() {
		return mark.pos - mark.start;
	}
	
	/**
	 * Return the state of the lexer (position in the text, line, token accumulated so far, etc).
	 * It can then later be recovered by unmark.
	 * 
	 * @return The current state
	 */
	protected Mark mark() {
		return new Mark(this.mark);
	}
	
	
	/**
	 * Restores a state previously stored by mark.
	 * 
	 * @param mark The state being restored
	 */
	protected void unmark(Mark mark) {
		this.mark = new Mark(mark);
	}
	
	/**
	 * Checks if we have reached the end of the text.
	 * 
	 * @return True if we have reached the end, false otherwise.
	 */
	protected boolean eof() {
		return mark.pos >= text.length();
	}
	
	/**
	 * Emit a token with the given type and string.
	 * 
	 * @param type Token type
	 * @param str Token string
	 */
	protected void emitString(T type, String str) {
		tokens.addLast(new Token<T>(name, mark.line, type, str));
	}
	
	/**
	 * Emit the currently accumulated string as a token with the given type.
	 * 
	 * @param type Token type
	 */
	protected void emit(T type) {
		emitString(type, get());
		ignore();
	}
	
	/**
	 * Emit an error token.
	 * 
	 * @param format As String.format
	 * @param args As string.format
	 * @return null
	 */
	protected State errorf(String format, Object... args) {
		emitString(tokenTypeError, String.format(format, args));
		return null;
	}

	/**
	 * Emit a warning token. Does nothing if no Warning token type was provided to the constructor.
	 * 
	 * @param format As String.format
	 * @param args As String.format
	 */
	protected void warningf(String format, Object... args) {
		if (tokenTypeWarning != null) {
			emitString(tokenTypeWarning, String.format(format, args));
		}
	}

	/**
	 * Return the next character in the text and advance the lexer state by one character.
	 * 
	 * @return The next character to be lexed
	 */
	protected char next() {
		if (eof()) {
			mark.width = 0;
			return EndOfText;
		}
		char c = text.charAt(mark.pos);
		mark.width = 1;
		mark.pos++;
		if (c == '\n') {
			mark.line++;
		}
		return c;
	}
	
	/**
	 * Undo the last next. Only works once.
	 */
	protected void back() {
		mark.pos -= mark.width;
		mark.width = 0;
	}
	
	/**
	 * Peeks at the upcoming character.
	 * 
	 * @return The next character to be read by next.
	 */
	protected char peek() {
		char c = next();
		back();
		return c;
	}
	
	/**
	 * Ignore the string accumulated so far.
	 */
	protected void ignore() {
		mark.start = mark.pos;
		mark.width = 0;
	}
	
	/**
	 * Restart the current token.
	 */
	protected void retry() {
		mark.pos = mark.start;
		mark.width = 0;
	}
	
	/**
	 * Read the given string completely, or nothing at all.
	 * 
	 * @param valid The string that must be matched completely.
	 * @return True if a match occurred, false otherwise.
	 */
	protected boolean string(String valid) {
		Mark start = mark();
		for (int i=0; i<valid.length(); i++) {
			char c = valid.charAt(i);
			char n = next();
			if (n == EndOfText || n != c) {
				unmark(start);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find the given string in the text.
	 * 
	 * @param valid The string being searched for.
	 * @return True if a match occurred, false otherwise.
	 */
	protected boolean find(String valid) {
		Mark start = mark();
		do {
			Mark v = mark();
			if (string(valid)) {
				unmark(v);
				return true;
			}
		} while(next() != EndOfText);
		unmark(start);
		return false;
	}
	
	protected boolean accept(String valid) {
		char c = next();
		for (int i=0; i<valid.length(); i++) {
			if (valid.charAt(i) == c) {
				return true;
			}
		}
		back();
		return false;
	}
	
	protected int acceptRun(String valid) {
		int num = 0;
		while (accept(valid)) {
			num++;
		}
		return num;
	}
	
	boolean whitespace() {
		while(true) {
			boolean found = false;
			char c = next();
			if (c == EndOfText) {
				return found;
			}
			if (Character.isSpaceChar(c)) {
				found = true;
			} else {
				back();
				return found;
			}
		}
	}
}
