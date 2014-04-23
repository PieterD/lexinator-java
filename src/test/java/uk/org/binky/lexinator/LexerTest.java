package uk.org.binky.lexinator;

import org.junit.Test;


public class LexerTest {
	@Test
	public void testBasic() {
		new MyLexer(" hello = 123; bye = 456;");
	}
}

class MyLexer extends Lexer<MyLexer.Type> {
	MyLexer(String text) {
		super("test", text, Type.Eof, Type.Error, Type.Warning);
		this.state = stateVariable;
	}
	
	enum Type {
		Eof, Error, Warning,
		Variable,
		Assign,
		Number,
		Semi
	}
	
	private final State stateVariable = new State() {
		public State stateMethod() {
			whitespace();
			ignore();
			if (eof()) {
				return emitEof();
			}
			if (acceptRun("abcdefghijklmnopqrstuvwxyz") == 0) {
				return errorf("Expected variable name!");
			}
			emit(Type.Variable);
			return stateAssign;
		}
	};
	
	private final State stateAssign = new State() {
		public State stateMethod() {
			whitespace();
			ignore();
			if (!accept("=")) {
				return errorf("Expected assignment character!");
			}
			emit(Type.Assign);
			return stateNumber;
		}
	};
	
	private final State stateNumber = new State() {
		public State stateMethod() {
			whitespace();
			ignore();
			if (!accept("123456789")) {
				return errorf("Expected number!");
			}
			emit(Type.Number);
			return stateSemi;
		}
	};
	
	private final State stateSemi = new State() {
		public State stateMethod() {
			whitespace();
			ignore();
			if (!accept(";")) {
				return errorf("Expected semicolon!");
			}
			emit(Type.Semi);
			return stateVariable;
		}
	};
}
