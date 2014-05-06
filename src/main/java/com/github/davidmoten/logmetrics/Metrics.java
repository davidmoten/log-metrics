package com.github.davidmoten.logmetrics;

public class Metrics {

    private final long timestamp;
    private final Level level;
    private final String line;
    private final String category;

    public Metrics(String category, long timestamp, Level level, String line) {
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

}
