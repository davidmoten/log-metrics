package com.github.davidmoten.logmetrics;

import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import rx.Observable;

public class Store<T> {

	private final ConcurrentSkipListMap<String, ConcurrentSkipListSet<Timestamped<T>>> map = new ConcurrentSkipListMap<>();
	private final Object insertLock = new Object();

	public Store<T> add(long time, String key, T value) {
		NavigableSet<Timestamped<T>> set = map.get(key);
		// double checked locking pattern gives better concurrent insert
		// performance
		if (set == null) {
			synchronized (insertLock) {
				set = map.get(key);
				if (set == null)
					map.put(key, new ConcurrentSkipListSet<>());
			}
		}
		set.add(new Timestamped<T>(time, value));
		return this;
	}

	public Observable<Timestamped<T>> values(String key, long startTime,
			long finishTime) {
		return Observable.just(map).flatMap(
				map -> {
					NavigableSet<Timestamped<T>> set = map.get(key);
					if (set == null)
						return Observable.empty();
					else {
						SortedSet<Timestamped<T>> s = set.subSet(
								new Timestamped<T>(startTime, null),
								new Timestamped<T>(finishTime, null));
						return Observable.from(s);
					}
				});
	}

}
