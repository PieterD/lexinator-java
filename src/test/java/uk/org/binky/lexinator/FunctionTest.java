package uk.org.binky.lexinator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FunctionTest {
	@Test
	public void Whitespace() {
		WSLexer lexer = new WSLexer();
		lexer.step();
	}

	@Test
	public void Simple() {
		FuncLexer lexer = new FuncLexer();
		lexer.step();
	}
}

class WSLexer extends Lexer<WSLexer.Type> {
	enum Type {
		Error,
		Test
	}

	WSLexer() {
		super("test", "hello\n  world  \nthere is nothing\n\tthere! ", Type.Error);
		this.setState(testState);
	}

	private final State testState = new State() {
		public State stateMethod() {
			assertFalse(whitespace());
			assertFalse(whitespace(true));
			assertTrue(string("hello"));
			assertTrue(whitespace(true));
			assertEquals(' ', peek());
			assertTrue(whitespace());
			assertTrue(string("world"));
			assertTrue(whitespace());
			assertTrue(string("there"));
			assertFalse(whitespace(true));
			assertTrue(string(" "));
			assertFalse(whitespace(true));
			assertTrue(string("is"));
			assertFalse(whitespace(true));
			assertTrue(whitespace());
			assertTrue(string("nothing"));
			assertTrue(whitespace(true));
			assertTrue(whitespace());
			assertTrue(string("there!"));
			Mark stored = mark();
			assertTrue(whitespace());
			assertTrue(eof());
			assertFalse(whitespace());
			assertTrue(whitespace(true));
			unmark(stored);
			assertTrue(whitespace(true));
			assertTrue(eof());

			return null;
		}
	};
}

class FuncLexer extends Lexer<FuncLexer.Type> {
	enum Type {
		Error,
		Test
	}
	
	FuncLexer() {
		super("test", "ABtestingXYXYZ\nline2", Type.Error);
		this.setState(testState);
	}
	
	private final State testState = new State() {
		public State stateMethod() {
			assertEquals('A', next());
			assertEquals('B', next());
			back();
			Mark stored = mark();
			assertEquals('B', next());
			back();
			back();
			assertEquals('A', next());
			assertEquals('B', next());
			back();
			back();
			back();
			assertEquals('A', next());
			assertEquals('B', next());
			
			assertEquals(2, len());
			assertEquals("AB", get());
			
			assertEquals('t', peek());
			assertTrue(string("testing"));
			
			unmark(stored);
			assertEquals('B', next());
			retry();
			assertTrue(find("testing"));
			assertEquals("AB", get());
			ignore();
			assertEquals(0, len());
			assertEquals('t', peek());
			back();
			assertEquals(0, len());
			assertEquals('t', peek());
			
			assertEquals(4, acceptRun("tse"));
			assertEquals("test", get());
			
			stored = mark();
			
			assertEquals(3, exceptRun("YX"));
			ignore();
			
			assertFalse(find("spoopty"));
			assertEquals(0, len());
			assertFalse(string("spoopty"));
			assertEquals(0, len());
			assertTrue(find("line2"));
			assertEquals(2, mark.line);
			assertEquals("XYXYZ\n", get());
			
			unmark(stored);
			assertEquals(1, mark.line);
			
			assertFalse(eof());
			
			assertEquals(14, exceptRun("QPD"));
			
			 assertTrue(eof());
			
			return null;
		}
	};
}
