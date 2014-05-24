package com.verhas.filecompare;

class SourceLine {
	final String line;

	SourceLine(String line) {
		this.line = line;
	}

	boolean eof() {
		return line == null;
	}

	boolean isComment() {
		return line != null && line.startsWith("#");
	}

	boolean doesNotEqual(Object other) {
		return !equals(other);
	}

	public boolean equals(Object other) {
		return (line == null && ((SourceLine) other).line == null)
				|| (line != null && line.equals(((SourceLine) other).line));
	}

	public String toString() {
		return line;
	}
}