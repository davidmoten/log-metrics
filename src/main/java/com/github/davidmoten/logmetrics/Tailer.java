package com.github.davidmoten.logmetrics;

import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.StringObservable;

public class Tailer {

    private final File file;
    private final AtomicLong skipBytes = new AtomicLong();

    public Tailer(File file, long skipBytes) {
        this.file = file;
        this.skipBytes.set(skipBytes);

    }

    public Observable<String> tail() {

        return WatchServiceObservable
        // watch the file for changes
                .from(file, StandardWatchEventKinds.ENTRY_MODIFY)
                // emit a max of 1 event a second (the most recent)
                .sample(1, TimeUnit.SECONDS)
                // emit any new lines
                .concatMap(reportNewLines(file, skipBytes));

    }

    private static Func1<? super WatchEvent<?>, ? extends Observable<String>> reportNewLines(
            final File file, final AtomicLong skipBytes) {
        return new Func1<WatchEvent<?>, Observable<String>>() {

            @Override
            public Observable<String> call(WatchEvent<?> event) {
                if (file.length() > skipBytes.get()) {
                    final CustomFileReader reader = new CustomFileReader(file, skipBytes.get());
                    return reader.charsRead()
                    // when reader closed increase skipBytes
                            .doOnNext(increaseSkipBytes(skipBytes))
                            // doesn't contribute lines
                            .ignoreElements().cast(String.class)
                            // read lines from the reader
                            .startWith(StringObservable.from(reader));
                } else
                    return Observable.empty();
            }

        };
    }

    private static Action1<Long> increaseSkipBytes(final AtomicLong skipBytes) {
        return new Action1<Long>() {
            @Override
            public void call(Long charsRead) {
                skipBytes.addAndGet(charsRead);
            }
        };
    }
}
