package uk.org.binky.lexinator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ExpectExceptionTest {
	enum Type {
		Test,
		Boo
	}

	@Test
	public void TestExceptionString() {
		Token<Type> e = new Token<Type>("file", 1, Type.Test, "value");
		Token<Type> t = new Token<Type>("quack", 100, Type.Boo, "menacing");
		ExpectException exception = new ExpectException(e, t);
		assertEquals("expected/received: file(file/quack) line(1/100) type(Test/Boo) value(value/menacing)", exception.toString());
	}
}
