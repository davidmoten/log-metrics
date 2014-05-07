package com.github.davidmoten.util.rx;

import java.io.Reader;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;

import rx.Observable;
import rx.observables.StringObservable;

public class IoObservable {

    public static Observable<String> lines(Reader reader) {
        return StringObservable.split(StringObservable.from(reader), "\\n");
    }

    public static Observable<WatchEvent<?>> from(WatchService watchService) {
        return WatchServiceObservable.from(watchService);
    }
}
