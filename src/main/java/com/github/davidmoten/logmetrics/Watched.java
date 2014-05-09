package com.github.davidmoten.logmetrics;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

import com.github.davidmoten.util.rx.FileTailer;
import com.github.davidmoten.util.rx.IoObservable;
import com.google.common.base.Optional;

public class Watched {

    private final String category;
    private final File file;
    private final boolean tail;
    private final MetricExtractor extractor;
    private final Optional<Long> startTime;
    private final long sampleTimeMs;

    public Watched(String category, File file, boolean tail, MetricExtractor extractor,
            Optional<Long> startTime, long sampleTimeMs) {
        this.category = category;
        this.file = file;
        this.tail = tail;
        this.extractor = extractor;
        this.startTime = startTime;
        this.sampleTimeMs = sampleTimeMs;
    }

    public Observable<? extends Metrics> watch() {

        return tail(file)
        // extract metrics
                .flatMap(toMetrics(extractor, category))
                // include only those lines after start time
                .filter(after(startTime));

    }

    private Func1<String, ? extends Observable<? extends Metrics>> toMetrics(
            final MetricExtractor extractor, final String category) {
        return new Func1<String, Observable<? extends Metrics>>() {

            @Override
            public Observable<? extends Metrics> call(String line) {
                return extractor.extract(category, line);
            }
        };
    }

    private Func1<? super Metrics, Boolean> after(final Optional<Long> startTime) {
        return new Func1<Metrics, Boolean>() {

            @Override
            public Boolean call(Metrics metrics) {
                if (startTime.isPresent()) {
                    return metrics.getTimestamp() >= startTime.get();
                } else
                    return true;
            }
        };
    }

    private Observable<String> tail(final File file) {
        if (tail)
            return new FileTailer(file, 0).tail(sampleTimeMs);
        else
            return IoObservable
            // read lines from the current position
                    .lines(Util.createReader(file, 0));
    }
}
