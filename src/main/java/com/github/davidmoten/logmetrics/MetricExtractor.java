package com.github.davidmoten.logmetrics;

import rx.Observable;

public interface MetricExtractor {

    Observable<? extends Metrics> extract(String category, String line);

}
