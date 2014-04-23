package uk.org.binky.lexinator;


public class Mark {
	int pos, line, start, width;
	Mark() {
		this.pos = 0;
		this.line = 1;
		this.start = 0;
		this.width = 0;
	};
	Mark(Mark m) {
		this.pos = m.pos;
		this.line = m.line;
		this.start = m.start;
		this.width = m.width;
	}
}

