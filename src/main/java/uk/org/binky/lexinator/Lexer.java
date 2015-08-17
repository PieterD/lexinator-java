package uk.org.binky.lexinator;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements a basic FSM-style Lexer.
 * When extending this class, no methods should be overridden.
 * In the constructor, after super, make sure you call setState,
 * so that the Lexer knows which state to start with.
 *
 * @param <T> The token type to use (must contain at least an error value, given to the constructor)
 */
public abstract class Lexer<T extends Enum<T>> implements Tokenizer<T> {
	/**
	 * This is returned by next() when the end of the text is reached.
	 */
	public static final char EndOfText = '\u0003';

	private final T tokenTypeError;
	private final T tokenTypeWarning;
	private final String name;
	private final CharSequence text;

	private final LinkedList<Token<T>> tokens = new LinkedList<Token<T>>();
	private State state = null;
	private Mark mark = new Mark();
	
	/**
	 * Start a Lexer for a file with the given name and contents.
	 * Also provide a token type value for errors and warnings. 
	 * 
	 * @param name Name of the file being parsed
	 * @param text Contents of the file being parsed
	 * @param tokenTypeError Type value for errors
	 * @param tokenTypeWarning Type value for warnings
	 */
	protected Lexer(final String name, final CharSequence text, final T tokenTypeError, final T tokenTypeWarning) {
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
	protected Lexer(final String name, final CharSequence text, final T tokenTypeError) {
		this(name, text, tokenTypeError, null);
	}

	/**
	 * Set the initial state of the Lexer. This should be called from
	 * the constructor; since the default is null, it will otherwise
	 * stop immediately.
	 *
	 * @param state The initial state function to use
	 */
	protected void setState(State state) {
		this.state = state;
	}

	/**
	 * Deprecated; use the Expect class instead.
	 *
	 * Test helper: fetch the next token, and assert its values.
	 *
	 * @param line Expected line number.
	 * @param type Expected token type.
	 * @param value Expected token contents.
	 * @throws ExpectException if the token failed to match the given arguments.
	 */
	@Deprecated
	public void expect(final int line, final T type, final String value) throws ExpectException {
		new Expect<T>(this).expect(line, type, value);
	}

	/**
	 * Deprecated; use the Expect class instead.
	 *
	 * Test helper: assert that there are no more tokens.
	 *
	 * @throws ExpectException if a token was read, instead of nothing.
	 */
	@Deprecated
	public void expectEnd() throws ExpectException {
		new Expect<T>(this).expectEnd();
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

	private void step() {
		if (state != null) {
			State next = state.stateMethod();
			state = next;
		}
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
	 * Undo the last next. Can be used multiple times, undoes an operation each time,
	 * but only back until the last retry, or any of the emitting methods.
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
		return space(false);
	}
	
	/**
	 * Consumes all the whitespace it can find, except newlines. Returns true if it finds any.
	 *
	 * @return true if any whitespace was consumed.
	 */
	protected boolean spaceNoLine() {
		return space(true);
	}
}
