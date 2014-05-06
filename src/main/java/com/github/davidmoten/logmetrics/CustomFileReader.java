package com.github.davidmoten.logmetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.subjects.PublishSubject;

public class CustomFileReader extends Reader {

	private final FileReader reader;
	private final AtomicInteger bytesRead = new AtomicInteger(0);
	private final PublishSubject<Integer> subject = PublishSubject.create();

	public CustomFileReader(File file) {
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int size = reader.read(cbuf, off, len);
		boolean finished = size == -1;
		if (!finished)
			bytesRead.addAndGet(size);
		else {
			subject.onNext(bytesRead.get());
			subject.onCompleted();
		}
		return size;
	}

	@Override
	public void close() throws IOException {
		close();
	}

	public Observable<Integer> bytesRead() {
		return subject;
	}

}
