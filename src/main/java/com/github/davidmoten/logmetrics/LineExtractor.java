package com.github.davidmoten.logmetrics;

import rx.Observable;

public interface LineExtractor {

    Observable<? extends Line> extract(String category, String line);

}
