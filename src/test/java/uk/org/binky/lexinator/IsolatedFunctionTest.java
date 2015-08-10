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
		h.next();
		h.next();
		h.next();
		assertEquals(3, h.len());
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
}

class FunctionHelper extends Lexer<FunctionHelper.Type> {
	enum Type {
		Error
	}
	
	FunctionHelper(final CharSequence text) {
		super("test", text, Type.Error);
	}
}