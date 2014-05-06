package com.github.davidmoten.util.rx;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class WatchServiceObservableTest {

	@Test
	public void testNoEventsThrownIfFileDoesNotExist()
			throws InterruptedException {
		File file = new File("target/does-not-exist");
		@SuppressWarnings("unchecked")
		Observable<WatchEvent<?>> events = WatchServiceObservable.from(file,
				ENTRY_MODIFY);
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
		@SuppressWarnings("unchecked")
		Observable<WatchEvent<?>> events = WatchServiceObservable.from(file,
				ENTRY_CREATE, ENTRY_MODIFY);
		final CountDownLatch latch = new CountDownLatch(1);
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
						latch.countDown();
					}
				});

		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file, true);
		fos.write("hello there".getBytes());
		fos.close();
		assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
		sub.unsubscribe();
	}
}
