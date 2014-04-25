package uk.org.binky.lexinator;

import java.util.LinkedList;
import java.util.List;

public abstract class Lexer<T extends Enum<T>> {
	public final char EndOfText = '\u0003';
	private final T tokenTypeError;
	private final T tokenTypeWarning;
	
	final String name;
	final CharSequence text;
	private State state = null;
	
	final LinkedList<Token<T>> tokens = new LinkedList<Token<T>>();
	Mark mark = new Mark();
	
	public Lexer(String name, CharSequence text, T tokenTypeError, T tokenTypeWarning) {
		this.name = name;
		this.text = text;
		this.tokenTypeError = tokenTypeError;
		this.tokenTypeWarning = tokenTypeWarning;
	}
	
	public Lexer(String name, CharSequence text, T tokenTypeError) {
		this(name, text, tokenTypeError, null);
	}

	public void setState(State state) {
		this.state = state;
	}
	
	void step() {
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
	public void expect(int line, T type, String value) throws ExpectException {
		Token<T> token = getToken();
		Token<T> expect = new Token<T>(token.file, line, type, value);
		if (! token.compare(expect)) {
			throw new ExpectException(expect, token);
		}
	}
	
	/**
	 * Test helper: assert that there are no more tokens.
	 */
	public void expectEnd() throws ExpectException {
		Token<T> token = getToken();
		if (token != null) {
			throw new ExpectException(token);
		}
	}
	
	/**
	 * Fetch the next token.
	 * 
	 * @return The next token
	 */
	public Token<T> getToken() {
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
	public List<Token<T>> getAllTokens() {
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
		return this.mark;
	}
	
	
	/**
	 * Restores a state previously stored by mark.
	 * 
	 * @param mark The state being restored
	 */
	protected void unmark(Mark mark) {
		this.mark = mark;
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
			return EndOfText;
		}
		char c = text.charAt(mark.pos);
		mark = mark.next(c == '\n');
		return c;
	}
	
	/**
	 * Undo the last next. Only works once.
	 */
	protected void back() {
		mark = mark.back();
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
	 * back() will not go back beyond this.
	 */
	protected void ignore() {
		mark = mark.ignore();
	}
	
	/**
	 * Restart the current token.
	 */
	protected void retry() {
		mark = mark.retry();
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
		if (c == EndOfText) {
			return false;
		}
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
	
	protected boolean except(String invalid) {
		char c = next();
		if (c == EndOfText) {
			return false;
		}
		for (int i=0; i<invalid.length(); i++) {
			if (invalid.charAt(i) == c) {
				back();
				return false;
			}
		}
		return true;
	}
	
	protected int exceptRun(String invalid) {
		int num = 0;
		while (except(invalid)) {
			num++;
		}
		return num;
	}
	
	protected boolean whitespace() {
		while(true) {
			boolean found = false;
			char c = next();
			if (c == EndOfText) {
				return found;
			}
			if (Character.isWhitespace(c)) {
				found = true;
			} else {
				back();
				return found;
			}
		}
	}
}
