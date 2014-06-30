package com.github.davidmoten.logmetrics;

public class Line {

    private final long timestamp;
    private final Level level;
    private final String line;
    private final String category;

    public Line(String category, long timestamp, Level level, String line) {
	this.category = category;
	this.timestamp = timestamp;
	this.level = level;
	this.line = line;
    }

    public long getTimestamp() {
	return timestamp;
    }

    public Level getLevel() {
	return level;
    }

    public String getLine() {
	return line;
    }

    public String getCategory() {
	return category;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Line [timestamp=");
	builder.append(timestamp);
	builder.append(", level=");
	builder.append(level);
	builder.append(", line=");
	builder.append(line);
	builder.append(", category=");
	builder.append(category);
	builder.append("]");
	return builder.toString();
    }

}
