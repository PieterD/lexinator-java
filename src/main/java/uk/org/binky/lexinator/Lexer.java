package uk.org.binky.lexinator;

import java.util.LinkedList;
import java.util.List;

public abstract class Lexer<T extends Enum<T>> {
	public final char EndOfText = '\u0003';
	private final T tokenTypeError;
	private final T tokenTypeWarning;
	
	final String name;
	final CharSequence text;
	protected State state = null;
	
	final LinkedList<Token<T>> tokens = new LinkedList<Token<T>>();
	Mark mark = new Mark();
	
	/**
	 * Start a Lexer for a file with the given name and contents.
	 * Also provide a token type value for errors and warnings. 
	 * 
	 * @param name Name of the file being parsed
	 * @param text Contents of the file being parsed
	 * @param tokenTypeError Type value for errors
	 * @param tokenTypeWarning Type value for warnings
	 */
	public Lexer(final String name, final CharSequence text, final T tokenTypeError, final T tokenTypeWarning) {
		this.name = name;
		this.text = text;
		this.tokenTypeError = tokenTypeError;
		this.tokenTypeWarning = tokenTypeWarning;
	}
	
	/**
	 * Same as Lexer(name, text, tokenTypeError, null)
	 * 
	 * @param name Name of the file being parsed
	 * @param text Contents of the file being parsed
	 * @param tokenTypeError Type value for errors
	 */
	public Lexer(final String name, final CharSequence text, final T tokenTypeError) {
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
	 * @param line Expected line number.
	 * @param type Expected token type.
	 * @param value Expected token contents.
	 * @throws ExpectException if the token failed to match the given arguments.
	 */
	public void expect(final int line, final T type, final String value) throws ExpectException {
		final Token<T> token = getToken();
		if (token == null) {
			throw new ExpectException(new Token<T>("???", line, type, value), null);
		}
		final Token<T> expect = new Token<T>(token.file, line, type, value);
		if (! token.compare(expect)) {
			throw new ExpectException(expect, token);
		}
	}
	
	/**
	 * Test helper: assert that there are no more tokens.
	 * 
	 * @throws ExpectException if a token was read, instead of nothing.
	 */
	public void expectEnd() throws ExpectException {
		final Token<T> token = getToken();
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
		Token<T> token = tokens.pollFirst();
		while(token == null && state != null) {
			step();
			token = tokens.pollFirst();
		}
		return token;
	}
	
	/**
	 * Fetch all (remaining) tokens.
	 * 
	 * @return A list of all tokens remaining.
	 */
	public List<Token<T>> getAllTokens() {
		final LinkedList<Token<T>> list = new LinkedList<Token<T>>();
		while(true) {
			final Token<T> token = getToken();
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
	protected void unmark(final Mark mark) {
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
	protected void emitString(final T type, final String str) {
		tokens.addLast(new Token<T>(name, mark.line, type, str));
	}
	
	/**
	 * Emit the currently accumulated string as a token with the given type.
	 * 
	 * @param type Token type
	 */
	protected void emit(final T type) {
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
	protected State errorf(final String format, final Object... args) {
		emitString(tokenTypeError, String.format(format, args));
		return null;
	}

	/**
	 * Emit a warning token. Does nothing if no Warning token type was provided to the constructor.
	 * 
	 * @param format As String.format
	 * @param args As String.format
	 */
	protected void warningf(final String format, final Object... args) {
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
		final char c = text.charAt(mark.pos);
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
		final char c = next();
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
	protected boolean string(final String valid) {
		final Mark start = mark();
		for (int i=0; i<valid.length(); i++) {
			final char c = valid.charAt(i);
			final char n = next();
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
	protected boolean find(final String valid) {
		final Mark start = mark();
		do {
			final Mark v = mark();
			if (string(valid)) {
				unmark(v);
				return true;
			}
		} while(next() != EndOfText);
		unmark(start);
		return false;
	}

	/**
	 * Read a single character, as long as it is in valid.
	 * 
	 * @param valid The list of possible characters to accept.
	 * @return True if a character was accepted, false otherwise.
	 */
	protected boolean accept(final String valid) {
		final char c = next();
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

	/**
	 * Like accept, but it keeps reading until a character is found
	 * that is not in the valid set.
	 * 
	 * @param valid The list of possible characters to accept.
	 * @return The number of characters read.
	 */
	protected int acceptRun(final String valid) {
		int num = 0;
		while (accept(valid)) {
			num++;
		}
		return num;
	}

	/**
	 * The reverse of accept; read a single character, but only if
	 * it is not in invalid.
	 * 
	 * @param invalid The list of characters to reject
	 * @return True if a character was accepted, false otherwise.
	 */
	protected boolean except(final String invalid) {
		final char c = next();
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

	/**
	 * Like except, but for multiple characters.
	 * It keeps calling except until it finds a character in invalid.
	 * 
	 * @param invalid The list of characters to reject.
	 * @return The number of characters read.
	 */
	protected int exceptRun(final String invalid) {
		int num = 0;
		while (except(invalid)) {
			num++;
		}
		return num;
	}

	/**
	 * This is too complicated. Don't use this. I don't know what I was thinking. See space().
	 * 
	 * Consumes whitespace.
	 * If newline is false, it consumes all the whitespace it can find, returning true if it finds any.
	 * If newline is true, it consumes all whitespace up to and including the first newline it can find.
	 * If newline is true, it returns true if a newline was found or if the end of the text was reached, and false otherwise.
	 * If newline is true, and no newline was found, then no characters are consumed.
	 *
	 * @param newline Whether newlines are included in whitespace.
	 * @return true if any whitespace was consumed.
	 */
	@Deprecated
	protected boolean whitespace(boolean newline) {
		Mark stored = mark();
		boolean found = false;
		while(true) {
			final char c = next();
			if (c == EndOfText) {
				return found || newline;
			}
			if (newline && c == '\n') {
				return true;
			}
			if (Character.isWhitespace(c)) {
				found = true;
			} else {
				if (newline) {
					unmark(stored);
					return false;
				}
				back();
				return found;
			}
		}
	}

	/**
	 * This one is not too complicated, but the other one is. Deprecate both, see space().
	 * 
	 * Consumes all the whitespace it can find, returning true if it finds any.
	 *
	 * @return true if any whitespace was consumed.
	 */
	@Deprecated
	protected boolean whitespace() {
		return whitespace(false);
	}

	private boolean space(final boolean newline) {
		boolean found = false;
		while (true) {
			final char c = next();
			if (c == EndOfText) {
				return found;
			}
			if (newline && c == '\n') {
				back();
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
	
	/**
	 * Consumes all the whitespace it can find. Returns true if it finds any.
	 *
	 * @return true if any whitespace was consumed.
	 */
	protected boolean space() {
		return space(true);
	}
	
	/**
	 * Consumes all the whitespace it can find, except newlines. Returns true if it finds any.
	 *
	 * @return true if any whitespace was consumed.
	 */
	protected boolean spaceNoLine() {
		return space(false);
	}
}
