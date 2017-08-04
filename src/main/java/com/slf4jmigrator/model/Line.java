package com.slf4jmigrator.model;

public class Line {

	private int lineNumber;
	private String content;
	private Line nextLine;

	public Line(int lineNumber, String content) {
		this.lineNumber = lineNumber;
		this.content = content;
		this.nextLine = null;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public Line getNextLine() {
		return nextLine;
	}

	public void setNextLine(Line nextLine) {
		this.nextLine = nextLine;
	}

	public void incrementLineNumber(int increment) {
		lineNumber += increment;
	}

	public void incrementLineNumber() {
		incrementLineNumber(1);
	}

	public void decrementLineNumber() {
		incrementLineNumber(-1);
	}

	public boolean isMultiLine() {
		return nextLine != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Line line = (Line) o;

		return lineNumber == line.lineNumber && (content != null ? content.equals(line.content) : line.content == null);
	}

	@Override
	public int hashCode() {
		return 31 * lineNumber + (content != null ? content.hashCode() : 0);
	}

	@Override
	public String toString() {
		return lineNumber + ": " + content;
	}
}
