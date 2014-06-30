package com.github.davidmoten.logmetrics;

import java.util.Optional;

public class Line {

    private final long timestamp;
    private final Level level;
    private final String line;
    private final String category;
    private final Optional<String> threadName;
    private final Optional<String> className;
    private final Optional<String> methodName;

    private Line(long timestamp, Level level, String line, String category, Optional<String> threadName, Optional<String> className, Optional<String> methodName){
        this.timestamp = timestamp;
        this.level = level;
        this.line = line;
        this.category = category;
        this.threadName = threadName;
        this.className = className;
        this.methodName = methodName;
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

    public Optional<String> getThreadName() {
        return threadName;
    }

    public Optional<String> getClassName() {
        return className;
    }

    public Optional<String> getMethodName() {
        return methodName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long timestamp;
        private Level level;
        private String line;
        private String category;
        private Optional<String> threadName;
        private Optional<String> className;
        private Optional<String> methodName;

        private Builder() {
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder line(String line) {
            this.line = line;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder threadName(Optional<String> threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder className(Optional<String> className) {
            this.className = className;
            return this;
        }

        public Builder methodName(Optional<String> methodName) {
            this.methodName = methodName;
            return this;
        }

        public Line build() {
            return new Line(timestamp, level, line, category, threadName, className, methodName);
        }
    }
}
