package uk.org.binky.lexinator;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class TokenTest {
	enum Type {
		Test,
		Boo
	}
	
	@Test
	public void TestCompare() {
		Token<Type> orig = new Token<Type>("file", 5, Type.Test, "value");
		assertFalse(orig.compare(new Token<Type>("NO", 5, Type.Test, "value")));
		assertFalse(orig.compare(new Token<Type>("file", 500, Type.Test, "value")));
		assertFalse(orig.compare(new Token<Type>("file", 5, Type.Boo, "value")));
		assertFalse(orig.compare(new Token<Type>("file", 5, Type.Test, "mohawk")));
		assertTrue(orig.compare(new Token<Type>("file", 5, Type.Test, "value")));
	}
}
