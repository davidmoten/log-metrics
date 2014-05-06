package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class FileObservable {

	public static Observable<File> changes(File file) {
		return Observable.create(new FileOnSubscribe(file));
	}

	public static class FileOnSubscribe implements OnSubscribe<File> {
		private final File file;

		public FileOnSubscribe(File file) {
			this.file = file;

		}

		@Override
		public void call(Subscriber<? super File> subscriber) {
			Path path = Paths.get(file.toURI());

			if (path == null)
				subscriber.onError(new RuntimeException("file not found: "
						+ file));
			else {
				try {
					WatchService service = path.getFileSystem()
							.newWatchService();

				} catch (IOException e) {
					subscriber.onError(e);
				}
			}
		}
	}

}
