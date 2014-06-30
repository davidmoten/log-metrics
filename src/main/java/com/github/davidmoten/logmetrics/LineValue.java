package com.github.davidmoten.logmetrics;

public class LineValue<T> {
    private final Line line;
    private final String key;
    private final T value;

    public LineValue(Line line, String key, T value) {
	this.line = line;
	this.key = key;
	this.value = value;
    }

    public Line getLine() {
	return line;
    }

    public String getKey() {
	return key;
    }

    public T getValue() {
	return value;
    }

    @Override
    public String toString() {
	return "LineValue [line=" + line + ", key=" + key + ", value=" + value
		+ "]";
    }
}
