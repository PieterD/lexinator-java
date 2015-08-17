package uk.org.binky.lexinator;

public interface Tokenizer<T extends Enum<T>> {
    Token<T> getToken();
}
