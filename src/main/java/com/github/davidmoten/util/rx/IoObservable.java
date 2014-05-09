package com.github.davidmoten.util.rx;

import java.io.Reader;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.List;
import java.util.UUID;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.StringObservable;

public class IoObservable {

    public static Observable<String> lines(Reader reader) {
        return ignoreZeroLengthAtEnd(StringObservable.split(StringObservable.from(reader), "\\n"));
    }

    public static Observable<String> ignoreZeroLengthAtEnd(Observable<String> source) {
        final String terminator = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return Observable.just(terminator).startWith(source).buffer(2, 1)
                .concatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> list) {
                        if (list.size() > 1)
                            if (terminator.equals(list.get(1)))
                                if (list.get(0).length() == 0)
                                    return Observable.empty();
                                else
                                    return Observable.just(list.get(0));
                            else
                                return Observable.just(list.get(0));
                        else if (terminator.equals(list.get(0)))
                            return Observable.empty();
                        else
                            throw new RuntimeException("unexpected");
                    }
                });
    }

    public static Observable<WatchEvent<?>> from(WatchService watchService) {
        return WatchServiceObservable.from(watchService);
    }

}
