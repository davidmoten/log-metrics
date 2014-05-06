package com.github.davidmoten.util.rx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

public class WatchServiceObservable {

	public static Observable<WatchEvent<?>> from(WatchService watchService) {
		return Observable.create(new WatchServiceOnSubscribe(watchService));
	}

	public static Observable<WatchEvent<?>> from(final File file,
			Kind<Path>... kinds) {
		return watchService(file, kinds).flatMap(
				new Func1<WatchService, Observable<WatchEvent<?>>>() {

					@Override
					public Observable<WatchEvent<?>> call(
							WatchService watchService) {
						return from(watchService);
					}
				}).filter(onlyRelatedTo(file));
	}

	public static Observable<WatchService> watchService(final File file,
			final Kind<Path>... kinds) {
		return Observable.create(new OnSubscribe<WatchService>() {

			@Override
			public void call(Subscriber<? super WatchService> subscriber) {
				final Path path;
				if (file.isDirectory())
					path = Paths.get(file.toURI());
				else
					path = Paths.get(file.getParentFile().toURI());
				try {
					WatchService watchService = path.getFileSystem()
							.newWatchService();
					path.register(watchService, kinds);
					subscriber.onNext(watchService);
					subscriber.onCompleted();
				} catch (IOException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	private static Func1<WatchEvent<?>, Boolean> onlyRelatedTo(final File file) {
		return new Func1<WatchEvent<?>, Boolean>() {

			@Override
			public Boolean call(WatchEvent<?> event) {
				if (file.isDirectory())
					return true;
				else {
					Object context = event.context();
					if (context != null && context instanceof Path) {
						Path p = (Path) context;
						return p.toFile().equals(file);
					} else
						return false;
				}
			}
		};
	}

	public static class WatchServiceOnSubscribe implements
			OnSubscribe<WatchEvent<?>> {

		private final WatchService watchService;

		public WatchServiceOnSubscribe(WatchService watchService) {
			this.watchService = watchService;
		}

		@Override
		public void call(Subscriber<? super WatchEvent<?>> subscriber) {
			try {
				if (subscriber.isUnsubscribed())
					return;
				// get the first event before looping
				WatchKey key = watchService.poll(100, TimeUnit.MILLISECONDS);
				while (true) {
					if (subscriber.isUnsubscribed())
						return;
					// we have a polled event, now we traverse it and
					// receive all the states from it
					for (WatchEvent<?> event : key.pollEvents()) {
						if (subscriber.isUnsubscribed())
							return;
						else
							subscriber.onNext(event);
					}
					boolean valid = key.reset();
					if (!valid) {
						subscriber.onCompleted();
						return;
					} else
						key = watchService.poll(100, TimeUnit.MILLISECONDS);
				}
			} catch (InterruptedException e) {
				subscriber.onError(e);
			}
		}

	}

}
