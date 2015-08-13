package uk.org.binky.lexinator;


/**
 * This class represents the current state of the input text.
 * It contains the position, start, line number, and the previous state.
 */
public final class Mark {
	final int pos, line, start;
	final Mark prev; 
	Mark(final int pos, final int line, final int start, final Mark prev) {
		this.pos = pos;
		this.line = line;
		this.start = start;
		this.prev = prev;
	}
	Mark() {
		this(0, 1, 0, null);
	};
	Mark next(final boolean isLine) {
		int addline = 0;
		if (isLine) {
			addline ++;
		}
		return new Mark(pos+1, line + addline, start, this);
	}
	Mark back() {
		if (prev == null) {
			return this;
		}
		return prev;
	}
	Mark ignore() {
		return new Mark(pos, line, pos, null);
	}
	Mark retry() {
		Mark m = this;
		while (m.prev != null) {
			m = m.prev;
		}
		return m;
	}
}
