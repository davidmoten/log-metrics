package com.github.davidmoten.logmetrics;

import java.util.List;

import rx.Observable;

public class Watcher {

	private final List<Watch> watched;

	public Watcher(List<Watch> watched) {
		this.watched = watched;
	}

	public Observable<Metrics> run() {
		return Observable.from(watched).flatMap(w -> w.watch());
	}

}
