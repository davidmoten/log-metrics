package com.github.davidmoten.logmetrics;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class Watcher {

    private final List<Watched> watched;

    public Watcher(List<Watched> watched) {
        this.watched = watched;
    }

    public Observable<Metrics> run() {
        return Observable.from(watched).flatMap(WATCH);
    }

    private static final Func1<? super Watched, Observable<? extends Metrics>> WATCH = new Func1<Watched, Observable<? extends Metrics>>() {

        @Override
        public Observable<? extends Metrics> call(Watched w) {
            return w.watch();
        }
    };

}
