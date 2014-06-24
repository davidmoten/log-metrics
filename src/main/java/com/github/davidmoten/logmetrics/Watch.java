package com.github.davidmoten.logmetrics;

import static com.github.davidmoten.logmetrics.Util.createReader;

import java.io.File;
import java.nio.charset.Charset;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.StringObservable;

import com.github.davidmoten.rx.FileObservable;
import com.google.common.base.Optional;

public class Watch {

    private final String category;
    private final File file;
    private final boolean tail;
    private final MetricExtractor extractor;
    private final Optional<Long> startTime;
    private final long sampleTimeMs;

    private Watch(String category, File file, boolean tail, MetricExtractor extractor,
            Optional<Long> startTime, long sampleTimeMs) {
        if (file == null)
            throw new NullPointerException("file parameter cannot be null");
        if (extractor == null)
            throw new NullPointerException("extractor parameter cannot be null");
        this.category = category;
        this.file = file;
        this.tail = tail;
        this.extractor = extractor;
        this.startTime = startTime;
        this.sampleTimeMs = sampleTimeMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String category = "General";
        private File file;
        private boolean tail = true;
        private MetricExtractor extractor;
        private Optional<Long> startTime = Optional.absent();
        private long sampleTimeMs = 1000;

        private Builder() {
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder file(File file) {
            this.file = file;
            return this;
        }

        public Builder tail(boolean tail) {
            this.tail = tail;
            return this;
        }

        public Builder extractor(MetricExtractor extractor) {
            this.extractor = extractor;
            return this;
        }

        public Builder startTime(Optional<Long> startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder sampleTimeMs(long sampleTimeMs) {
            this.sampleTimeMs = sampleTimeMs;
            return this;
        }

        public Watch build() {
            return new Watch(category, file, tail, extractor, startTime, sampleTimeMs);
        }
    }

    public Observable<? extends Metrics> watch() {

        return tail(file)
        // extract metrics
                .flatMap(toMetrics(extractor, category))
                // include only those lines after start time
                .filter(after(startTime));

    }

    private Func1<String, Observable<? extends Metrics>> toMetrics(final MetricExtractor extractor,
            final String category) {
        return line -> extractor.extract(category, line);
    }

    private Func1<Metrics, Boolean> after(final Optional<Long> startTime) {
        return metrics -> {
            if (startTime.isPresent()) {
                return metrics.getTimestamp() >= startTime.get();
            } else
                return true;
        };
    };

    private Observable<String> tail(final File file) {
        if (tail)
            return FileObservable.tailTextFile(file, 0, sampleTimeMs, Charset.forName("UTF-8"));
        else
            // TODO use Observable.using
            return StringObservable.split(StringObservable.from(createReader(file, 0)), "\n");
    }
}
