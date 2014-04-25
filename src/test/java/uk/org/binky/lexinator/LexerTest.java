package uk.org.binky.lexinator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;


public class LexerTest {
	@Test
	public void testBasic() {
		MyLexer lexer = new MyLexer(" hello = 123;\nbye = 456;");
		List<Token<MyLexer.Type>> tokens = lexer.getAllTokens();
		List<Token<MyLexer.Type>> expect = new LinkedList<Token<MyLexer.Type>>();
		expect.add(new Token<MyLexer.Type>("test", 1, MyLexer.Type.Variable, "hello"));
		expect.add(new Token<MyLexer.Type>("test", 1, MyLexer.Type.Assign,   "="));
		expect.add(new Token<MyLexer.Type>("test", 1, MyLexer.Type.Number,   "123"));
		expect.add(new Token<MyLexer.Type>("test", 1, MyLexer.Type.Semi,     ";"));
		expect.add(new Token<MyLexer.Type>("test", 2, MyLexer.Type.Variable, "bye"));
		expect.add(new Token<MyLexer.Type>("test", 2, MyLexer.Type.Assign,   "="));
		expect.add(new Token<MyLexer.Type>("test", 2, MyLexer.Type.Number,   "456"));
		expect.add(new Token<MyLexer.Type>("test", 2, MyLexer.Type.Semi,     ";"));
		expect.add(new Token<MyLexer.Type>("test", 2, MyLexer.Type.Eof,      "EOF"));
		for (int i=0; i < expect.size(); i++) {
			Token<MyLexer.Type> t = tokens.get(i);
			Token<MyLexer.Type> e = expect.get(i);
			assertNotNull(t);
			assertNotNull(e);
			assertEquals(e.file, t.file);
			assertEquals(e.line, t.line);
			assertEquals(e.type, t.type);
			assertEquals(e.value, t.value);
		}
		assertEquals(expect.size(), tokens.size());
	}
	
	@Test
	public void testExpect() throws ExpectException {
		MyLexer lexer;
		lexer = new MyLexer("knack = 5;654");
		lexer.expect(1, MyLexer.Type.Variable, "knack");
		lexer.expect(1, MyLexer.Type.Assign,   "=");
		lexer.expect(1, MyLexer.Type.Number,   "5");
		lexer.expect(1, MyLexer.Type.Semi,     ";");
		lexer.expect(1, MyLexer.Type.Error,    "Expected variable name!");
		lexer.expectEnd();
		
		lexer = new MyLexer("knack = 5");
		lexer.expect(1, MyLexer.Type.Variable, "knack");
		lexer.expect(1, MyLexer.Type.Assign,   "=");
		lexer.expect(1, MyLexer.Type.Number,   "5");
		lexer.expect(1, MyLexer.Type.Error,    "Expected semicolon!");
		lexer.expectEnd();
		
		lexer = new MyLexer("knack =");
		lexer.expect(1, MyLexer.Type.Variable, "knack");
		lexer.expect(1, MyLexer.Type.Assign,   "=");
		lexer.expect(1, MyLexer.Type.Error,    "Expected number!");
		
		lexer = new MyLexer("knack");
		lexer.expect(1, MyLexer.Type.Variable, "knack");
		lexer.expect(1, MyLexer.Type.Error,    "Expected assignment character!");
	}
	
	@Test(expected = ExpectException.class)
	public void testExpectFail() throws ExpectException {
		MyLexer lexer;
		lexer = new MyLexer("knack = 5;654");
		lexer.expect(1, MyLexer.Type.Variable, "cracker");
	}
}

class MyLexer extends Lexer<MyLexer.Type> {
	MyLexer(String text) {
		super("test", text, Type.Error);
		this.state = stateVariable;
	}
	
	enum Type {
		Eof, Error, Warning,
		Variable,
		Assign,
		Number,
		Semi
	}
	
	private State emitEof() {
		emitString(Type.Eof, "EOF");
		return null;
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
			if (!string("=")) {
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
			if (acceptRun("123456789") == 0) {
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
			if (!string(";")) {
				return errorf("Expected semicolon!");
			}
			emit(Type.Semi);
			return stateVariable;
		}
	};
}
