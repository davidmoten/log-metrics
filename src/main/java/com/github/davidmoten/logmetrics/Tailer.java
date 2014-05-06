package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;

public class Tailer {

	private final File file;
	private final AtomicInteger skipBytes = new AtomicInteger();

	public Tailer(File file, int skipBytes) {
		this.file = file;
		this.skipBytes.set(skipBytes);

	}

	public Observable<String> tail() {
		Path path = Paths.get(file.getParentFile().toURI());
		if (path == null)
			return Observable.error(new FileNotFoundException(
					"file not found: " + file));
		else {
			try {
				WatchService watchService = path.getFileSystem()
						.newWatchService();
				path.register(watchService,
						StandardWatchEventKinds.ENTRY_MODIFY);
				return WatchServiceObservable.from(watchService);
			} catch (IOException e) {
				return Observable.error(new RuntimeException(e));
			}
		}
	}
}
