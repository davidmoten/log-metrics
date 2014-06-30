package com.github.davidmoten.logmetrics;

import java.util.List;

import rx.Observable;

public class Watcher {

    private final List<FileWatch> watched;

    public Watcher(List<FileWatch> watched) {
	this.watched = watched;
    }

    public Observable<Line> run() {
	return Observable.from(watched).flatMap(w -> w.lines());
    }

}
