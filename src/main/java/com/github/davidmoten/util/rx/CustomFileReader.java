package com.github.davidmoten.util.rx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.subjects.PublishSubject;

public class CustomFileReader extends Reader {

    private final FileReader reader;
    private final AtomicLong bytesRead = new AtomicLong(0);
    private final PublishSubject<Long> subject = PublishSubject.create();

    public CustomFileReader(File file, long skipChars) {
        try {
            reader = new FileReader(file);
            reader.skip(skipChars);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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

    public Observable<Long> charsRead() {
        return subject;
    }

}
