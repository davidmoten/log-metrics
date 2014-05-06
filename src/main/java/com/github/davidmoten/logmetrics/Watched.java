package com.github.davidmoten.logmetrics;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.StringObservable;

import com.google.common.base.Optional;

public class Watched {

    private final String category;
    private final File file;
    private final boolean tail;
    private final MetricExtractor extractor;
    private final Optional<Long> startTime;

    public Watched(String category, File file, boolean tail, MetricExtractor extractor,
            Optional<Long> startTime) {
        this.category = category;
        this.file = file;
        this.tail = tail;
        this.extractor = extractor;
        this.startTime = startTime;
    }

    public Observable<? extends Metrics> watch() {
        CustomFileReader reader = new CustomFileReader(file, 0);
        return tail(file, reader.charsRead())
        // read contents first
                .startWith(StringObservable.from(reader))
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

    private Observable<String> tail(final File file, Observable<Long> skipBytes) {
        if (tail)
            return skipBytes.flatMap(new Func1<Long, Observable<String>>() {

                @Override
                public Observable<String> call(Long skipBytes) {
                    return new FileTailer(file, skipBytes).tail();
                }
            });
        else
            return Observable.empty();
    }
}
