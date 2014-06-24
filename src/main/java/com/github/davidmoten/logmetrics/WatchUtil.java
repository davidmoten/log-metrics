package com.github.davidmoten.logmetrics;

import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.schedulers.Schedulers;

public class WatchUtil {

    public static Watch merge(Collection<Watch> watches) {
        return () -> {
            Observable<Metrics> o = Observable.empty();
            for (Watch watch : watches) {
                o = watch.metrics().subscribeOn(Schedulers.newThread()).mergeWith(o);
            }
            return o;
        };
    }

    public static Watch merge(Watch... watches) {
        return merge(Arrays.asList(watches));
    }

}
