package uk.org.binky.lexinator;

public class FunctionHelper extends Lexer<FunctionHelper.Type> {
	public enum Type {
		Error, Warning
	}
	
	public FunctionHelper(final State state, final CharSequence text) {
		super("test", text, Type.Error, Type.Warning);
		this.setState(state);
	}
}
