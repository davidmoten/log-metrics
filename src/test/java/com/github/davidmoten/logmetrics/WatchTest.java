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

        File log = new File("target/log");
        log.delete();
        File log2 = new File("target/log2");
        log2.delete();

        MetricExtractor extractor = (category, line) -> Observable.just(new Metrics(category,
                System.currentTimeMillis(), Level.INFO, line));

        Watch watch1 = FileWatch.builder().file(log).category("cat1").extractor(extractor).build();
        Watch watch2 = FileWatch.builder().file(log2).category("cat2").extractor(extractor).build();

        Watch watch = WatchUtil.merge(watch1, watch2);

        watch.metrics().lift(Logging.logger().showValue().log()).subscribe();

        PrintWriter out = new PrintWriter(log);
        PrintWriter out2 = new PrintWriter(log2);
        Observable.interval(300, TimeUnit.MILLISECONDS).doOnNext(n -> {
            out.println("hi from Dave at " + n);
            out2.println("hi from Dave2 at " + n);
            out.flush();
            out2.flush();
        }).observeOn(Schedulers.trampoline()).subscribe();
        Thread.sleep(5000);
        out.close();
        out2.close();
    }
}
