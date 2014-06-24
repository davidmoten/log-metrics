package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.schedulers.Schedulers;

import com.github.davidmoten.rx.slf4j.Logging;

public class WatchTest {

    @Test
    public void test() throws InterruptedException, IOException {

        MetricExtractor extractor = (category, line) -> Observable.just(new Metrics(category,
                System.currentTimeMillis(), Level.INFO, line));
        File log = new File("target/log");
        log.delete();
        Watch.builder().file(log).extractor(extractor).build().watch()
                .lift(Logging.logger().showValue().log()).subscribeOn(Schedulers.computation())
                .subscribe();
        PrintWriter out = new PrintWriter(log);
        Observable.interval(300, TimeUnit.MILLISECONDS).doOnNext(n -> {
            out.println("hi from Dave at " + n);
            out.flush();
        }).observeOn(Schedulers.trampoline()).subscribe();
        Thread.sleep(30000);
    }
}
