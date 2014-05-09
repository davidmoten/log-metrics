package com.github.davidmoten.util.rx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FileTailerTest {

    @Test
    public void testFileTailingFromStartOfFile() throws InterruptedException, IOException {
        File log = new File("target/test.log");
        log.delete();
        log.createNewFile();
        append(log, "a0");
        FileTailer tailer = FileTailer.builder().file(log).startPositionBytes(0).build();
        @SuppressWarnings("unchecked")
        final List<String> list = Mockito.mock(List.class);
        InOrder inOrder = Mockito.inOrder(list);
        Subscription sub = tailer.tail(50).subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String line) {
                        System.out.println("received: '" + line + "'");
                        list.add(line);
                    }
                });
        // pause to setup WatchService
        Thread.sleep(100);
        append(log, "a1");
        append(log, "a2");
        Thread.sleep(100);
        inOrder.verify(list).add("a0");
        inOrder.verify(list).add("a1");
        inOrder.verify(list).add("a2");
        inOrder.verifyNoMoreInteractions();
        sub.unsubscribe();
    }

    private static void append(File file, String line) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(line.getBytes());
            fos.write('\n');
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
