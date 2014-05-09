package com.github.davidmoten.util.rx;

import static com.github.davidmoten.logmetrics.Util.createReader;
import static com.github.davidmoten.util.rx.IoObservable.lines;
import static com.github.davidmoten.util.rx.IoObservable.trimEmpty;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.base.Preconditions;

public class FileTailer {

    private static enum Event {
        EVENT;
    }

    private final File file;
    private final AtomicLong currentPosition = new AtomicLong();

    public FileTailer(File file, long startPositionBytes) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(startPositionBytes >= 0,
                "startPositionBytes must be non-negative");
        this.file = file;
        this.currentPosition.set(startPositionBytes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File file;
        private long startPositionBytes = 0;

        public Builder file(File file) {
            this.file = file;
            return this;
        }

        public Builder startPositionBytes(long startPositionBytes) {
            this.startPositionBytes = startPositionBytes;
            return this;
        }

        public FileTailer build() {
            return new FileTailer(file, startPositionBytes);
        }
    }

    public Observable<String> tail(long sampleEveryMillis) {
        checkArgument(file.exists(), "file does not exist: " + file);
        checkArgument(!file.isDirectory(), "file cannot be a directory: " + file);

        return WatchServiceObservable
        // watch the file for changes
                .from(file, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY).map(TO_EVENT)
                // get lines once on subscription so we tail the lines in the
                // file at startup
                .startWith(Event.EVENT)
                // emit a max of 1 event per sample period
                .sample(sampleEveryMillis, TimeUnit.MILLISECONDS)
                // emit any new lines
                .concatMap(reportNewLines(file, currentPosition));
    }

    private static final Func1<WatchEvent<?>, Event> TO_EVENT = new Func1<WatchEvent<?>, Event>() {

        @Override
        public Event call(WatchEvent<?> event) {
            return Event.EVENT;
        }
    };

    private static Func1<Event, Observable<String>> reportNewLines(final File file,
            final AtomicLong currentPosition) {
        return new Func1<Event, Observable<String>>() {
            @Override
            public Observable<String> call(Event event) {
                if (file.length() > currentPosition.get()) {
                    return trimEmpty(lines(createReader(file, currentPosition.get())))
                    // as each line produced increment the current
                    // position with its length plus one for the new
                    // line separator
                            .doOnNext(moveCurrentPositionByStringLengthPlusOne(currentPosition));
                } else
                    return Observable.empty();
            }
        };
    }

    private static Action1<String> moveCurrentPositionByStringLengthPlusOne(
            final AtomicLong currentPosition) {
        return new Action1<String>() {
            boolean firstTime = true;

            @Override
            public void call(String line) {
                if (firstTime)
                    currentPosition.addAndGet(line.length());
                else
                    currentPosition.addAndGet(line.length() + 1);
                firstTime = false;
            }
        };
    }

}
