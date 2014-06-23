package com.github.davidmoten.logmetrics;

import java.util.List;

import rx.Observable;

public class Watcher {

	private final List<Watched> watched;

	public Watcher(List<Watched> watched) {
		this.watched = watched;
	}

	public Observable<Metrics> run() {
		return Observable.from(watched).flatMap(w -> w.watch());
	}

}
