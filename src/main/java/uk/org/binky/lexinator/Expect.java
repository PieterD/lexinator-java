package uk.org.binky.lexinator;

/**
 * This class can be used to quickly test a lexer (or any other Tokenizer).
 *
 * @param <T> The token type
 */
public class Expect<T extends Enum<T>> {
    final Tokenizer<T> tokenizer;
    public Expect(final Tokenizer<T> tokenizer) {
        this.tokenizer = tokenizer;
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
        final Token<T> token = tokenizer.getToken();
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
        final Token<T> token = tokenizer.getToken();
        if (token != null) {
            throw new ExpectException(token);
        }
    }

}
