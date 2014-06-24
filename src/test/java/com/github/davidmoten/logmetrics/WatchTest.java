package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
                .lift(Logging.logger().showValue().log()).take(5)
                .subscribeOn(Schedulers.computation()).subscribe();
        PrintWriter out = new PrintWriter(log);
        while (true) {
            out.println("hi there from Dave at " + System.currentTimeMillis());
            out.flush();
            Thread.sleep(300);
        }
    }
}
