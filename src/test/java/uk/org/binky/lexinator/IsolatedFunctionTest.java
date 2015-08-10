package uk.org.binky.lexinator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IsolatedFunctionTest {
	@Test
	public void testNext() {
		final FunctionHelper h = new FunctionHelper("12");
		assertEquals('1', h.next());
		assertEquals('2', h.next());
		assertEquals(h.EndOfText, h.next());
	}
	
	@Test
	public void testNextBack() {
		final FunctionHelper h = new FunctionHelper("12");
		assertEquals('1', h.next());
		h.back();
		assertEquals('1', h.next());
		h.back();
		assertEquals('1', h.next());
		assertEquals('2', h.next());
		h.back();
		assertEquals('2', h.next());
		h.back();
		assertEquals('2', h.next());
		assertEquals(h.EndOfText, h.next());
	}
	
	@Test
	public void testNoWayBack() {
		final FunctionHelper h = new FunctionHelper("12");
		h.back();
		assertEquals('1', h.next());
		h.back();
		assertEquals('1', h.next());
		h.back();
		h.back();
		assertEquals('1', h.next());
	}
	
	@Test
	public void testNextNewline() {
		final FunctionHelper h = new FunctionHelper("1\n2");
		assertEquals('1', h.next());
		assertEquals(1, h.mark().line);
		assertEquals('\n', h.next());
		assertEquals(2, h.mark().line);
		assertEquals('2', h.next());
		assertEquals(h.EndOfText, h.next());
	}
	
	@Test
	public void testNextNewlineBack() {
		final FunctionHelper h = new FunctionHelper("1\n2");
		assertEquals('1', h.next());
		assertEquals(1, h.mark().line);
		assertEquals('\n', h.next());
		assertEquals(2, h.mark().line);
		h.back();
		assertEquals(1, h.mark().line);
		assertEquals('\n', h.next());
		assertEquals(2, h.mark().line);
		assertEquals('2', h.next());
		assertEquals(h.EndOfText, h.next());
	}
	
	@Test
	public void testPeekBack() {
		final FunctionHelper h = new FunctionHelper("123");
		assertEquals('1', h.peek());
		assertEquals('1', h.next());
		assertEquals('2', h.peek());
		h.back();
		assertEquals('1', h.peek());
		assertEquals('1', h.next());
		assertEquals('2', h.peek());
	}
	
	@Test
	public void testGet() {
		final FunctionHelper h = new FunctionHelper("1234");
		h.next();
		h.next();
		assertEquals("12", h.get());
	}
	
	@Test
	public void testLen() {
		final FunctionHelper h = new FunctionHelper("1234");
		assertEquals(0, h.len());
		h.next();
		assertEquals(1, h.len());
		h.next();
		assertEquals(2, h.len());
		h.next();
		assertEquals(3, h.len());
	}

	@Test
	public void testRetry() {
		final FunctionHelper h = new FunctionHelper("12345");
		assertEquals('1', h.next());
		assertEquals(1, h.len());
		h.ignore();
		assertEquals('2', h.next());
		assertEquals('3', h.next());
		assertEquals(2, h.len());
		h.retry();
		assertEquals(0, h.len());
		assertEquals('2', h.next());
	}
	
	@Test
	public void testIgnore() {
		final FunctionHelper h = new FunctionHelper("12345");
		assertEquals('1', h.next());
		assertEquals(1, h.len());
		h.ignore();
		assertEquals('2', h.next());
		assertEquals('3', h.next());
		assertEquals(2, h.len());
		h.ignore();
		assertEquals(0, h.len());
		assertEquals('4', h.next());
		assertEquals('5', h.next());
		assertEquals("45", h.get());
	}
	
	@Test
	public void testMarkUnmark() {
		final FunctionHelper h = new FunctionHelper("12345");
		assertEquals('1', h.next());
		assertEquals('2', h.peek());
		final Mark mark1 = h.mark();
		assertEquals('2', h.next());
		assertEquals('3', h.next());
		final Mark mark2 = h.mark();
		h.unmark(mark1);
		assertEquals('2', h.peek());
		assertEquals('2', h.next());
		h.unmark(mark2);
		assertEquals('4', h.peek());
		assertEquals('4', h.next());
	}
	
	@Test
	public void testAccept() {
		final FunctionHelper h = new FunctionHelper("1234");
		assertFalse(h.accept("2345"));
		assertTrue(h.accept("1"));
		assertFalse(h.accept("1"));
		assertTrue(h.accept("2345"));
		assertTrue(h.accept("3"));
		assertTrue(h.accept("4"));
		assertFalse(h.accept("12345"));
		assertEquals(h.EndOfText, h.next());
	}
	
	@Test
	public void testExcept() {
		final FunctionHelper h = new FunctionHelper("1234");
		assertFalse(h.except("12345"));
		assertTrue(h.except("234"));
		assertFalse(h.except("234"));
		assertTrue(h.except("34"));
		assertTrue(h.except("4"));
		assertTrue(h.except("123"));
		assertFalse(h.except("123"));
		assertEquals(h.EndOfText, h.next());
	}

	@Test
	public void testAcceptRun() {
		final FunctionHelper h = new FunctionHelper("123456");
		assertEquals(0, h.acceptRun("23456"));
		assertEquals(3, h.acceptRun("123"));
		assertEquals('4', h.peek());
	}

	@Test
	public void testExceptRun() {
		final FunctionHelper h = new FunctionHelper("123456");
		assertEquals(0, h.exceptRun("1"));
		assertEquals(3, h.exceptRun("456"));
		assertEquals('4', h.peek());
	}
	
	@Test
	public void testSpace() {
		final FunctionHelper h = new FunctionHelper("X \t\nY");
		assertFalse(h.space());
		assertEquals('X', h.next());
		assertTrue(h.space());
		assertFalse(h.space());
		assertEquals('Y', h.next());
		assertFalse(h.space());
	}
	
	@Test
	public void testSpaceNoLine() {
		final FunctionHelper h = new FunctionHelper("X \t\nY");
		assertFalse(h.spaceNoLine());
		assertEquals('X', h.next());
		assertTrue(h.spaceNoLine());
		assertFalse(h.spaceNoLine());
		assertEquals('\n', h.next());
		assertEquals('Y', h.next());
		assertFalse(h.spaceNoLine());
	}
	
	@Test
	public void testString() {
		final FunctionHelper h = new FunctionHelper("1abc2");
		assertFalse(h.string("abc"));
		assertEquals('1', h.next());
		assertTrue(h.string("abc"));
		assertFalse(h.string("abc"));
		assertEquals('2', h.next());
		assertFalse(h.string("abc"));
	}
	
	@Test
	public void testFind() {
		final FunctionHelper h = new FunctionHelper("123123abc123");
		assertFalse(h.find("abcd"));
		assertTrue(h.find("123"));
		assertEquals(0, h.len());
		assertTrue(h.find("abc"));
		assertEquals("123123", h.get());
		h.ignore();
		assertTrue(h.find("123"));
		assertEquals("abc", h.get());
	}
	
	@Test
	@Deprecated
	public void testWhitespace() {
		final FunctionHelper h = new FunctionHelper("X \t\nY");
		assertFalse(h.whitespace());
		assertEquals('X', h.next());
		assertTrue(h.whitespace());
		assertFalse(h.whitespace());
		assertEquals('Y', h.next());
		assertFalse(h.whitespace());
	}
	
	@Test
	@Deprecated
	public void testWhitespaceNewline() {
		final FunctionHelper h = new FunctionHelper("X \t\n\t Y\t\tZ");
		assertFalse(h.whitespace(true));
		assertEquals('X', h.next());
		assertTrue(h.whitespace(true));
		assertEquals('\t', h.next());
		assertEquals(' ', h.next());
		assertEquals('Y', h.next());
		assertFalse(h.whitespace(true));
		assertEquals('\t', h.next());
		assertEquals('\t', h.next());
		assertEquals('Z', h.next());
		assertTrue(h.whitespace(true));
	}
	
	@Test
	@Deprecated
	public void testWhitespaceEndOfText() {
		final FunctionHelper h = new FunctionHelper(" ");
		h.ignore();
		assertTrue(h.whitespace(false));
		h.ignore();
		assertTrue(h.whitespace(true));
		h.ignore();
		h.next();
		assertFalse(h.whitespace(false));
		assertTrue(h.whitespace(true));
	}
}

class FunctionHelper extends Lexer<FunctionHelper.Type> {
	enum Type {
		Error
	}
	
	FunctionHelper(final CharSequence text) {
		super("test", text, Type.Error);
	}
}