package com.github.davidmoten.util.rx;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class WatchServiceObservableTest {

    @Test
    public void testNoEventsThrownIfFileDoesNotExist() throws InterruptedException {
        File file = new File("target/does-not-exist");
        @SuppressWarnings("unchecked")
        Observable<WatchEvent<?>> events = WatchServiceObservable.from(file, ENTRY_MODIFY);
        final CountDownLatch latch = new CountDownLatch(1);
        Subscription sub = events.subscribeOn(Schedulers.io()).subscribe(
                new Observer<WatchEvent<?>>() {

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        latch.countDown();
                    }

                    @Override
                    public void onNext(WatchEvent<?> arg0) {
                        latch.countDown();
                    }
                });
        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
        sub.unsubscribe();
    }

    @Test
    public void testEvents() throws InterruptedException, IOException {
        File file = new File("target/f");
        file.delete();
        file.createNewFile();
        @SuppressWarnings("unchecked")
        Observable<WatchEvent<?>> events = WatchServiceObservable.from(file, ENTRY_MODIFY);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Kind> eventKinds = Mockito.mock(List.class);
        InOrder inOrder = Mockito.inOrder(eventKinds);
        Subscription sub = events.subscribeOn(Schedulers.io()).subscribe(
                new Observer<WatchEvent<?>>() {

                    @Override
                    public void onCompleted() {
                        System.out.println("completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WatchEvent<?> event) {
                        System.out.println("event=" + event);
                        eventKinds.add(event.kind());
                        latch.countDown();
                    }
                });
        Thread.sleep(200);
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write("hello there".getBytes());
        fos.close();
        assertTrue(latch.await(30000, TimeUnit.MILLISECONDS));
        inOrder.verify(eventKinds).add(StandardWatchEventKinds.ENTRY_MODIFY);
        inOrder.verifyNoMoreInteractions();
        sub.unsubscribe();
    }
}
