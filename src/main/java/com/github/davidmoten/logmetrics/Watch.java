package com.github.davidmoten.logmetrics;

import rx.Observable;

public interface Watch {
    Observable<Metrics> metrics();
}
